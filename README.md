<div align="center">
  <img src="art/app_logo_new.png" width="120" height="120" alt="Bareuang Logo" style="border-radius: 32px;" />
  
  # BareBudget — Bareuang
  **Your cozy money companion · Teman cozy buat uangmu**
  
  *Atur pengeluaran bulanan, tahu sampai kapan uangmu tahan (Runway), kelola banyak dompet, dan wujudkan target tabungan — dengan beruang madu yang menemanimu.*
  
  ---
  
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Bareuang%20Bubbly-845400?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Bareuang Design](https://img.shields.io/badge/UI-Bareuang%20Modern%20Bubbly-F4A216?style=for-the-badge)]()
  [![Room DB](https://img.shields.io/badge/Local%20Cache-Room%20SQLite-3DDC84?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
  [![100% Offline](https://img.shields.io/badge/Privasi-100%25%20Offline-34A853?style=for-the-badge&logo=android&logoColor=white)]

</div>

---

## 📖 Tentang BareBudget — Bareuang

**BareBudget (Bareuang)** adalah aplikasi pencatat keuangan yang **hangat, bubbly, dan minimalis**. Dibangun di sekitar persona *friendly bear companion* — beruang madu dengan tas honey pot "B" dan koin Rp — yang mengubah tracking dari beban jadi kegiatan menyenangkan.

> *"Dengan pola jajan kayak sekarang, sampai kapan uangku tahan?"* → Beruang akan menjawab dengan **Financial Runway** & Death Day.

Antarmuka **100% Bareuang Design System** (*Modern Bubbly Minimalism* — `DESIGN.MD`): Jetpack Compose tanpa `MaterialKolor` dinamis, palet statis `surface #FDF9F3 / primary #845400 / primary-container #F4A216`, tipografi `Plus Jakarta Sans + Be Vietnam Pro`, shape `pill/squircle/rounded-xl`.

BareBudget **100% offline & privat**: tanpa backend Go, tanpa Firebase, tanpa izin `INTERNET`. Semua di Room lokal, ditambah **Backup & Restore JSON** & **Home Widget** beruang.

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
* **Notifikasi Pengingat Jatuh Tempo**: Notifikasi lokal otomatis saat tagihan mendekati jatuh tempo (H-3 s.d. H+ lewat tempo), lengkap dengan ringkasan nominal, penyedia, dan status tenggat (*hari ini*, *besok*, *terlambat N hari*).
* **Jam Pengingat Fleksibel**: Pilih sendiri waktu harian notifikasi muncul lewat menu Pengaturan (default pukul 12 malam). Pengecekan cepat juga berjalan setiap aplikasi dibuka atau tagihan ditambahkan/dibayar.
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

### 8. 🎨 Bareuang Design System & Filter Fleksibel
* **Palet Statis Bareuang**: Tanpa `Material You` — semua warna dari `DESIGN.MD` (`background #FDF9F3`, `primary-container #F4A216` honey, `outline-variant #D8C3AD`). Card `surfaceContainerLowest #FFFFFF` di canvas cream, border `0.8dp outlineVariant 0.35`.
* **Bentuk Pebble**: `rounded-sm 8 / DEFAULT 16 / md 24 / lg 32 / xl 48 / pill 9999` — semua kartu squircle/rounded-xl, tidak ada sudut tajam.
* **Floating Pill Nav (74dp gemuk) + Transfer Menonjol**: Bar kapsul melayang + tombol Transfer tengah `64dp` honey pill mengapung `-14dp` dengan border 3D, auto-hide di layar Transfer (full-screen).
* **Pencarian & Filter Cepat**: *Semua Transaksi* (search + bottom sheet tipe/kategori/dompet draft), *Tagihan* (`Semua/Belum Lunas/Lunas`), *Target* (`Semua/Aktif/Tercapai`).

### 9. 📦 Backup & Restore JSON Offline
* Ekspor seluruh data transaksi, dompet, tagihan, dan target tabungan ke file `.json` sebagai cadangan lokal.
* Pulihkan (*restore*) data kapan saja dengan cepat dan aman ke database lokal.

### 10. 🚀 Splash, Onboarding & Animasi Bear yang Fun
* **Splash Bareuang (Compose + Native)**: Logo beruang honey pot baru di card putih `rounded-xl 48dp` + soft blob cream, `headline-xl 40/800` + tagline pill `secondaryContainer`. Native splash `ic_splash_logo` diberi padding safe-zone 62% agar tidak ter-crop.
* **Onboarding Bubbly**: 3 slide + permission, ilustrasi di card putih `rounded-xl` shadow tertiary 0.10, dots pill honey `22×8`, language pill `surfaceContainerLow` + selected honey.
* **8 Animasi Interaktif**: `BearPeek` (muncul di Goal selesai & Budget input), `ConfettiBurst` (42 partikel honey/green/brown saat goal/transfer), `RollingNumber` (count-up saldo), `Bear Wiggle` (>90% progress), `StaggeredList` (40ms per item), `SpeedDial fan-out` (arc + rotate), `Squish` (logo squash saat refresh), `Tab Pop` (pill 1.08x).
* **Panduan 7 Langkah**: Spotlight coach marks tetap, bilingual, bisa replay di Settings.

### 11. 🔒 Clean Architecture & Privasi Maksimal
* **100% Offline & Tanpa Server**: Go backend & outbox dihapus, tidak ada `INTERNET` permission. Semua via Room lokal (`AppDatabase` + `withTransaction` row locking).
* **Multi-module Rapi**: `:domain` pure Kotlin (tanpa Android), `:data` Room/Backup/Notifications/Worker, `:presentation` Compose + Hilt Navigation tanpa `material-kolor`, `:app` composition root (`app → presentation → data → domain`).
* **Bug Analytics Fixed**: Cashflow kini exclude `isRecurringParent` & transaksi future (`date > today`) — income recurring Senin 31 tidak lagi muncul dini.
* **Slogan Natural**: `Finance that feels like a warm hug / Uang tenang, hati senang`, `Your cozy money companion / Teman cozy buat uangmu` — tidak kaku Google Translate.
* **Widget Bear-Themed**: Home widget Glance `24dp rounded-xl` outer cream + inner white pebble, header avatar beruang 28dp + pill `SISA RUNWAY` honey, price honey `22sp`, bear paw pill tertiary, track 8dp rounded, secondary cards daily/tagihan/terpakai.

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
├── data/                       # Android Library — Room, Backup JSON (→ :domain)
│   ├── src/main/java/com/ssajudn/barebudget/data/
│   │   ├── datasource/local/   # LocalDataSource + withTransaction + ownerId
│   │   ├── datasource/remote/  # RemoteDataSource (DTO→domain mapping)
│   │   ├── local/room/         # Entities, Daos, AppDatabase
│   │   ├── repository/         # *RepositoryImpl (local-only), DomainMappers
│   │   ├── notification/       # BillReminderWorker (WorkManager+Hilt), Scheduler, Prefs, NotificationHelper
│   │   ├── service/            # WalletBalanceService (single writer)
│   │   └── utils/              # DateUtils
│   └── schemas/                # Room schema history
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
│       │   ├── settings/       # Appearance (dark mode), JSON Backup/Restore, Profile
│       │   ├── splash/         # Animated Branded Splash Screen
│       │   ├── theme/          # AppShapes (AsymmetricHero, Squircle, Pill), crispBorder, Theme.kt
│       │   ├── tour/           # TourScript, TourOverlay (spotlight coach marks), TourRegistry
│       │   ├── transaction/    # Add Expense, TransferScreen, AllTransactions (Search & Filter BottomSheet)
│       │   └── wallets/        # Physical Debit-Style Wallet Cards
│       └── utils/              # CurrencyFormatter, CurrencyVisualTransformation, DateUtils (UI)
├── app/                        # Android Application — Composition Root & Application Entry (→ :presentation)
│   └── src/main/java/com/ssajudn/barebudget/
│       └── BareBudgetApplication.kt # Hilt + HiltWorkerFactory (WorkManager)
```

**Teknologi:** Room `withTransaction`, Gson, Hilt, WorkManager + `androidx.hilt`, Jetpack Compose (tanpa `material-kolor`), Glance widget, `Channel<UiEffect>`.

---

## 🚀 Panduan Menjalankan Project

### 1. Tanpa Backend — 100% Lokal

BareBudget tidak memerlukan server, API key, atau akun cloud apa pun. Cukup build dan jalankan.


---

### 2. Menjalankan Aplikasi Android

1. Buka folder root project di **Android Studio**.
2. Tidak perlu konfigurasi apa pun — tidak ada `google-services.json`, API key, maupun server yang dibutuhkan.
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

#### CI/CD: Build, Test & Release Otomatis ke GitHub Releases

| Workflow | Trigger | Hasil |
|----------|---------|-------|
| **CI** (`ci.yml`) | PR ke `main`/`dev`, atau push ke `main` | Build debug + seluruh unit test |
| **Release** (`release.yml`) | Push ke `main` | Test → build APK+AAB signed → update release **prerelease "Latest Build"** (aset `BareBudget-latest.*` di-replace tiap merge) |
| **Release** (`release.yml`) | Push tag `v*` (mis. `v1.0.0`) | Test → build signed → **GitHub Release stabil** `BareBudget-v1.0.0.*` dengan auto release notes |

Setup sekali saja — tambahkan 4 secret di repo (**Settings → Secrets and variables → Actions**):

| Secret | Isi |
|--------|-----|
| `SIGNING_KEYSTORE_BASE64` | Hasil `base64 -i keystores/barebudget-release.keystore` |
| `KEYSTORE_PASSWORD` | Password keystore |
| `KEY_ALIAS` | Alias key (mis. `barebudget`) |
| `KEY_PASSWORD` | Password key |

CI membaca kredensial dari environment variable — `keystore.properties` lokal tidak pernah dibutuhkan di repo.

### 4. Menjalankan Pengujian & Verifikasi

```bash
# Jalankan seluruh unit test
./gradlew test

# Verifikasi batasan arsitektur (memastikan :domain tidak mengimpor library Android)
./gradlew :domain:check

# Uji coba mode offline: aktifkan mode pesawat → gunakan aplikasi seperti biasa.
# Semua fitur tetap berfungsi penuh karena seluruh data berada di Room lokal.
```

---

## 🎨 Sistem Desain Bareuang

BareBudget meninggalkan Material You dan mengikuti penuh **`DESIGN.MD`** — *Modern Bubbly Minimalism*:
* **Palet**: `surface #FDF9F3` cream, `primary #845400` bear brown, `primary-container #F4A216` honey, `secondary #396842` green, `tertiary #7A5648` brown. Surfaces `surfaceContainerLowest #FFFFFF` (card) → `Low #F7F3ED` → `High #EBE8E2`.
* **Tipografi**: `Plus Jakarta Sans` (headline `40/800`, `32/700`, price `36/800 tabular`) + `Be Vietnam Pro` (body `18/500`, `16/400`, label `14/600 +0.05em`).
* **Shape**: `sm 8 / DEFAULT 16 / md 24 / lg 32 / xl 48 / full pill`. Card `rounded-md/lg`, dialog `xl`, button `pill` + 2dp honey bottom border 3D.
* **Elevation**: Tonal layering cream→white + soft ambient shadow 5-8% `tertiary #7A5648`, press sink.
* **Logo**: Beruang di honey pot "B" + daun + koin Rp, dipakai di launcher (IconKitchen adaptive), splash (padded 62%), in-app (`ic_app_logo`/`ic_bear_head`), notifikasi (`ic_bill_reminder.png` monochrome), widget avatar, README.

---

## 📄 Lisensi

Proyek ini didistribusikan di bawah lisensi **MIT License**. Lihat file `LICENSE` untuk informasi selengkapnya.

<div align="center">
  <sub>Dibuat dengan ❤️ oleh <a href="https://github.com/Udean777">Udean777</a> untuk membantu pengelolaan keuangan yang lebih sehat dan terencana.</sub>
</div>
