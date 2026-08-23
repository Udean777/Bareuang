package auth

import (
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"strings"
	"testing"
	"time"
)

func b64(b []byte) string { return base64.RawURLEncoding.EncodeToString(b) }

func signToken(t *testing.T, key *rsa.PublicKey, header, payload string) string {
	t.Helper()
	signingInput := b64([]byte(header)) + "." + b64([]byte(payload))
	sum := sha256.Sum256([]byte(signingInput))
	sig, err := rsa.SignPKCS1v15(rand.Reader, mustPriv(t), crypto.SHA256, sum[:])
	if err != nil {
		t.Fatal(err)
	}
	return signingInput + "." + b64(sig)
}

var privKey *rsa.PrivateKey

func mustPriv(t *testing.T) *rsa.PrivateKey {
	t.Helper()
	if privKey == nil {
		k, err := rsa.GenerateKey(rand.Reader, 2048)
		if err != nil {
			t.Fatal(err)
		}
		privKey = k
	}
	return privKey
}

func newTestVerifier(t *testing.T) *FirebaseVerifier {
	mustPriv(t)
	return &FirebaseVerifier{
		projectID: "test-project",
		certs:     map[string]*rsa.PublicKey{"test-kid": &privKey.PublicKey},
		expires:   time.Now().Add(time.Hour),
	}
}

func TestVerifyIDToken_Valid(t *testing.T) {
	v := newTestVerifier(t)
	payload, _ := json.Marshal(map[string]interface{}{
		"iss":     "https://securetoken.google.com/test-project",
		"aud":     "test-project",
		"exp":     time.Now().Add(time.Hour).Unix(),
		"user_id": "uid-abc",
	})
	token := signToken(t, &privKey.PublicKey,
		`{"alg":"RS256","kid":"test-kid"}`, string(payload))

	uid, err := v.VerifyIDToken(token)
	if err != nil {
		t.Fatalf("expected valid token, got: %v", err)
	}
	if uid != "uid-abc" {
		t.Fatalf("want uid-abc, got %s", uid)
	}
}

func TestVerifyIDToken_RejectsTamperedPayload(t *testing.T) {
	v := newTestVerifier(t)
	payload, _ := json.Marshal(map[string]interface{}{
		"iss":     "https://securetoken.google.com/test-project",
		"aud":     "test-project",
		"exp":     time.Now().Add(time.Hour).Unix(),
		"user_id": "victim",
	})
	token := signToken(t, &privKey.PublicKey,
		`{"alg":"RS256","kid":"test-kid"}`, string(payload))

	parts := strings.Split(token, ".")
	forgedPayload, _ := json.Marshal(map[string]interface{}{
		"iss":     "https://securetoken.google.com/test-project",
		"aud":     "test-project",
		"exp":     time.Now().Add(time.Hour).Unix(),
		"user_id": "attacker",
	})
	forged := parts[0] + "." + b64(forgedPayload) + "." + parts[2]

	if _, err := v.VerifyIDToken(forged); err == nil {
		t.Fatal("forged payload must be rejected")
	}
}

func TestVerifyIDToken_RejectsWrongAudience(t *testing.T) {
	v := newTestVerifier(t)
	payload := `{"iss":"https://securetoken.google.com/other","aud":"other","exp":9999999999,"user_id":"u"}`
	token := signToken(t, &privKey.PublicKey, `{"alg":"RS256","kid":"test-kid"}`, payload)
	if _, err := v.VerifyIDToken(token); err == nil {
		t.Fatal("wrong audience must be rejected")
	}
}

func TestVerifyIDToken_RejectsExpired(t *testing.T) {
	v := newTestVerifier(t)
	payload := `{"iss":"https://securetoken.google.com/test-project","aud":"test-project","exp":1,"user_id":"u"}`
	token := signToken(t, &privKey.PublicKey, `{"alg":"RS256","kid":"test-kid"}`, payload)
	if _, err := v.VerifyIDToken(token); err == nil {
		t.Fatal("expired token must be rejected")
	}
}

func TestVerifyClaims_SubFallback(t *testing.T) {
	uid, err := verifyClaims([]byte(`{"iss":"https://securetoken.google.com/p","aud":"p","exp":9999999999,"sub":"sub-1"}`), "p")
	if err != nil || uid != "sub-1" {
		t.Fatalf("want sub-1, got %q err %v", uid, err)
	}
}
