# Audit Validasi Input — Bareuang

Status audit menyeluruh terhadap seluruh permukaan input aplikasi (UI, ViewModel, use-case, data layer, network/proxy). Dokumen ini memuat temuan A–E dan rencana perbaikan berprioritas.

> Semua baris (path:line) mengacu pada kondisi kode saat audit dilakukan.

---

## Prioritas perbaikan (urutan eksekusi)

| Prioritas | Area | Dampak |
|---|---|---|
| **A** | Integritas data (saldo, amount, non-blank) | Korupsi data / saldo negatif |
| **B** | Validasi file & image (OOM, format) | Crash / OOM / konten tak terpercaya |
| **C** | Proxy & network (CORS, base64, rate-limit) | Abuse / biaya / leak memori |
| **D** | Input teks bebas (length limit) | Memory / abuse |
| **E** | Tanggal & format | Crash / data salah |

---

## A. Integritas data

### A.1 — Transfer ke dompet yang sama hanya dicek di ViewModel
- **Lokasi:** `AddTransactionViewModel.kt:261`
- **Masalah:** Data layer (`TransactionLocalDataSource`) tidak pernah memvalidasi `walletId == toWalletId`. Bypass VM → transfer ke dompet yang sama tersimpan.
- **Perbaikan:** Pindahkan/duplikasi guard `walletId != toWalletId` ke `TransactionLocalDataSource.createTransaction`.

### A.2 — Saldo & amount tanpa guard `> 0` / non-blank
- **Lokasi:**
  - `GoalLocalDataSource.depositToGoal:60` — `balanceService.add(-amount)` tanpa cek saldo → saldo negatif
  - `GoalLocalDataSource.createGoal:40-52` — `targetAmount` 0/negatif, `name` blank lolos
  - `WalletLocalDataSource.createWallet:63-78` — `name` blank, `balance` negatif lolos
  - `DueBillLocalDataSource.createDueBill:48-66` — `providerName` blank, `totalAmount` 0/negatif lolos
  - `BudgetLocalDataSource.setBudget:23-44` — `monthlyLimit <= 0` lolos
  - `TransactionLocalDataSource.createTransaction` — tidak ada `amount > 0`
- **Perbaikan:** Tambahkan validasi invariant di tiap datasource: `amount > 0`, `name/provider isNotBlank`, dan cek saldo sebelum mengurangi.

### A.3 — DB tanpa jaring pengaman
- **Lokasi:** `data/local/room/Entities.kt`, `Daos.kt`
- **Masalah:** Tidak ada `CHECK`/FK; `@Insert(REPLACE)` menimpa duplikat diam-diam; `updateBalance`/`depositToGoal` bisa negatif.
- **Perbaikan:** Tambahkan `@Check` constraint (mis. `amount > 0`) pada kolom numerik, dan `@Index` unik di mana relevan. Integritas final di DB, bukan hanya app layer.

---

## B. Validasi file & image

### B.1 — Receipt AI tanpa batas bytes client-side
- **Lokasi:** `data/service/ReceiptAiService.kt:85-107`
- **Masalah:** Image dimensi ekstrem bisa **OOM** saat decode; `total`/`category`/`merchant` response eksternal tidak disanitasi di Android (total negatif lolos).
- **Perbaikan:**
  - Batasi ukuran bytes (mis. tolak > ~5MB) sebelum decode.
  - Batasi dimensi gambar saat decode (guaranteed max side).
  - Sanitasi di client: `total = max(0L, total)`, `merchant.trim()`, fallback category.

### B.2 — Provider image & backup JSON tanpa guard
- **Lokasi:** `DueBillFormDialog.kt:57-64, 208-215`; `BackupRestoreManager.kt:70-116`
- **Masalah:** Tanpa batas ukuran/format; `importBackupFromUri` baca seluruh stream → OOM; isi backup yang dimanipulasi dipercaya penuh (negatif/corrupt masuk DB).
- **Perbaikan:** Batas ukuran file image/backup, allowlist mime, dan validasi isi backup (version, nilai numerik, tanggal) sebelum insert.

