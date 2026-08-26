"use strict";

// Year
const yearEl = document.getElementById("year");
if (yearEl) yearEl.textContent = String(new Date().getFullYear());

// Mobile nav
const hamburger = document.getElementById("hamburger");
const navLinks = document.getElementById("navLinks");
if (hamburger && navLinks) {
  hamburger.addEventListener("click", () => navLinks.classList.toggle("open"));
  navLinks
    .querySelectorAll("a")
    .forEach((a) =>
      a.addEventListener("click", () => navLinks.classList.remove("open")),
    );
}

// Nav shadow on scroll
const nav = document.querySelector(".nav");
if (nav) {
  const onScroll = () => nav.classList.toggle("scrolled", window.scrollY > 8);
  window.addEventListener("scroll", onScroll, { passive: true });
  onScroll();
}

// Reveal on scroll
const reveals = document.querySelectorAll(".reveal");
if ("IntersectionObserver" in window && reveals.length) {
  const io = new IntersectionObserver(
    (entries) => {
      for (const e of entries)
        if (e.isIntersecting) {
          e.target.classList.add("in");
          io.unobserve(e.target);
        }
    },
    { threshold: 0.12, rootMargin: "0px 0px -40px 0px" },
  );
  reveals.forEach((el) => io.observe(el));
} else {
  reveals.forEach((el) => el.classList.add("in"));
}

