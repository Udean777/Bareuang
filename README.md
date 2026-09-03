<div align="center">
  <img src="art/app_logo_new.png" width="110" height="110" alt="Bareuang Logo" style="border-radius: 28px;" />

  # Bareuang
  **Teman cozy buat uangmu · Your cozy money companion**

  *Tahu sampai kapan uangmu tahan, kelola banyak dompet, pantau tagihan, dan wujudkan target tabungan — bersama si beruang madu.*

  ---

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Compose](https://img.shields.io/badge/Jetpack_Compose-UI-845400?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Offline-first](https://img.shields.io/badge/Offline--first-Local%20data-34A853?style=flat-square&logo=android&logoColor=white)]()
  [![License MIT](https://img.shields.io/badge/License-MIT-F4A216?style=flat-square)]()

</div>

---

## Mengapa Bareuang?

Kebanyakan aplikasi keuangan terasa seperti spreadsheet — dingin dan membebani. Bareuang hadir untuk membalikkan itu.

Satu pertanyaan sederhana jadi fondasinya: **"Dengan pola pengeluaranku sekarang, sampai kapan uangku tahan?"** — dan beruang madu akan menjawabnya lewat fitur **Financial Runway**.

Data keuangan utama **tersimpan lokal** di perangkat. Fitur Scan Struk bersifat opsional
dan mengirim foto ke proxy Bareuang lalu Google Gemini untuk ekstraksi, setelah consent.
Bareuang tidak memiliki akun, login, cloud sync, atau profil multi-user: aplikasi ini
sepenuhnya **guest-only**. Reset Data di Pengaturan menghapus data lokal aplikasi.

### Privasi OCR

- Foto struk hanya dikirim setelah consent eksplisit dan koneksi internet tersedia.
- Proxy meneruskan gambar terkompresi ke Google Gemini dan mengembalikan draft transaksi.
- Bareuang tidak menyimpan gambar, base64, isi struk, merchant, atau item pada log.
- Metadata operasional proxy memiliki retensi maksimum 14 hari.
- Input manual selalu tersedia jika pengguna tidak menyetujui OCR atau sedang offline.
- Kebijakan lengkap: [Privacy Policy](https://bareuang.app/privacy.html).

---

## ✨ Fitur

| | Fitur | Deskripsi singkat |
|---|---|---|
| 📊 | **Financial Runway** | Hitung *burn rate* harian & prediksi kapan saldo habis (*Estimated Death Day*) |
| 🏷️ | **Monthly & Category Budget** | Kunci anggaran bulanan + atur limit per kategori pos pengeluaran (*Food, Transport, dll*) |
| 💰 | **Multi-Wallet** | Kelola Tunai, BCA, GoPay, OVO, dll — kalkulasi total *net worth* real-time |
| 🔄 | **Transfer Antar Dompet** | Smart switch anti-duplikasi + 1-tap swap dompet dari bar navigasi cepat |
| 📥 | **Import Mutasi CSV** | Impor transaksi dari BCA / e-wallet (delimiter `,`/`;`, debit-kredit terpisah, 8 format tanggal, dedup, guard saldo & budget, index DB) |
| 🧾 | **Scan Struk Belanja (OCR)** | Foto struk → AI Gemini via proxy (butuh internet dan consent), preview kertas termal, edit merchant/total/category, currency `Rp` real-time |
| 🎯 | **Savings Goals** | Target tabungan dengan kalkulator nominal cerdas, alokasi setor (*deposit*) & tarik (*withdraw*) |
| 📋 | **Bill Reminder** | Pengingat tagihan rutin, notifikasi jatuh tempo H-3, auto-rollover, & auto-refund jika batal bayar |
| 🤝 | **Split Bill** | Hitung patungan makan/belanja bareng teman (pajak & service charge) + 1-klik share ke WhatsApp |
| 💱 | **Currency (IDR / USD)** | Pilih mata uang utama (Rupiah / Dollar) sejak Onboarding — dapat diubah kapan saja di Pengaturan, format `Rp`/`$` konsisten di seluruh input |
| 🌓 | **Theme Mode** | Pilihan tema Terang, Gelap, atau Ikuti Sistem — dapat disetel sejak Onboarding |
| 📦 | **Backup & Restore** | Cadangkan dan pulihkan seluruh data keuangan secara offline via file `.json` |
| 🌐 | **Bilingual (ID / EN)** | Pilihan Bahasa Indonesia & English yang berganti seketika tanpa jeda (*zero-blink*) |
| 📈 | **Financial Analytics** | Visualisasi tren *Cashflow*, riwayat *Net Worth*, dan distribusi pengeluaran per kategori |
| 🏠 | **Home Widget** | Widget beruang interaktif di layar utama: pantau sisa runway, saldo, & tagihan harian |

> **Budget Gate** — pencatatan transaksi baru aktif setelah budget bulan berjalan diatur. Hal ini memastikan Financial Runway dan estimasi hari bertahan selalu memiliki data acuan yang akurat. Import CSV & Scan Struk juga melewati gate + cek saldo (fail-fast) via `BulkCreateTransactionsUseCase` dan `OcrScanViewModel`.

**Import offline, OCR online opsional:** CSV `5MB` guard + `DocumentFile` name, `parseWithStats` + `getByDates` dedup, bulk insert 1 transaksi DB (`bulkCreate`), `ImportPreferences` counter. Scan struk membutuhkan internet dan consent; gambar dikirim melalui proxy Bareuang ke Google Gemini, lalu hasilnya dapat diedit sebelum disimpan lokal.

---

## 📸 Screenshots

<div align="center">

<table>
  <tr>
    <td align="center"><img src="art/screenshots/Dashboard.png" width="220" alt="Dashboard & Financial Runway" /></td>
    <td align="center"><img src="art/screenshots/Budget.png" width="220" alt="Monthly Budget" /></td>
    <td align="center"><img src="art/screenshots/Due-Bills.png" width="220" alt="Bills & Commitments" /></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Dashboard & Financial Runway</b></sub></td>
    <td align="center"><sub><b>Monthly Budget</b></sub></td>
    <td align="center"><sub><b>Bills & Commitments</b></sub></td>
  </tr>
  <tr>
    <td align="center"><img src="art/screenshots/Goals.png" width="220" alt="Savings Goals" /></td>
    <td align="center"><img src="art/screenshots/Transfer.png" width="220" alt="Wallet Transfer" /></td>
    <td align="center"><img src="art/screenshots/Analytics.png" width="220" alt="Financial Analytics" /></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Savings Goals</b></sub></td>
    <td align="center"><sub><b>Wallet Transfer</b></sub></td>
    <td align="center"><sub><b>Financial Analytics</b></sub></td>
  </tr>
</table>

</div>

---

## 🏛️ Arsitektur

```
Bareuang/
├── app/           # Composition root, Application entry
├── domain/        # Pure Kotlin — entities, repository ports, use-cases
├── data/          # Room DB, Backup JSON, WorkManager notifications
├── presentation/  # Jetpack Compose UI, ViewModels, Hilt Navigation
└── web/           # Landing page + Privacy/Terms (static, no build)
    ├── index.html      # Landing 1 halaman (ID/EN, responsive, SEO)
    ├── privacy.html    # Privacy Policy — local data + optional online OCR
    ├── terms.html      # Terms of Service + Disclaimer
    ├── css/style.css   # Single stylesheet, no framework
    ├── js/main.js      # ~30 lines + i18n dict
    └── assets/         # Logo & screenshots (reuse dari art/)
```

**Stack Android:** Kotlin 2.0 · Jetpack Compose · Room v16 (migration historis dan guest-only schema) · Hilt · WorkManager · Glance Widget · Gson · Gemini receipt proxy

**Stack Web:** Pure HTML/CSS/JS — tanpa framework, tanpa build step, tanpa `node_modules`. Deploy ke GitHub Pages / Cloudflare Pages. SEO: canonical, hreflang ID/EN, OG/Twitter, JSON-LD (SoftwareApplication, FAQPage, Organization, Breadcrumb), sitemap.xml, robots.txt.

---

## 🚀 Menjalankan Project

Fitur inti tidak memerlukan akun. Build aplikasi memakai konfigurasi lokal; endpoint OCR produksi membutuhkan konfigurasi server Gemini dan koneksi internet.

```bash
# Debug
./gradlew installDebug

# Release APK (butuh keystore.properties)
./gradlew :app:assembleRelease

# AAB untuk Play Store
./gradlew :app:bundleRelease

# Validasi lokal
./gradlew test
./gradlew lint
./gradlew :data:compileDebugAndroidTestKotlin
```

`connectedDebugAndroidTest` membutuhkan emulator atau perangkat Android aktif. Migration
Room dan perilaku widget/notification harus diuji pada perangkat sebelum release.

### 🌐 Web — Landing Page

```bash
# Preview lokal (tanpa build)
python3 -m http.server --directory web 8000
# buka http://localhost:8000

# Struktur
# web/index.html    → landing 1 halaman (bilingual ID/EN toggle, responsive, smooth reveal)
# web/privacy.html  → Privacy Policy (local data + consent-gated online OCR)
# web/terms.html    → Terms + Disclaimer keuangan
# web/sitemap.xml + robots.txt → SEO
```

Deploy: push `web/` ke GitHub Pages (Settings → Pages → Deploy from `/web`) atau connect repo ke Cloudflare Pages (root `web`). Ganti `https://bareuang.app` di `web/index.html`, `privacy.html`, `terms.html`, `sitemap.xml` jika pakai domain lain. URL Privacy/Terms dipakai di Play Console → Data safety & Store listing.

Release APK dipublikasikan dari GitHub Releases resmi `Udean777/Bareuang`. Setiap release menyertakan `SHA256SUMS.txt`; verifikasi dengan `sha256sum -c SHA256SUMS.txt`. Fingerprint sertifikat signing diambil dari keystore produksi dengan `keytool -list -v -keystore <keystore>` dan dicatat di Play Console/secret manager, bukan di repository.

<details>
<summary>Setup keystore untuk release build</summary>

1. Generate keystore:
   ```bash
   keytool -genkey -v -keystore keystores/bareuang-release.keystore \
     -alias bareuang -keyalg RSA -keysize 4096 -validity 10000
   ```
2. Buat `keystore.properties` di root (sudah di-gitignore):
   ```properties
   storeFile=keystores/bareuang-release.keystore
   storePassword=****
   keyAlias=bareuang
   keyPassword=****
   ```

Untuk CI/CD GitHub Actions, tambahkan 4 secret: `SIGNING_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

</details>

---

## 📄 Lisensi

MIT License — lihat file [`LICENSE`](LICENSE) untuk detail.

<div align="center">
  <sub>Dibuat dengan ❤️ oleh <a href="https://github.com/Udean777">Udean777</a></sub>
</div>
