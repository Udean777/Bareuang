/**
 * Vercel Serverless — proxy APK dari GitHub Releases dengan header yang benar.
 * Mengatasi: chunked/302 tanpa Content-Length + rate-limit api.github.com di client.
 * Same-origin /api/download → Chrome anggap download, tidak stuck 100%.
 */
const GH_URL = "https://github.com/Udean777/Bareuang/releases/latest/download/Bareuang-latest.apk";

export default async function handler(req, res) {
  if (req.method !== "GET" && req.method !== "HEAD") {
    res.setHeader("Allow", "GET, HEAD");
    return res.status(405).json({ error: "Method not allowed" });
  }
  // HEAD: cek ketersediaan tanpa stream body
  if (req.method === "HEAD") {
    try {
      const head = await fetch(GH_URL, { method: "HEAD", redirect: "follow" });
      res.setHeader("Content-Type", "application/vnd.android.package-archive");
      res.setHeader("Content-Disposition", 'attachment; filename="Bareuang-latest.apk"');
      const len = head.headers.get("content-length");
      if (len) res.setHeader("Content-Length", len);
      res.setHeader("Cache-Control", "public, max-age=300, s-maxage=300");
      return res.status(head.ok ? 200 : 302).end();
    } catch {
      return res.redirect(302, GH_URL);
    }
  }

  try {
    const ghRes = await fetch(GH_URL, { redirect: "follow" });
    if (!ghRes.ok || !ghRes.body) {
      return res.redirect(302, GH_URL);
    }
    res.setHeader("Content-Type", "application/vnd.android.package-archive");
    res.setHeader("Content-Disposition", 'attachment; filename="Bareuang-latest.apk"');
    const len = ghRes.headers.get("content-length");
    if (len) res.setHeader("Content-Length", len);
    res.setHeader("Cache-Control", "public, max-age=300, s-maxage=300");
    // Stream body langsung tanpa buffer (hemat memori di serverless)
    const reader = ghRes.body.getReader();
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      res.write(value);
    }
    res.end();
  } catch (e) {
    return res.redirect(302, GH_URL);
  }
}
