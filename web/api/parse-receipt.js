/**
 * Vercel Serverless — proxy Gemini 2.5 Flash untuk scan struk.
 * - POST { image_base64 } (data:image/jpeg;base64,... atau raw base64, max ~3MB)
 * - Memanggil gemini-2.5-flash dan mengembalikan JSON terstruktur
 * - API key hanya di env Vercel (GEMINI_API_KEY), tidak pernah ke APK
 */

const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
const MAX_BODY_BYTES = 3 * 1024 * 1024;

const RATE_LIMIT_RPD = Number(process.env.RATE_LIMIT_RPD || 20);
const RATE_WINDOW_MS = 24 * 60 * 60 * 1000;
const _rateMap = new Map(); // ip -> { count, windowStart }

function getClientIp(req) {
  const fwd = req.headers["x-forwarded-for"];
  if (typeof fwd === "string" && fwd.length) return fwd.split(",")[0].trim();
  return req.headers["x-real-ip"] || req.socket?.remoteAddress || "unknown";
}

function checkRateLimit(ip) {
  // prune expired entries (prevent memory leak)
  const now = Date.now();
  for (const [k, v] of _rateMap) {
    if (now - v.windowStart >= RATE_WINDOW_MS) _rateMap.delete(k);
  }
  const entry = _rateMap.get(ip);
  if (!entry || now - entry.windowStart >= RATE_WINDOW_MS) {
    _rateMap.set(ip, { count: 1, windowStart: now });
    return { allowed: true, remaining: RATE_LIMIT_RPD - 1, resetMs: RATE_WINDOW_MS };
  }
  if (entry.count >= RATE_LIMIT_RPD) {
    return { allowed: false, remaining: 0, resetMs: entry.windowStart + RATE_WINDOW_MS - now };
  }
  entry.count += 1;
  return { allowed: true, remaining: RATE_LIMIT_RPD - entry.count, resetMs: entry.windowStart + RATE_WINDOW_MS - now };
}

const ALLOWED_MIMES = new Set(["image/jpeg", "image/png", "image/webp", "image/gif"]);
function isValidImageMagic(b64) {
  try {
    const buf = Buffer.from(b64.slice(0, 32), "base64");
    if (buf.length < 4) return false;
    // JPEG FF D8 FF, PNG 89 50 4E 47, GIF 47 49 46 38, WebP 52 49 46 46 ... 57 45 42 50
    if (buf[0] === 0xFF && buf[1] === 0xD8 && buf[2] === 0xFF) return true;
    if (buf[0] === 0x89 && buf[1] === 0x50 && buf[2] === 0x4E && buf[3] === 0x47) return true;
    if (buf[0] === 0x47 && buf[1] === 0x49 && buf[2] === 0x46 && buf[3] === 0x38) return true;
    if (buf[0] === 0x52 && buf[1] === 0x49 && buf[2] === 0x46 && buf[3] === 0x46) {
      if (buf.length >= 12 && buf[8] === 0x57 && buf[9] === 0x45 && buf[10] === 0x42 && buf[11] === 0x50) return true;
    }
    return false;
  } catch { return false; }
}

