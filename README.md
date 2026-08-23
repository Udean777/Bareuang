<div align="center">
  <img src="art/app_logo.png" width="120" height="120" alt="BareBudget Logo" style="border-radius: 24px;" />
  
  # BareBudget
  
  **Aplikasi Pencatat Keuangan & Pelacak Survival Runway Tanpa Ribet**
  
  *Atur pengeluaran bulanan, ketahui batas bertahan hidup finansialmu (Runway / Death Day), kelola banyak dompet, dan wujudkan target tabungan.*

  ---

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Material Design 3](https://img.shields.io/badge/UI-Material%20Design%203-7B1FA2?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)
  [![Go Fiber](https://img.shields.io/badge/Backend-Go%20Fiber-00ADD8?style=for-the-badge&logo=go&logoColor=white)](https://gofiber.io/)
  [![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
  [![Room DB](https://img.shields.io/badge/Local%20Cache-Room%20SQLite-3DDC84?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
  [![Firebase Auth](https://img.shields.io/badge/Auth-Firebase%20%26%20Google-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)

</div>

---

## 📖 Tentang BareBudget

**BareBudget** adalah aplikasi pencatat keuangan (*personal finance*) yang minimalis, gesit, dan modern. Aplikasi ini dirancang khusus untuk menjawab satu pertanyaan penting yang sering muncul di pertengahan bulan:

> *"Dengan pola jajan dan pengeluaran kayak sekarang, sisa uangku bakal cukup sampai tanggal berapa ya?"*

Antarmuka aplikasi dibangun **100% dengan Material Design 3 (M3)** menggunakan Jetpack Compose, menghadirkan tampilan yang bersih, adaptif, serta mendukung penuh tema dinamis (*Material You*).

Mengusung pendekatan **Offline-First**, BareBudget bisa langsung dipakai tanpa perlu login (**Guest Mode**). Kamu juga bisa menghubungkannya ke **Akun Google** kapan saja untuk sinkronisasi ke cloud tanpa khawatir kehilangan riwayat data transaksi sebelumnya.

---

## ✨ Fitur Unggulan

### 1. 🧮 Financial Runway & "Estimated Death Day"
* **Kalkulasi Laju Pengeluaran (*Burn Rate*)**: Pantau rata-rata pengeluaran harian secara otomatis.
* **Prediksi Tanggal Habis Saldo**: Ketahui perkiraan pasti di tanggal berapa anggaran bulan ini bakal habis.
* **Indikator Kesehatan Finansial**: Status kondisi keuangan terpantau langsung (*Aman*, *Waspada*, atau *Kritis*).
* **Monthly Budget Lock & Audit**: Anggaran bulanan dikunci (hanya disetel 1x di awal bulan) agar kamu tetap disiplin dan riwayat pengeluaran tetap rapi (khusus *expenses*, terpisah dari pos tagihan/bills).
* **Budget Gate**: Fitur pencatatan pemasukan dan pengeluaran baru aktif setelah anggaran bulan berjalan diatur (transfer antar dompet tetap bisa dilakukan kapan saja). Tersedia banner pengingat dan pintasan langsung ke menu anggaran.

### 2. 🔄 Transfer Antar Dompet, Smart Switch & Arus Kas Real-Time
* **Pencatatan Lengkap**: Kategori transaksi mencakup **Pemasukan (Income)**, **Pengeluaran (Expense)**, dan **Transfer Antar Dompet**.
* **Akses Cepat Menu Transfer**: Tab navigasi khusus di tengah *Bottom Bar* untuk transfer instan antar dompet (Tunai, BCA, Mandiri, GoPay, OVO, ShopeePay, dll).
* **Fitur Smart Switch & 1-Tap Wallet Swap**: Sistem otomatis mencegah pemilihan dompet asal dan tujuan yang sama, dilengkapi tombol *swap* 1-ketuk.
* **Manajemen Dompet Fleksibel**: Tambah, pantau saldo, ubah nama/warna, hingga hapus dompet. Dompet default *"Uang Tunai"* langsung siap pakai sejak awal.
* **Update Kekayaan Bersih (*Net Worth*) Real-Time**: Perhitungan total saldo selalu terbarui otomatis di semua layar tanpa perlu refresh manual.

### 3. 🎯 Target Tabungan Cerdas (*Savings Goals & Pockets*)
* Buat pos tabungan impian dengan mudah (Dana Darurat, Liburan, Gadget, Kendaraan, dll).
* **Terintegrasi Langsung dengan Dompet (Deposit & Withdraw)**:
  * **Setor (Deposit)**: Memotong saldo dari dompet pilihan dan mencatatnya sebagai alokasi tabungan.
  * **Tarik (Withdraw)**: Mengurangi saldo tabungan dan mengembalikannya ke dompet yang dipilih.
* **Kalkulator Cerdas & Pilihan Warna**: Rekomendasi nominal yang perlu ditabung per hari atau per bulan agar target tercapai tepat waktu, lengkap dengan pemilih warna kartu.
* **Visualisasi Progres Interaktif**: Progress bar lengkap dengan persentase dan label status (*Tercapai 100%*, *Mendekati Deadline*, *On Track*).

### 4. 📋 Pengingat Tagihan, Langganan & Sistem Refund
* Pantau dan catat semua tagihan rutin (WiFi, Kos, Listrik, Langganan Streaming, PayLater: Shopee, Kredivo, GoPay, atau custom).
* **Pilihan Penyedia & Ikon Kustom**: Pilihan layanan populer berlogo resmi serta opsi upload ikon sendiri yang tersimpan secara lokal.
* **Auto-Rollover**: Begitu tagihan ditandai **Lunas (PAID)**, sistem otomatis menjadwalkan ulang tagihan untuk periode berikutnya (*Weekly, Monthly, Yearly*).
* **Refund Otomatis Saat Batal Bayar**: Jika status tagihan lunas diubah kembali ke **Belum Lunas**, saldo dompet yang terpotong sebelumnya akan langsung dikembalikan (*refund*) dan dicatat sebagai penyesuaian tanpa mengacaukan audit keuangan.

### 5. ⚡ Quick Action Bottom Sheet & Dialog Responsif
* Ketuk kartu Tagihan atau Target Tabungan untuk membuka **Quick Action Bottom Sheet** yang ringkas dan informatif.
* Tampilan rincian yang rapi dengan tombol aksi yang nyaman ditekan serta penjelasan yang jelas.
* **Dialog Adaptif**: Tampilan input form dan pop-up tetap proporsional dan nyaman digunakan di berbagai ukuran layar.

### 6. 🌐 Pilihan Bahasa & Transisi Mulus (*In-App Locale Switching*)
* **Mendukung Dua Bahasa Penuh**: Tersedia dalam Bahasa Indonesia dan English di semua layar (Onboarding, Dashboard, Tagihan, Transfer, Target, Anggaran, Analitik, dan Pengaturan).
* **Selektor Kapsul di Onboarding**: Ganti bahasa favoritmu langsung sejak layar pertama onboarding.
* **Ganti Bahasa Tanpa Kedip (*Zero-Blink*)**: Menggunakan manajemen konfigurasi aplikasi modern berbasis `android:configChanges` sehingga bahasa berganti seketika (*in-place recomposition*) tanpa layar berkedip atau memuat ulang aplikasi.

### 7. 👥 Kalkulator Patungan (*Smart Split Bill*)
* Hitung bagi tagihan bareng teman langsung dari aplikasi.
* Mendukung pembagian rata (*Equal Split*) lengkap dengan hitungan pajak (PB1 10%) dan *service charge* (5%).
* **1-Click Share ke WhatsApp**: Format rincian patungan siap kirim langsung ke chat pribadi maupun grup.

### 8. 🎨 Desain Ekspresif Material 3 & Filter Fleksibel
* **Palet Dinamis MaterialKolor**: Tampilan warna modern dan hidup dengan kontras yang nyaman di mata.
* **Bentuk Kartu Khas (Asymmetric & Squircle)**: Kartu *Financial Runway* asimetris yang elegan, kartu dompet bergaya kartu debit fisik, dan kartu item *Squircle* dengan garis tepi yang presisi (`crispBorder 0.8.dp`).
* **Floating Pill Navigation Bar**: Menu navigasi bawah melayang berbentuk kapsul dengan animasi transisi yang luwes.
* **Pencarian & Filter Cepat**:
  - *Semua Transaksi*: Kolom pencarian instan + filter bottom sheet (Tipe Transaksi, Kategori, Dompet) dengan penyimpanan *draft*.
  - *Tagihan*: Kolom pencarian + tombol filter status (`Semua`, `Belum Lunas`, `Lunas`).
  - *Target Tabungan*: Kolom pencarian + tab filter status (`Semua`, `Aktif`, `Tercapai`).

### 9. 📦 Backup & Restore JSON Offline
* Ekspor seluruh data transaksi, dompet, tagihan, dan target tabungan ke file `.json` sebagai cadangan lokal.
* Pulihkan (*restore*) data kapan saja dengan cepat dan aman ke database lokal.

### 10. 🚀 Splash Screen Animasi, Onboarding 3D & Panduan Interaktif
* **Splash Screen Beranimasi**: Transisi *fade & spring scale* yang mulus dengan dukungan native *Android 12+ SplashScreen API*.
* **Onboarding Berilustrasi 3D**: Alur perkenalan fitur aplikasi yang ramah dan interaktif dengan visual semi-3D.
* **Panduan Interaktif 7 Langkah (Coach Marks)**: Memandu pengguna baru mengenal alur utama (*atur anggaran → siapkan dompet → catat transaksi → eksplor fitur*). Dilengkapi efek *spotlight*, tooltip penjelasan, indikator langkah, navigasi layar otomatis, serta dukungan bilingual. Bisa diulang kapan saja lewat menu Pengaturan.

### 11. 🔒 Clean Architecture & Sinkronisasi Cloud Offline-First
* **Multi-module Terisolasi Rapi**: `:domain` murni Kotlin (bebas dependensi Android/Room/Retrofit), `:data` Android library (Room/Retrofit/Firebase/WorkManager), `:presentation` antarmuka Compose & ViewModel (MaterialKolor, Compose Navigation, Hilt), serta `:app` sebagai composition root (`app → presentation → data → domain`).
* **Pemisahan Model DTO & Domain**: Lapisan DTO diatur terpisah dengan `Gson(LOWER_CASE_WITH_UNDERSCORES)` dan `@SerializedName`, dipetakan via `dto.toDomain()`. Perubahan domain tidak akan merusak kontrak API (diverifikasi via `ApiContractTest` MockWebServer).
* **Pola Outbox + WorkManager**: Sinkronisasi data lokal ke server menggunakan antrean *Room outbox* yang dieksekusi oleh `@HiltWorker` dan `WorkManager` (berjalan otomatis saat perangkat online dengan strategi exponential backoff).
* **Manajemen State & UiEffect Bersih**: Komunikasi one-shot event menggunakan `Channel<UiEffect>` (mencegah event terkirim ulang saat layar berotasi) dan proteksi tombol saat operasi sedang berjalan untuk mencegah klik ganda.
* **Keamanan & Isolasi Data Akun**: Autentikasi API memakai **Firebase ID Token asli** yang diverifikasi signature RS256-nya di backend terhadap sertifikat publik Google, mutasi saldo dompet dilindungi *row-level locking* (`SELECT ... FOR UPDATE`) dari race condition, pembersihan data lokal yang aman saat *sign out*, serta validasi migrasi data dari mode Guest ke akun Google tanpa risiko kehilangan data.

---

## 🏛️ Struktur Arsitektur & Tech Stack

```
BareBudget/
├── domain/                     # Pure Kotlin JVM — entities, repository ports, use-cases, AppTheme
│   └── src/main/java/com/ssajudn/barebudget/domain/
│       ├── model/              # Wallet, Transaction, Goal, DueBill, Budget, AppTheme, DomainModels
│       ├── repository/         # WalletRepository, TransactionRepository, GoalRepository, DueBillRepository, BudgetRepository
│       ├── usecase/            # GetDashboardSummary, GetCashflow/NetWorth, PayDueBill
│       └── error/              # AppException (typed)
├── data/                       # Android Library — Room, Retrofit, Firebase, WorkManager (→ :domain)
│   ├── src/main/java/com/ssajudn/barebudget/data/
│   │   ├── auth/               # AuthManager (Firebase), verifier
│   │   ├── datasource/local/   # LocalDataSource + withTransaction + ownerId
│   │   ├── datasource/remote/  # RemoteDataSource (DTO→domain mapping)
│   │   ├── local/room/         # Entities, Daos, AppDatabase v9 (outbox), OutboxEntity/Dao
│   │   ├── sync/               # OutboxWorker (@HiltWorker), OutboxScheduler (WorkManager)
│   │   ├── network/            # ApiClient, ApiService (DTO), dto/ (Wallet/Transaction/Goal/DueBillDto)
│   │   ├── repository/         # *RepositoryImpl (isGuestMode routing), DomainMappers, MigrationRepositoryImpl
│   │   ├── service/            # WalletBalanceService (single writer)
│   │   └── utils/              # AppConfig (BuildConfig data), DateUtils
│   └── schemas/                # Room schema 8.json, 9.json (outbox)
├── presentation/               # Android Library — Jetpack Compose UI, ViewModels, Navigation (→ :domain, :data)
│   └── src/main/java/com/ssajudn/barebudget/
│       ├── ui/
│       │   ├── analytics/      # Financial Breakdown & Category Charts
│       │   ├── bills/          # Due Bills, 1-Line Search, Segmented Filter & Refund System
│       │   ├── budget/         # Monthly Spending Target & Locked Budget UI
│       │   ├── components/     # Squircle Dialogs, Floating Pill Navigation Bar, StateViews
│       │   ├── dashboard/      # Asymmetric Financial Runway Card & Quick Actions
│       │   ├── goals/          # Savings Goals, Pockets, 1-Line Search & Smart Calculator
│       │   ├── navigation/     # AppNavigation & TopLevelDestinations
│       │   ├── onboarding/     # AuthScreen, 3D Illustrated Onboarding
│       │   ├── settings/       # Appearance (MaterialKolor Expressive), JSON Backup/Restore, Profile
│       │   ├── splash/         # Animated Branded Splash Screen
│       │   ├── theme/          # AppShapes (AsymmetricHero, Squircle, Pill), crispBorder, Theme.kt
│       │   ├── tour/           # TourScript, TourOverlay (spotlight coach marks), TourRegistry
│       │   ├── transaction/    # Add Expense, TransferScreen, AllTransactions (Search & Filter BottomSheet)
│       │   └── wallets/        # Physical Debit-Style Wallet Cards
│       └── utils/              # CurrencyFormatter, CurrencyVisualTransformation, DateUtils (UI)
├── app/                        # Android Application — Composition Root & Application Entry (→ :presentation)
│   └── src/main/java/com/ssajudn/barebudget/
│       └── BareBudgetApplication.kt # Hilt + HiltWorkerFactory (WorkManager)
└── server/                     # Go + Fiber + GORM + PostgreSQL
    ├── cmd/api/main.go         # Fiber + AuthMiddlewareWithVerifier + graceful shutdown
    ├── internal/
    │   ├── apperr/             # Typed errors (BadRequest/NotFound/Conflict) → mapping status HTTP
    │   ├── auth/               # TokenVerifier — verifikasi signature RS256 Firebase ID token vs sertifikat publik Google
    │   ├── config/             # ENV, CORS, IsProduction (fail-fast tanpa DATABASE_URL di produksi)
    │   ├── database/           # postgres.go AutoMigrate User/Wallet/Transaction/DueBill/Budget/Goal
    │   ├── handler/            # HTTP Handlers (interface Service consumer-side, error mapping terpusat)
    │   ├── i18n/               # Pesan runway & notes transaksi bilingual (en/id)
    │   ├── middleware/         # AuthMiddlewareWithVerifier (Bearer → verified UID), locale
    │   ├── models/             # GORM entities + typed patch (DueBillPatch/GoalPatch)
    │   ├── repository/         # Store interface tipis + Transactional (query atomik, row locking)
    │   └── service/            # Logika bisnis uang (saldo, bayar/refund tagihan, deposit goal)
    └── go.mod                  # go 1.25, Dockerfile golang:1.25-alpine
```

**Teknologi & Utilitas Modern:** Hilt Work (`hilt-work`), WorkManager (`work-runtime-ktx`), MockWebServer (`mockwebserver`), Gson `LOWER_CASE_WITH_UNDERSCORES`, Room `withTransaction`, `Channel<UiEffect>`.

---

## 🚀 Panduan Menjalankan Project

### 1. Menjalankan Backend Server (Go + PostgreSQL)

Pastikan kamu sudah menginstal **Go (versi 1.25 ke atas)** dan **PostgreSQL**.

```bash
# Masuk ke direktori server
cd server

# Salin contoh konfigurasi environment, lalu isi FIREBASE_PROJECT_ID
# (Firebase Console → Project Settings → General → Project ID)
cp .env.example .env

# Jalankan server API (default port: 8080)
go run cmd/api/main.go
```

> **Catatan Produksi**: Set `ENV=production` + `DATABASE_URL` (wajib, server *fail-fast* tanpa itu) + `FIREBASE_PROJECT_ID` (wajib — semua token diverifikasi signature-nya; tanpa ini semua request ditolak 401).

> **Tips Docker**: Kamu juga bisa menjalankan instance PostgreSQL secara praktis lewat Docker:
> ```bash
> docker run --name barebudget-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=barebudget -p 5432:5432 -d postgres:16-alpine
> ```

---

### 2. Menjalankan Aplikasi Android

1. Buka folder root project di **Android Studio**.
2. Pastikan file konfigurasi `google-services.json` sudah ditaruh di direktori `app/` untuk kebutuhan Firebase Auth.
3. Siapkan emulator Android atau sambungkan perangkat HP langsung.
4. Tekan tombol **Run 'app'** (`Shift + F10`) atau compile lewat terminal:

```bash
./gradlew installDebug
# atau assemble multi-module:
./gradlew :domain:build :data:assembleDebug :app:assembleDebug
```

### 3. Build Release (Signed APK / AAB)

Release build memakai **R8 minification** dan ditandatangani dengan keystore yang kredensialnya **tidak pernah masuk git**:

1. Generate keystore: `keytool -genkey -v -keystore keystores/barebudget-release.keystore -alias barebudget -keyalg RSA -keysize 4096 -validity 10000`
2. Buat `keystore.properties` di root project (sudah di-gitignore):
   ```properties
   storeFile=keystores/barebudget-release.keystore
   storePassword=****
   keyAlias=barebudget
   keyPassword=****
   ```
3. Build:
   ```bash
   ./gradlew :app:assembleRelease   # APK signed (~5 MB)
   ./gradlew :app:bundleRelease     # AAB untuk Play Store
   ```

> Tanpa `keystore.properties`, build release tetap jalan tapi menghasilkan APK *unsigned* — cocok untuk CI/reviewer.

### 4. Menjalankan Pengujian & Verifikasi

```bash
# Unit test domain + data (termasuk ApiContractTest MockWebServer)
./gradlew :domain:test :data:testDebugUnitTest --tests "*ApiContractTest*"

# Verifikasi batasan arsitektur (memastikan :domain tidak mengimpor library Android)
./gradlew :domain:check

# Uji coba outbox sync offline: aktifkan mode pesawat → buat transaksi → matikan mode pesawat → amati log WorkManager "outbox_sync"
```

---

## 🎨 Sistem Desain Material 3

BareBudget mengimplementasikan pedoman desain **Material Design 3 (M3)** secara menyeluruh:
* **`M3 Dynamic Theming`**: Palet warna tonal adaptif (*Tonal Spot / Dynamic Colors*) yang selaras dengan tema Material You di Android 12+.
* **`M3 Navigation Bar`**: Navigasi bawah standar Material 3 dengan indikator kapsul (*pill*) aktif dan transisi yang halus.
* **`M3 Elevated & Outlined Cards`**: Pengelompokan informasi keuangan dengan hirarki elevasi permukaan yang rapi dan terstruktur.
* **`M3 Expressive Typography & Shapes`**: Sudut lengkung (*shapes*) membulat yang ekspresif dan ergonomis saat dioperasikan dengan satu tangan.

---

## 📄 Lisensi

Proyek ini didistribusikan di bawah lisensi **MIT License**. Lihat file `LICENSE` untuk informasi selengkapnya.

<div align="center">
  <sub>Dibuat dengan ❤️ oleh <a href="https://github.com/Udean777">Udean777</a> untuk membantu pengelolaan keuangan yang lebih sehat dan terencana.</sub>
</div>
