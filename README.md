<div align="center">
  <img src="art/app_logo_new.png" width="110" height="110" alt="Bareuang Logo" style="border-radius: 28px;" />

  # Bareuang
  **Teman cozy buat uangmu · Your cozy money companion**

  *Tahu sampai kapan uangmu tahan, kelola banyak dompet, pantau tagihan, dan wujudkan target tabungan — bersama si beruang madu.*

  ---

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Compose](https://img.shields.io/badge/Jetpack_Compose-UI-845400?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![100% Offline](https://img.shields.io/badge/100%25_Offline-No_Internet-34A853?style=flat-square&logo=android&logoColor=white)]()
  [![License MIT](https://img.shields.io/badge/License-MIT-F4A216?style=flat-square)]()

</div>

---

## Mengapa Bareuang?

Kebanyakan aplikasi keuangan terasa seperti spreadsheet — dingin dan membebani. Bareuang hadir untuk membalikkan itu.

Satu pertanyaan sederhana jadi fondasinya: **"Dengan pola pengeluaranku sekarang, sampai kapan uangku tahan?"** — dan beruang madu akan menjawabnya lewat fitur **Financial Runway**.

Semua data **100% tersimpan lokal**, tanpa server, tanpa akun, tanpa izin internet.

---

## ✨ Fitur

| | Fitur | Deskripsi singkat |
|---|---|---|
| 📊 | **Financial Runway** | Hitung *burn rate* harian & prediksi kapan saldo habis (*Estimated Death Day*) |
| 💰 | **Multi-Wallet** | Kelola Tunai, BCA, GoPay, OVO, dll — saldo *net worth* real-time |
| 🔄 | **Transfer Antar Dompet** | Smart switch + 1-tap swap, akses cepat dari nav bar |
| 🎯 | **Savings Goals** | Target tabungan dengan kalkulator cerdas, deposit & withdraw terintegrasi dompet |
| 📋 | **Bill Reminder** | Tagihan rutin + notifikasi jatuh tempo H-3, auto-rollover, refund otomatis jika dibatalkan |
| 🤝 | **Split Bill** | Hitung patungan dengan pajak & service charge, share ke WhatsApp 1 klik |
| 📦 | **Backup & Restore** | Ekspor/impor semua data ke file `.json` lokal |
| 🌐 | **Bilingual** | Ganti bahasa Indonesia ↔ English tanpa restart (*zero-blink*) |
| 📈 | **Analytics** | Breakdown pengeluaran per kategori & dompet |
| 🏠 | **Home Widget** | Widget beruang di homescreen: runway, saldo, tagihan hari ini |

> **Budget Gate** — pencatatan transaksi baru aktif setelah budget bulan berjalan di-set. Ini memastikan Financial Runway selalu punya baseline yang valid.

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
app/
├── domain/        # Pure Kotlin — entities, repository ports, use-cases
├── data/          # Room DB, Backup JSON, WorkManager notifications
├── presentation/  # Jetpack Compose UI, ViewModels, Hilt Navigation
└── app/           # Composition root, Application entry
```

**Stack:** Kotlin 2.0 · Jetpack Compose · Room · Hilt · WorkManager · Glance Widget · Gson

---

## 🚀 Menjalankan Project

Tidak perlu konfigurasi — tanpa API key, tanpa `google-services.json`, tanpa server.

```bash
# Debug
./gradlew installDebug

# Release APK (butuh keystore.properties)
./gradlew :app:assembleRelease

# AAB untuk Play Store
./gradlew :app:bundleRelease
```

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