export default async function handler(req, res) {
  if (req.method === "OPTIONS") {
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type");
    return res.status(204).end();
  }
  if (req.method !== "POST") {
    res.setHeader("Allow", "POST");
    return res.status(405).json({ error: "Method not allowed" });
  }
  res.setHeader("Access-Control-Allow-Origin", "*");

  const len = Number(req.headers["content-length"] || 0);
  if (len > MAX_BODY_BYTES) {
    return res.status(413).json({ error: "Image too large (max 3MB base64)" });
  }

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    return res.status(500).json({ error: "Server misconfigured: GEMINI_API_KEY missing" });
  }

  let body = req.body;
  if (typeof body === "string") {
    try { body = JSON.parse(body); } catch { return res.status(400).json({ error: "Invalid JSON" }); }
  }
  const raw = body?.image_base64;
  if (!raw || typeof raw !== "string") {
    return res.status(400).json({ error: "Missing image_base64" });
  }
  if (raw.length > MAX_BODY_BYTES) {
    return res.status(413).json({ error: "Image too large (max 3MB base64)" });
  }

  const ip = getClientIp(req);
  const rl = checkRateLimit(ip);
  res.setHeader("X-RateLimit-Limit", String(RATE_LIMIT_RPD));
  res.setHeader("X-RateLimit-Remaining", String(rl.remaining));
  res.setHeader("X-RateLimit-Reset", String(Math.ceil(rl.resetMs / 1000)));
  if (!rl.allowed) {
    return res.status(429).json({
      error: "Rate limit exceeded",
      detail: `Maks ${RATE_LIMIT_RPD} scan per hari per IP. Coba lagi dalam ${Math.ceil(rl.resetMs / 60000)} menit.`,
    });
  }

  // Extract base64 data + mime from data URL (or assume jpeg)
  let mimeType = "image/jpeg";
  let b64Data = raw;
  if (raw.startsWith("data:")) {
    const m = raw.match(/^data:([^;]+);base64,(.*)$/s);
    if (m) { mimeType = m[1]; b64Data = m[2]; }
  }
  if (!ALLOWED_MIMES.has(mimeType.toLowerCase())) {
    return res.status(400).json({ error: "Unsupported image type (allow jpeg/png/webp/gif)" });
  }
  if (!isValidImageMagic(b64Data)) {
    return res.status(400).json({ error: "Invalid image data" });
  }

  const systemPrompt = `Kamu adalah parser struk Indonesia. Dari gambar struk, ekstrak JSON dengan format:
{
  "merchant": string (nama toko/merchant, kosongkan jika tidak ada),
  "date": string (ISO 8601 YYYY-MM-DD, kosongkan jika tidak ada),
  "total": number (total bayar dalam rupiah tanpa titik/koma, 0 jika tidak ada),
  "category": string (salah satu: FOOD, SHOPPING, TRANSPORT, BILLS, ENTERTAINMENT, HEALTH, EDUCATION, SOCIAL, OTHER),
  "items": string[] (baris item, opsional),
  "raw_text": string (transkripsi teks struk yang dinormalisasi, baris dipisah \\n)
}
Aturan:
- total = angka pada baris TOTAL / GRAND TOTAL / JUMLAH / BAYAR / TAGIHAN (pilih yang terbesar jika ada beberapa). Abaikan subtotal/discount.
- category ditebak dari merchant & isi struk (mis. Indomaret->SHOPPING, kopi/restoran->FOOD, SPBU->TRANSPORT, PLN->BILLS).
- Kembalikan HANYA JSON valid, tanpa markdown.`;

  try {
    const gRes = await fetch(`${GEMINI_URL}?key=${encodeURIComponent(apiKey)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        systemInstruction: { parts: [{ text: systemPrompt }] },
        contents: [
          {
            role: "user",
            parts: [
              { text: "Parse struk ini ke JSON sesuai format di atas." },
              { inlineData: { mimeType, data: b64Data } },
            ],
          },
        ],
        generationConfig: {
          responseMimeType: "application/json",
          thinkingConfig: { thinkingBudget: 0 },
          maxOutputTokens: 1024,
        },
      }),
    });

    if (!gRes.ok) {
      const errText = await gRes.text().catch(() => "");
      return res.status(502).json({ error: "Gemini error", detail: errText.slice(0, 800) });
    }

    const gJson = await gRes.json();
    const content = gJson?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!content || typeof content !== "string") {
      return res.status(502).json({ error: "Empty response from Gemini" });
    }

    let parsed;
    try { parsed = JSON.parse(content); } catch {
      return res.status(502).json({ error: "Invalid JSON from model", raw: content.slice(0, 800) });
    }

    const total = Math.max(0, Math.floor(Number(parsed.total) || 0));
    const allowed = new Set(["FOOD","SHOPPING","TRANSPORT","BILLS","ENTERTAINMENT","HEALTH","EDUCATION","SOCIAL","OTHER"]);
    const cat = String(parsed.category || "OTHER").toUpperCase();
    return res.status(200).json({
      merchant: String(parsed.merchant || "").trim(),
      date: String(parsed.date || "").trim(),
      total,
      category: allowed.has(cat) ? cat : "OTHER",
      items: Array.isArray(parsed.items) ? parsed.items.map(String).slice(0, 30) : [],
      raw_text: String(parsed.raw_text || "").trim(),
    });
  } catch (e) {
    return res.status(500).json({ error: "Proxy error", detail: String(e?.message || e).slice(0, 500) });
  }
}
