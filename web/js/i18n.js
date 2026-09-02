"use strict";
const dict = {
  id: {
    "nav.features": "Fitur",
    "nav.how": "Cara kerja",
    "nav.screenshots": "Screenshot",
    "nav.faq": "FAQ",
    "nav.download": "Download",
    "hero.eyebrow": "Offline-first · Data lokal · OCR online opsional",
    "hero.title":
      "Tahu sampai <em>kapan uangmu tahan</em>, tanpa spreadsheet dingin.",
    "hero.desc":
      'Bareuang menjawab satu pertanyaan penting: <strong>"dengan pola pengeluaranku sekarang, sampai kapan uangku tahan?"</strong> — lewat Financial Runway, multi-wallet, dan budget yang menjaga pengeluaran tetap waras.',
    "hero.download": "Download",
    "hero.viewFeatures": "Lihat fitur",
    "hero.note": "Gratis · Tidak ada iklan · Data tetap di HP kamu",
    "hero.badge1": "OCR online, dengan consent",
    "hero.badge2": "Backup .json offline",
    "hero.badge3": "Cozy, bukan kaku",
    "hero.floatTitle": "Runway: 47 hari lagi",
    "hero.floatSub": "Burn rate Rp 68.400 / hari · Estimasi habis 12 Feb",
    "trust.ocr": "Scan struk AI opsional",
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
      "Foto struk → Google Gemini via proxy Bareuang (butuh internet dan consent). Preview termal, edit hasil sebelum disimpan lokal.",
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
      "Data keuangan utama tersimpan di Room DB lokal. Scan struk adalah fitur online opsional: setelah consent, foto dikirim melalui proxy Bareuang ke Google Gemini untuk diekstrak. Input manual dan import CSV tetap bisa dipakai tanpa scan.",
    "faq.q2": "Kenapa harus atur budget dulu sebelum catat transaksi?",
    "faq.a2":
      'Ini "Budget Gate" — biar Financial Runway punya acuan yang akurat. Tanpa budget, estimasi hari bertahan tidak bisa dihitung dengan benar.',
    "faq.q3": "Apakah bisa impor mutasi BCA / GoPay / OVO?",
    "faq.a3":
      "Bisa via Import CSV. Mendukung delimiter koma/semicolon, debit-kredit terpisah, 8 format tanggal, dan dedup otomatis berdasarkan tanggal + nominal + merchant.",
    "faq.q4": "Scan struk butuh internet?",
    "faq.a4":
      "Ya. Scan struk membutuhkan internet dan consent karena foto dikirim ke proxy Bareuang dan Google Gemini. Hasilnya dapat kamu edit sebelum disimpan ke database lokal.",
    "faq.q5": "Apakah Bareuang memberi saran investasi?",
    "faq.a5":
      "Tidak. Bareuang hanya alat pencatatan & estimasi. Bukan penasihat keuangan — lihat Disclaimer di Terms.",
    "faq.q6": "Bagaimana cara hapus semua data?",
    "faq.a6":
      "Pengaturan → Hapus Data, atau hapus langsung via Settings Android → Apps → Bareuang → Clear Data, atau uninstall. Karena offline, data hilang permanen.",
    "cta.title": "Siap tahu kapan uangmu habis — sebelum benar-benar habis?",
    "cta.sub": "Download Bareuang. Data lokal, OCR online opsional, dan jujur soal angka.",
    "cta.play": "Download",
    "footer.desc":
      "Teman cozy buat uangmu. Data utama lokal; Scan Struk online opsional dengan consent. Dibuat dengan ❤️ di Indonesia.",
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
    "hero.eyebrow": "Offline-first · Local data · Optional online OCR",
    "hero.title":
      "Know <em>how long your money lasts</em> — no cold spreadsheets.",
    "hero.desc":
      'Bareuang answers one key question: <strong>"with my current spending, how long will my money last?"</strong> — via Financial Runway, multi-wallet, and budgets that keep spending sane.',
    "hero.download": "Download",
    "hero.viewFeatures": "View features",
    "hero.note": "Free · No ads · Data stays on your phone",
    "hero.badge1": "Online OCR, with consent",
    "hero.badge2": "Backup .json offline",
    "hero.badge3": "Cozy, not stiff",
    "hero.floatTitle": "Runway: 47 days left",
    "hero.floatSub": "Burn rate Rp 68,400 / day · Est. out Feb 12",
    "trust.ocr": "Optional AI receipt scan",
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
      "Snap a receipt → Google Gemini through Bareuang's proxy (internet and consent required). Edit the result before saving locally.",
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
      "Core financial data stays in the local Room database. Receipt scan is optional and online: after consent, the photo is sent through Bareuang's proxy to Google Gemini for extraction. Manual entry and CSV import remain available without scanning.",
    "faq.q2": "Why set budget before logging transactions?",
    "faq.a2":
      "That's the Budget Gate — so Financial Runway has an accurate baseline. Without budget, the survival estimate can't be computed correctly.",
    "faq.q3": "Can I import BCA / GoPay / OVO statements?",
    "faq.a3":
      "Yes via CSV import. Supports comma/semicolon, split debit-credit, 8 date formats, and auto dedup by date + amount + merchant.",
    "faq.q4": "Does receipt scan need internet?",
    "faq.a4":
      "Yes. Receipt scan needs internet and consent because the photo is sent to Bareuang's proxy and Google Gemini. You can edit the result before saving it locally.",
    "faq.q5": "Does Bareuang give investment advice?",
    "faq.a5":
      "No. Bareuang is a logging & estimation tool, not a financial advisor — see Disclaimer in Terms.",
    "faq.q6": "How to delete all data?",
    "faq.a6":
      "Settings → Clear Data, or Android Settings → Apps → Bareuang → Clear Data, or uninstall. Offline means permanently gone.",
    "cta.title": "Ready to know when your money runs out — before it does?",
    "cta.sub": "Download Bareuang. Local data, optional online OCR, and honest numbers.",
    "cta.play": "Download",
    "footer.desc":
      "Your cozy money companion. Core data stays local; receipt scanning is optional online OCR with consent. Made with ❤️ in Indonesia.",
    "footer.product": "Product",
    "footer.features": "Features",
    "footer.screenshots": "Screenshots",
    "footer.lang": "Language: Indonesia · English",
  },
};
