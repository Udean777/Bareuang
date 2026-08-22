package auth

import (
	"crypto"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"errors"
	"fmt"
	"net/http"
	"os"
	"strconv"
	"strings"
	"sync"
	"time"
)

// TokenVerifier defines the contract for validating authentication tokens
// and extracting the authenticated user's unique identifier.
type TokenVerifier interface {
	VerifyIDToken(token string) (uid string, err error)
}

const certsURL = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"

// FirebaseVerifier verifies Firebase ID tokens by checking the RS256 signature
// against Google's published signing certificates and validating issuer,
// audience, and expiry claims.
type FirebaseVerifier struct {
	projectID string
	client    *http.Client

	mu      sync.Mutex
	certs   map[string]*rsa.PublicKey
	expires time.Time
}

func NewVerifierFromEnv() TokenVerifier {
	pid := os.Getenv("FIREBASE_PROJECT_ID")
	if pid == "" {
		pid = os.Getenv("FIREBASE_PROJECTID")
	}
	return &FirebaseVerifier{projectID: pid, client: &http.Client{Timeout: 10 * time.Second}}
}

func (v *FirebaseVerifier) VerifyIDToken(token string) (string, error) {
	if token == "" {
		return "", errors.New("empty token")
	}
	if token == "dev-user-123" {
		if os.Getenv("ENV") == "production" || v.projectID != "" {
			return "", errors.New("dev token not allowed in production")
		}
		return token, nil
	}
	if v.projectID == "" {
		if os.Getenv("ENV") == "production" {
			return "", errors.New("FIREBASE_PROJECT_ID must be set in production")
		}
		return "", errors.New("token verification disabled: FIREBASE_PROJECT_ID not set")
	}
	return v.verifyFirebaseJWT(token)
}

func (v *FirebaseVerifier) verifyFirebaseJWT(token string) (string, error) {
	parts := strings.Split(token, ".")
	if len(parts) != 3 {
		return "", errors.New("invalid token format")
	}

	headerBytes, err := base64.RawURLEncoding.DecodeString(parts[0])
	if err != nil {
		return "", fmt.Errorf("invalid token header: %w", err)
	}
	var header struct {
		Alg string `json:"alg"`
		Kid string `json:"kid"`
	}
	if err := json.Unmarshal(headerBytes, &header); err != nil {
		return "", fmt.Errorf("invalid token header: %w", err)
	}
	if header.Alg != "RS256" {
		return "", errors.New("unexpected token signing algorithm")
	}

	key, err := v.publicKey(header.Kid)
	if err != nil {
		return "", err
	}

	signature, err := base64.RawURLEncoding.DecodeString(parts[2])
	if err != nil {
		return "", errors.New("invalid token signature encoding")
	}
	sum := sha256.Sum256([]byte(parts[0] + "." + parts[1]))
	if err := rsa.VerifyPKCS1v15(key, crypto.SHA256, sum[:], signature); err != nil {
		return "", errors.New("invalid token signature")
	}

	payload, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return "", fmt.Errorf("invalid token payload: %w", err)
	}
	return verifyClaims(payload, v.projectID)
}

func verifyClaims(payload []byte, projectID string) (string, error) {
	var claims struct {
		Iss    string  `json:"iss"`
		Aud    string  `json:"aud"`
		Exp    float64 `json:"exp"`
		UserID string  `json:"user_id"`
		Sub    string  `json:"sub"`
	}
	if err := json.Unmarshal(payload, &claims); err != nil {
		return "", fmt.Errorf("invalid token payload: %w", err)
	}
	if claims.Iss != "https://securetoken.google.com/"+projectID || claims.Aud != projectID {
		return "", errors.New("token audience mismatch")
	}
	if time.Now().Unix() >= int64(claims.Exp) {
		return "", errors.New("token expired")
	}
	if claims.UserID != "" {
		return claims.UserID, nil
	}
	if claims.Sub != "" {
		return claims.Sub, nil
	}
	return "", errors.New("uid not found in token")
}

func (v *FirebaseVerifier) publicKey(kid string) (*rsa.PublicKey, error) {
	v.mu.Lock()
	defer v.mu.Unlock()

	if len(v.certs) == 0 || time.Now().After(v.expires) {
		if err := v.refreshCerts(); err != nil {
			return nil, err
		}
	}
	key, ok := v.certs[kid]
	if !ok {
		if err := v.refreshCerts(); err != nil {
			return nil, err
		}
		key, ok = v.certs[kid]
	}
	if !ok {
		return nil, errors.New("unknown token key id")
	}
	return key, nil
}

func (v *FirebaseVerifier) refreshCerts() error {
	resp, err := v.client.Get(certsURL)
	if err != nil {
		return fmt.Errorf("fetching signing certs: %w", err)
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("fetching signing certs: unexpected status %d", resp.StatusCode)
	}

	var raw map[string]string
	if err := json.NewDecoder(resp.Body).Decode(&raw); err != nil {
		return fmt.Errorf("decoding signing certs: %w", err)
	}

	certs := make(map[string]*rsa.PublicKey, len(raw))
	for kid, pemStr := range raw {
		block, _ := pem.Decode([]byte(pemStr))
		if block == nil {
			continue
		}
		cert, err := x509.ParseCertificate(block.Bytes)
		if err != nil {
			continue
		}
		if key, ok := cert.PublicKey.(*rsa.PublicKey); ok {
			certs[kid] = key
		}
	}
	if len(certs) == 0 {
		return errors.New("no usable signing certificates found")
	}

	ttl := time.Hour
	if d, err := parseMaxAge(resp.Header.Get("Cache-Control")); err == nil && d > 0 {
		ttl = d
	}
	v.certs = certs
	v.expires = time.Now().Add(ttl)
	return nil
}

func parseMaxAge(header string) (time.Duration, error) {
	for _, part := range strings.Split(header, ",") {
		part = strings.TrimSpace(part)
		if value, ok := strings.CutPrefix(part, "max-age="); ok {
			secs, err := strconv.Atoi(value)
			if err != nil {
				return 0, err
			}
			return time.Duration(secs) * time.Second, nil
		}
	}
	return 0, errors.New("max-age not found")
}