// i18n — lightweight, no dependencies
const dict = {
  id: {
    "nav.features": "Fitur",
    "nav.how": "Cara kerja",
    "nav.screenshots": "Screenshot",
    "nav.faq": "FAQ",
    "nav.download": "Download",
    "hero.eyebrow": "100% Offline · Tanpa akun · Tanpa internet",
    "hero.title":
      "Tahu sampai <em>kapan uangmu tahan</em>, tanpa spreadsheet dingin.",
    "hero.desc":
      'Bareuang menjawab satu pertanyaan penting: <strong>"dengan pola pengeluaranku sekarang, sampai kapan uangku tahan?"</strong> — lewat Financial Runway, multi-wallet, dan budget yang menjaga pengeluaran tetap waras.',
    "hero.download": "Download di Play Store",
    "hero.viewFeatures": "Lihat fitur",
    "hero.note": "Gratis · Tidak ada iklan · Data tetap di HP kamu",
    "hero.badge1": "No Internet permission",
    "hero.badge2": "Backup .json offline",
    "hero.badge3": "Cozy, bukan kaku",
    "hero.floatTitle": "Runway: 47 hari lagi",
    "hero.floatSub": "Burn rate Rp 68.400 / hari · Estimasi habis 12 Feb",
    "trust.ocr": "Scan struk OCR on-device",
    "trust.note":
      "Transaksi baru aktif setelah budget bulanan diatur — biar runway selalu akurat.",
    "features.kicker": "Fitur cozy",
    "features.title": "Semua yang kamu butuh, tanpa yang bikin pusing",
    "features.sub":
      "Dirancang untuk pemakaian harian — cepat, lokal, dan tidak menggurui.",
    "features.c1t": "Financial Runway",
    "features.c1d":
      "Hitung burn rate harian & prediksi kapan saldo habis (Estimated Death Day). Tahu sejak awal kapan harus rem.",
    "features.c2t": "Monthly & Category Budget",
    "features.c2d":
      "Kunci anggaran bulanan + limit per kategori (Food, Transport, dll). Lewat limit? langsung kelihatan.",
    "features.c3d":
      "Tunai, BCA, GoPay, OVO — total net worth real-time dalam satu layar.",
    "features.c4t": "Transfer Antar Dompet",
    "features.c4d":
      "Smart switch anti-duplikasi + 1-tap swap. Pindah uang antar dompet tanpa double catat.",
    "features.c5t": "Import Mutasi CSV",
    "features.c5d":
      "Impor BCA/e-wallet (delimiter , / ;, 8 format tanggal, dedup otomatis). 5MB guard.",
    "features.c6t": "Scan Struk (OCR)",
    "features.c6d":
      "Foto struk → ML Kit on-device. Preview kertas termal, edit merchant/total/category, tanpa upload.",
    "features.c7t": "Savings Goals",
    "features.c7d":
      "Target tabungan + kalkulator setoran/penarikan. Progress jelas, motivasi jalan.",
    "features.c8t": "Bill Reminder",
    "features.c8d":
      "Tagihan rutin, notifikasi H-3, auto-rollover & auto-refund jika batal bayar.",
    "features.c9t": "Split Bill",
    "features.c9d":
      "Patungan makan/belanja (pajak & service) + share 1-klik ke WhatsApp.",
    "how.kicker": "Cara kerja",
    "how.title": "3 langkah, langsung jalan",
    "how.s1t": "Atur Budget Bulanan",
    "how.s1d":
      "Set limit bulanan + per kategori. Ini jadi acuan runway & guard transaksi.",
    "how.s2t": "Catat / Impor / Scan",
    "how.s2d":
      "Input manual, impor CSV BCA/e-wallet, atau foto struk. Semua dicek saldo & dedup.",
    "how.s3t": "Pantau Runway",
    "how.s3d":
      "Lihat berapa hari uangmu tahan, tren cashflow, dan distribusi pengeluaran.",
    "shots.kicker": "Tampilan",
    "shots.title": "Cozy di mata, jelas di angka",
    "shots.sub":
      "Geser untuk lihat Dashboard, Budget, Bills, Goals, Transfer, dan Analytics.",
    "faq.title": "Yang sering ditanya",
    "faq.q1": "Apakah data saya dikirim ke server?",
    "faq.a1":
      "Tidak. Bareuang 100% offline. Tidak minta izin INTERNET, tidak ada akun, tidak ada upload. Semua di Room DB lokal HP kamu. Backup pun file .json offline.",
    "faq.q2": "Kenapa harus atur budget dulu sebelum catat transaksi?",
    "faq.a2":
      'Ini "Budget Gate" — biar Financial Runway punya acuan yang akurat. Tanpa budget, estimasi hari bertahan tidak bisa dihitung dengan benar.',
    "faq.q3": "Apakah bisa impor mutasi BCA / GoPay / OVO?",
    "faq.a3":
      "Bisa via Import CSV. Mendukung delimiter koma/semicolon, debit-kredit terpisah, 8 format tanggal, dan dedup otomatis berdasarkan tanggal + nominal + merchant.",
    "faq.q4": "Scan struk butuh internet?",
    "faq.a4":
      "Tidak. OCR pakai ML Kit on-device. Foto struk diproses lokal, preview kertas termal, kamu bisa edit sebelum simpan.",
    "faq.q5": "Apakah Bareuang memberi saran investasi?",
    "faq.a5":
      "Tidak. Bareuang hanya alat pencatatan & estimasi. Bukan penasihat keuangan — lihat Disclaimer di Terms.",
    "faq.q6": "Bagaimana cara hapus semua data?",
    "faq.a6":
      "Pengaturan → Hapus Data, atau hapus langsung via Settings Android → Apps → Bareuang → Clear Data, atau uninstall. Karena offline, data hilang permanen.",
    "cta.title": "Siap tahu kapan uangmu habis — sebelum benar-benar habis?",
    "cta.sub": "Download Bareuang. Cozy, offline, dan jujur soal angka.",
    "cta.play": "Play Store — Coming Soon",
    "footer.desc":
      "Teman cozy buat uangmu. 100% offline, tanpa akun, tanpa internet. Dibuat dengan ❤️ di Indonesia.",
    "footer.product": "Produk",
    "footer.features": "Fitur",
    "footer.screenshots": "Screenshot",
    "footer.lang": "Bahasa: Indonesia · English",
  },
  en: {
    "nav.features": "Features",
    "nav.how": "How it works",
    "nav.screenshots": "Screenshots",
    "nav.faq": "FAQ",
    "nav.download": "Download",
    "hero.eyebrow": "100% Offline · No account · No internet",
    "hero.title":
      "Know <em>how long your money lasts</em> — no cold spreadsheets.",
    "hero.desc":
      'Bareuang answers one key question: <strong>"with my current spending, how long will my money last?"</strong> — via Financial Runway, multi-wallet, and budgets that keep spending sane.',
    "hero.download": "Download on Play Store",
    "hero.viewFeatures": "View features",
    "hero.note": "Free · No ads · Data stays on your phone",
    "hero.badge1": "No Internet permission",
    "hero.badge2": "Backup .json offline",
    "hero.badge3": "Cozy, not stiff",
    "hero.floatTitle": "Runway: 47 days left",
    "hero.floatSub": "Burn rate Rp 68,400 / day · Est. out Feb 12",
    "trust.ocr": "Receipt OCR on-device",
    "trust.note":
      "New transactions unlock after setting monthly budget — so runway stays accurate.",
    "features.kicker": "Cozy features",
    "features.title": "Everything you need, nothing that nags",
    "features.sub": "Built for daily use — fast, local, and honest.",
    "features.c1t": "Financial Runway",
    "features.c1d":
      "Daily burn rate & Estimated Death Day. Know early when to slow down.",
    "features.c2t": "Monthly & Category Budget",
    "features.c2d":
      "Lock monthly budget + per-category limits (Food, Transport, etc). Over limit? Instantly visible.",
    "features.c3d":
      "Cash, BCA, GoPay, OVO — real-time net worth in one screen.",
    "features.c4t": "Wallet Transfer",
    "features.c4d":
      "Smart anti-duplicate switch + 1-tap swap. Move money without double entries.",
    "features.c5t": "CSV Import",
    "features.c5d":
      "Import BCA/e-wallet (comma/semicolon, 8 date formats, auto dedup). 5MB guard.",
    "features.c6t": "Receipt Scan (OCR)",
    "features.c6d":
      "Snap receipt → ML Kit on-device. Thermal preview, edit merchant/total/category, no upload.",
    "features.c7t": "Savings Goals",
    "features.c7d":
      "Targets + deposit/withdraw calculator. Clear progress, real motivation.",
    "features.c8t": "Bill Reminder",
    "features.c8d":
      "Recurring bills, H-3 reminder, auto-rollover & auto-refund if cancelled.",
    "features.c9t": "Split Bill",
    "features.c9d":
      "Split dining/shopping (tax & service) + 1-tap share to WhatsApp.",
    "how.kicker": "How it works",
    "how.title": "3 steps, ready to go",
    "how.s1t": "Set Monthly Budget",
    "how.s1d":
      "Set monthly + per-category limits — the baseline for runway & guards.",
    "how.s2t": "Log / Import / Scan",
    "how.s2d":
      "Manual entry, CSV import, or receipt photo. All checked for balance & dedup.",
    "how.s3t": "Track Runway",
    "how.s3d":
      "See how many days your money lasts, cashflow trends, and spend breakdown.",
    "shots.kicker": "Screens",
    "shots.title": "Cozy on the eyes, clear on the numbers",
    "shots.sub":
      "Swipe to see Dashboard, Budget, Bills, Goals, Transfer, and Analytics.",
    "faq.title": "Frequently asked",
    "faq.q1": "Is my data sent to a server?",
    "faq.a1":
      "No. Bareuang is 100% offline. No INTERNET permission, no account, no upload. Everything in local Room DB. Even backup is an offline .json file.",
    "faq.q2": "Why set budget before logging transactions?",
    "faq.a2":
      "That's the Budget Gate — so Financial Runway has an accurate baseline. Without budget, the survival estimate can't be computed correctly.",
    "faq.q3": "Can I import BCA / GoPay / OVO statements?",
    "faq.a3":
      "Yes via CSV import. Supports comma/semicolon, split debit-credit, 8 date formats, and auto dedup by date + amount + merchant.",
    "faq.q4": "Does receipt scan need internet?",
    "faq.a4":
      "No. OCR uses on-device ML Kit. Photo is processed locally, thermal preview, editable before saving.",
    "faq.q5": "Does Bareuang give investment advice?",
    "faq.a5":
      "No. Bareuang is a logging & estimation tool, not a financial advisor — see Disclaimer in Terms.",
    "faq.q6": "How to delete all data?",
    "faq.a6":
      "Settings → Clear Data, or Android Settings → Apps → Bareuang → Clear Data, or uninstall. Offline means permanently gone.",
    "cta.title": "Ready to know when your money runs out — before it does?",
    "cta.sub": "Download Bareuang. Cozy, offline, and honest about numbers.",
    "cta.play": "Play Store — Coming Soon",
    "footer.desc":
      "Your cozy money companion. 100% offline, no account, no internet. Made with ❤️ in Indonesia.",
    "footer.product": "Product",
    "footer.features": "Features",
    "footer.screenshots": "Screenshots",
    "footer.lang": "Language: Indonesia · English",
  },
};