### B.3 — CSV import tanpa batas konten
- **Lokasi:** `ImportMutasiViewModel.kt:109`, `data/service/CsvMutasiParser.kt:18`
- **Masalah:** Hanya guard 5MB; file raksasa baca penuh ke memory.
- **Perbaikan:** Batasi jumlah baris (mis. tolak > 5.000 baris) + batas total bytes.

---

## C. Proxy & network

### C.1 — `parse-receipt.js`
- **Lokasi:** `web/api/parse-receipt.js`
- **Masalah:**
  - CORS terbuka `Access-Control-Allow-Origin: *`
  - Tidak validasi base64 benar-benar image sebelum kirim ke Gemini (abuse + biaya)
  - Tidak ada mime allowlist
  - `_rateMap` tidak pernah di-prune → memory leak single-instance
- **Perbaikan:**
  - Validasi bahwa base64 decode menjadi gambar (cek magic bytes JPEG/PNG/WebP/GIF) dan mime allowlist.
  - Prune `_rateMap` (hapus entri kedaluwarsa saat akses).
  - Evaluasi batasi CORS origin (bila hanya dipakai app native, CORS tak relevan untuk non-browser — bisa hapus header CORS).

### C.2 — `download.js`
- **Lokasi:** `web/api/download.js`
- **Masalah:** Tanpa batasan method (POST lolos), tanpa rate-limit/auth.
- **Perbaikan:** Hanya terima `GET`/`HEAD`.

---

## D. Input teks bebas — tanpa length limit

- **Lokasi:** merchant & notes (`AddTransactionScreen`, `TransferScreen`, `GoalFormDialog`, `DueBillFormDialog`), nama wallet (`WalletsScreen`), custom provider name, search query (`AllTransactionsScreen`).
- **Masalah:** Input raksasa → memory; query besar langsung `contains`.
- **Perbaikan:** Batas panjang konsisten (mis. `maxLength` di `OutlinedTextField` via `onValueChange.take(n)`) untuk tiap field teks. Nilai usulan: merchant/provider/wallet 100, notes 500, search 100.

---

## E. Tanggal & format

### E.1 — Tanggal tidak divalidasi masa lalu/masa depan
- **Lokasi:** AddTransaction, GoalFormDialog, DueBillFormDialog; `OcrScanViewModel.kt:101`
- **Masalah:** OCR AI-date hanya cek regex `\d{4}-\d{2}-\d{2}` → `2025-13-40` lolos.
- **Perbaikan:** Validasi tanggal nyata (`LocalDate.parse` dalam try/catch) setelah regex; putuskan kebijakan masa lalu/masa depan per konteks.

### E.2 — `DateUtils` throw → `UnknownError`
- **Lokasi:** `data/utils/DateUtils.kt`; panggilan di datasource
- **Masalah:** Input korup (CSV/backup/AI) → `IllegalArgumentException` dipetakan ke `UnknownError` generik; bisa crash list.
- **Perbaikan:** Bungkus parse dalam try/catch → `DataException`, atau sediakan varian lenient untuk jalur read.

### E.3 — Inkonsistensi digit & substring rawan crash
- **Lokasi:** `WalletsScreen.kt:387` (15 digit) vs `AmountTextField` (12 digit); `GetCashflowAnalyticsUseCase.kt:20` `date.substring(0,10)`
- **Masalah:** Batas digit tidak konsisten; substring bisa `StringIndexOutOfBoundsException`.
- **Perbaikan:** Seragamkan batas digit; guard `date.length >= 10` sebelum substring.

---

## Catatan arsitektur (di luar A–E)

- Tidak ada validasi di **domain layer** — semua request/data class polos.
- Tidak ada **`ValidationException`** — datasource lempar `IllegalArgumentException` mentah → `UnknownError`.
- `amount` tidak pernah dicek `> 0` di use case (`BulkCreate`, `CheckDailyBudget`) → negatif/nol lolos, `sumOf` bisa overflow silent.

Rekomendasi: implementasi dimulai dari **A** (integritas data) sebagai fondasi, lalu **B** (file), **C** (proxy), **D** (length), **E** (tanggal). Setiap item ditandai selesai saat guard ditambahkan + diuji.
