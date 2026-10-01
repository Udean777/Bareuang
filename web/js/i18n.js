"use strict";
const dict = {
  id: {
    "nav.skip": "Lewati ke konten",
    "nav.features": "Fitur",
    "nav.how": "Cara kerja",
    "nav.screenshots": "Tangkapan Layar",
    "nav.faq": "FAQ",
    "nav.privacy": "Kebijakan Privasi",
    "nav.terms": "Ketentuan Layanan",
    "nav.download": "Unduh",
    "aria.primary": "Navigasi utama",
    "aria.language": "Bahasa",
    "aria.menu": "Menu",
    "brand.logo": "Logo Bareuang",
    "hero.eyebrow": "Utamakan offline · Data lokal · OCR pada build debug",
    "hero.title":
      "Tahu sampai <em>kapan uangmu tahan</em>, tanpa spreadsheet dingin.",
    "hero.desc":
      'Bareuang menjawab satu pertanyaan penting: <strong>"dengan pola pengeluaranku sekarang, sampai kapan uangku tahan?"</strong> - lewat perkiraan daya tahan keuangan, banyak dompet, dan anggaran yang membantu menjaga pengeluaran.',
    "hero.download": "Unduh",
    "hero.viewFeatures": "Lihat fitur",
    "hero.note": "Gratis · Tidak ada iklan · Data tetap di HP kamu",
    "hero.badge1": "OCR lokal pada build debug",
    "hero.badge2": "Cadangan .json offline",
    "hero.badge3": "Cozy, bukan kaku",
    "hero.imageAlt": "Pratinjau dashboard dan runway Bareuang",
    "hero.floatTitle": "Daya tahan: 47 hari lagi",
    "hero.floatSub": "Laju pengeluaran Rp 68.400 / hari · Perkiraan habis 12 Feb",
    "trust.ocr": "Pindai struk lokal pada build debug",
    "trust.budget": "Atur anggaran terlebih dahulu",
    "trust.csv": "Impor mutasi BCA/e-wallet",
    "trust.currency": "IDR / USD",
    "trust.widget": "Widget layar utama",
    "trust.note":
      "Transaksi baru dapat dicatat setelah anggaran bulanan diatur agar perkiraan daya tahan tetap akurat.",
    "features.kicker": "Fitur praktis",
    "features.title": "Semua yang kamu butuh, tanpa yang bikin pusing",
    "features.sub":
      "Dirancang untuk pemakaian harian - cepat, lokal, dan tidak menggurui.",
    "features.c1t": "Daya Tahan Keuangan",
    "features.c1d":
      "Hitung laju pengeluaran harian dan perkirakan kapan saldo habis. Ketahui lebih awal kapan perlu menghemat.",
    "features.c2t": "Anggaran Bulanan & Kategori",
    "features.c2d":
      "Atur anggaran bulanan dan batas per kategori (makanan, transportasi, dan lainnya). Pengeluaran yang melewati batas langsung terlihat.",
    "features.c3t": "Multi-Dompet",
    "features.c3d":
      "Tunai, BCA, GoPay, OVO - total kekayaan bersih dari semua dompet terlihat dalam satu layar.",
    "features.c4t": "Transfer Antar Dompet",
    "features.c4d":
      "Pindahkan uang antar dompet tanpa mencatat transaksi dua kali.",
    "features.c5t": "Impor Mutasi CSV",
    "features.c5d":
      "Impor mutasi BCA/e-wallet (pemisah koma atau titik koma, 8 format tanggal, duplikasi terdeteksi otomatis). Batas file 5 MB.",
    "features.c6t": "Scan Struk (OCR)",
    "features.c6d":
      "Foto struk diproses ML Kit di perangkat pada build debug. Pratinjau dan edit hasilnya sebelum disimpan secara lokal.",
    "features.c7t": "Target Tabungan",
    "features.c7d":
      "Atur target tabungan dan hitung setoran atau penarikan. Kemajuan terlihat jelas.",
    "features.c8t": "Pengingat Tagihan",
    "features.c8d":
      "Tagihan berulang, notifikasi 3 hari sebelumnya, perpanjangan otomatis, dan pengembalian saldo jika pembayaran dibatalkan.",
    "features.c9t": "Bagi Tagihan",
    "features.c9d":
      "Bagi tagihan makan atau belanja, termasuk pajak dan biaya layanan, lalu kirim ke WhatsApp sekali ketuk.",
    "how.kicker": "Cara kerja",
    "how.title": "3 langkah, langsung jalan",
    "how.s1t": "Atur Budget Bulanan",
    "how.s1d":
      "Atur batas bulanan dan per kategori. Anggaran ini menjadi acuan perkiraan daya tahan dan pencatatan transaksi.",
    "how.s2t": "Catat / Impor / Scan",
    "how.s2d":
      "Catat manual, impor CSV BCA/e-wallet, atau foto struk. Saldo diperiksa dan transaksi ganda dideteksi.",
    "how.s3t": "Pantau Runway",
    "how.s3d":
      "Lihat berapa lama uangmu bertahan, tren arus kas, dan pembagian pengeluaran.",
    "shots.kicker": "Tampilan",
    "shots.title": "Nyaman dilihat, angka mudah dipahami",
    "shots.sub":
      "Geser untuk melihat Dasbor, Anggaran, Tagihan, Target, Transfer, dan Analitik.",
    "shots.altDashboard": "Dashboard dan runway",
    "shots.altBudget": "Anggaran bulanan",
    "shots.altBills": "Tagihan dan komitmen",
    "shots.altGoals": "Target tabungan",
    "shots.altTransfer": "Transfer antar dompet",
    "shots.altAnalytics": "Analitik keuangan",
    "shots.captionDashboard": "Dashboard & Runway",
    "shots.captionBudget": "Anggaran Bulanan",
    "shots.captionBills": "Tagihan & Komitmen",
    "shots.captionGoals": "Target Tabungan",
    "shots.captionTransfer": "Transfer Antar Dompet",
    "shots.captionAnalytics": "Analitik Keuangan",
    "faq.title": "Yang sering ditanya",
    "faq.skip": "FAQ",
    "faq.q1": "Apakah data saya dikirim ke server?",
    "faq.a1":
      "Data keuangan utama tersimpan di Room DB lokal. Pada build debug, foto struk diproses ML Kit di perangkat dan tidak dikirim ke server. Input manual dan import CSV tetap bisa dipakai tanpa scan.",
    "faq.q2": "Kenapa harus atur budget dulu sebelum catat transaksi?",
    "faq.a2":
      'Ini "Budget Gate" - biar Financial Runway punya acuan yang akurat. Tanpa budget, estimasi hari bertahan tidak bisa dihitung dengan benar.',
    "faq.q3": "Apakah bisa impor mutasi BCA / GoPay / OVO?",
    "faq.a3":
      "Bisa via Import CSV. Mendukung delimiter koma/semicolon, debit-kredit terpisah, 8 format tanggal, dan dedup otomatis berdasarkan tanggal + nominal + merchant.",
    "faq.q4": "Scan struk butuh internet?",
    "faq.a4":
      "Pada build debug, tidak. Scan struk memakai ML Kit di perangkat dan dapat diproses tanpa internet. Build release menonaktifkan OCR.",
    "faq.q5": "Apakah Bareuang memberi saran investasi?",
    "faq.a5":
      "Tidak. Bareuang hanya alat pencatatan & estimasi. Bukan penasihat keuangan - lihat Disclaimer di Terms.",
    "faq.q6": "Bagaimana cara hapus semua data?",
    "faq.a6":
      "Pengaturan → Hapus Data, atau hapus langsung via Settings Android → Apps → Bareuang → Clear Data, atau uninstall. Karena offline, data hilang permanen.",
    "cta.title": "Siap tahu kapan uangmu habis - sebelum benar-benar habis?",
    "cta.sub": "Download Bareuang. Data lokal, OCR debug, dan jujur soal angka.",
    "cta.play": "Unduh",
    "footer.desc":
      "Teman cozy buat uangmu. Data utama lokal; Scan Struk diproses lokal di debug. Dibuat dengan ❤️ di Indonesia.",
    "footer.contact": "Kontak:",
    "footer.legal": "Legal",
    "footer.privacy": "Kebijakan Privasi",
    "footer.terms": "Ketentuan Layanan",
    "footer.cookies": "Kebijakan Cookie",
    "footer.disclaimer": "Pernyataan Penyangkalan",
    "footer.product": "Produk",
    "footer.features": "Fitur",
    "footer.screenshots": "Tangkapan layar",
    "footer.faq": "FAQ",
    "footer.githubLicense": "GitHub & Lisensi (MIT)",
    "footer.license": "Lisensi MIT",
    "footer.lang": "Bahasa: Indonesia · Inggris",
  },
  en: {
    "nav.skip": "Skip to content",
    "nav.features": "Features",
    "nav.how": "How it works",
    "nav.screenshots": "Screenshots",
    "nav.faq": "FAQ",
    "nav.privacy": "Privacy Policy",
    "nav.terms": "Terms of Service",
    "nav.download": "Download",
    "aria.primary": "Primary navigation",
    "aria.language": "Language",
    "aria.menu": "Menu",
    "brand.logo": "Bareuang logo",
    "hero.eyebrow": "Offline-first · Local data · OCR in debug builds",
    "hero.title":
      "Know <em>how long your money lasts</em> - no cold spreadsheets.",
    "hero.desc":
      'Bareuang answers one key question: <strong>"with my current spending, how long will my money last?"</strong> - via Financial Runway, multi-wallet, and budgets that keep spending sane.',
    "hero.download": "Download",
    "hero.viewFeatures": "View features",
    "hero.note": "Free · No ads · Data stays on your phone",
    "hero.badge1": "Local OCR in debug",
    "hero.badge2": "Backup .json offline",
    "hero.badge3": "Cozy, not stiff",
    "hero.imageAlt": "Bareuang dashboard and runway preview",
    "hero.floatTitle": "Runway: 47 days left",
    "hero.floatSub": "Burn rate Rp 68,400 / day · Est. out Feb 12",
    "trust.ocr": "Local receipt scan in debug",
    "trust.budget": "Budget Gate",
    "trust.csv": "BCA/e-wallet CSV import",
    "trust.currency": "IDR / USD",
    "trust.widget": "Home screen widget",
    "trust.note":
      "New transactions unlock after setting monthly budget - so runway stays accurate.",
    "features.kicker": "Cozy features",
    "features.title": "Everything you need, nothing that nags",
    "features.sub": "Built for daily use - fast, local, and honest.",
    "features.c1t": "Financial Runway",
    "features.c1d":
      "Daily burn rate & Estimated Death Day. Know early when to slow down.",
    "features.c2t": "Monthly & Category Budget",
    "features.c2d":
      "Lock monthly budget + per-category limits (Food, Transport, etc). Over limit? Instantly visible.",
    "features.c3t": "Multi-Wallet",
    "features.c3d":
      "Cash, BCA, GoPay, OVO - real-time net worth in one screen.",
    "features.c4t": "Wallet Transfer",
    "features.c4d":
      "Smart anti-duplicate switch + 1-tap swap. Move money without double entries.",
    "features.c5t": "CSV Import",
    "features.c5d":
      "Import BCA/e-wallet (comma/semicolon, 8 date formats, auto dedup). 5MB guard.",
    "features.c6t": "Receipt Scan (OCR)",
    "features.c6d":
      "Receipt photos use on-device ML Kit in debug builds. Preview and edit the result before saving locally.",
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
      "Set monthly + per-category limits - the baseline for runway & guards.",
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
    "shots.altDashboard": "Dashboard and runway",
    "shots.altBudget": "Monthly budget",
    "shots.altBills": "Bills and commitments",
    "shots.altGoals": "Savings goals",
    "shots.altTransfer": "Wallet transfer",
    "shots.altAnalytics": "Financial analytics",
    "shots.captionDashboard": "Dashboard & Runway",
    "shots.captionBudget": "Monthly Budget",
    "shots.captionBills": "Bills & Commitments",
    "shots.captionGoals": "Savings Goals",
    "shots.captionTransfer": "Wallet Transfer",
    "shots.captionAnalytics": "Financial Analytics",
    "faq.title": "Frequently asked",
    "faq.skip": "FAQ",
    "faq.q1": "Is my data sent to a server?",
    "faq.a1":
      "Core financial data stays in the local Room database. In debug builds, receipt photos are processed on-device with ML Kit and are not sent to a server. Manual entry and CSV import remain available without scanning.",
    "faq.q2": "Why set budget before logging transactions?",
    "faq.a2":
      "That's the Budget Gate - so Financial Runway has an accurate baseline. Without budget, the survival estimate can't be computed correctly.",
    "faq.q3": "Can I import BCA / GoPay / OVO statements?",
    "faq.a3":
      "Yes via CSV import. Supports comma/semicolon, split debit-credit, 8 date formats, and auto dedup by date + amount + merchant.",
    "faq.q4": "Does receipt scan need internet?",
    "faq.a4":
      "Not in debug builds. Receipt scan uses on-device ML Kit and can run without internet. Release builds disable OCR.",
    "faq.q5": "Does Bareuang give investment advice?",
    "faq.a5":
      "No. Bareuang is a logging & estimation tool, not a financial advisor - see Disclaimer in Terms.",
    "faq.q6": "How to delete all data?",
    "faq.a6":
      "Settings → Clear Data, or Android Settings → Apps → Bareuang → Clear Data, or uninstall. Offline means permanently gone.",
    "cta.title": "Ready to know when your money runs out - before it does?",
    "cta.sub": "Download Bareuang. Local data, debug-only OCR, and honest numbers.",
    "cta.play": "Download",
    "footer.desc":
      "Your cozy money companion. Core data stays local; receipt scanning runs locally in debug builds. Made with ❤️ in Indonesia.",
    "footer.contact": "Contact:",
    "footer.legal": "Legal",
    "footer.privacy": "Privacy Policy",
    "footer.terms": "Terms of Service",
    "footer.cookies": "Cookie Policy",
    "footer.disclaimer": "Disclaimer",
    "footer.product": "Product",
    "footer.features": "Features",
    "footer.screenshots": "Screenshots",
    "footer.faq": "FAQ",
    "footer.githubLicense": "GitHub & License (MIT)",
    "footer.license": "MIT License",
    "footer.lang": "Language: Indonesia · English",
  },
};