function applyLang(lang) {
  const d = dict[lang] || dict.id;
  document.documentElement.lang = lang;
  for (const el of document.querySelectorAll("[data-i18n]")) {
    const key = el.getAttribute("data-i18n");
    const val = d[key];
    if (val == null) continue;
    const hasSvg = el.querySelector("svg");
    if (val.includes("<")) {
      el.innerHTML = val;
      continue;
    }
    if (hasSvg && el.childNodes.length > 1) {
      const svgHtml = hasSvg.outerHTML;
      el.innerHTML = `${svgHtml} ${val}`;
    } else {
      el.textContent = val;
    }
  }
  // hero title/desc contain HTML already handled above
  for (const b of document.querySelectorAll(".lang-switch button")) {
    const active = b.dataset.lang === lang;
    b.classList.toggle("active", active);
    b.setAttribute("aria-pressed", String(active));
  }
  try {
    localStorage.setItem("bareuang_lang", lang);
  } catch {}
  document.title =
    lang === "en"
      ? "Bareuang — Your cozy money companion"
      : "Bareuang — Teman cozy buat uangmu";
  const metaDesc = document.querySelector('meta[name="description"]');
  if (metaDesc) {
    metaDesc.content =
      lang === "en"
        ? "Know how long your money lasts. Multi-wallet, bill tracking, and savings goals — 100% offline, no account, no internet."
        : "Tahu sampai kapan uangmu tahan. Kelola banyak dompet, pantau tagihan, dan wujudkan target tabungan — 100% offline, tanpa akun, tanpa internet.";
  }
}

const saved = (() => {
  try {
    return localStorage.getItem("bareuang_lang");
  } catch {
    return null;
  }
})();
const initial =
  saved === "en" || saved === "id"
    ? saved
    : (navigator.language || "").toLowerCase().startsWith("en")
      ? "en"
      : "id";
applyLang(initial);
for (const b of document.querySelectorAll(".lang-switch button"))
  b.addEventListener("click", () => applyLang(b.dataset.lang));
