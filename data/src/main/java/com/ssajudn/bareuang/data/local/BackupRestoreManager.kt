package com.ssajudn.bareuang.data.local

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.LocalBudgetEntity
import com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity
import com.ssajudn.bareuang.data.local.room.LocalDueBillEntity
import com.ssajudn.bareuang.data.local.room.LocalGoalEntity
import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import com.ssajudn.bareuang.data.local.room.LocalWalletEntity
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.error.BackupOperationReason
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.BackupRecordPreview
import com.ssajudn.bareuang.domain.model.BackupRestorePreview
import com.ssajudn.bareuang.domain.model.BackupRestoreSummary
import com.ssajudn.bareuang.domain.port.BackupRestorePort
import com.ssajudn.bareuang.domain.port.CurrencyPreferencesPort
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_LEGACY_BACKUP_BYTES = 5 * 1024 * 1024
private const val BACKUP_FORMAT_VERSION = 2

@androidx.annotation.Keep
data class BareuangBackupData(
    val version: Int = 2,
    val appVersion: String = "1.0.0",
    val backupDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val transactions: List<LocalTransactionEntity> = emptyList(),
    val dueBills: List<LocalDueBillEntity> = emptyList(),
    val budgets: List<LocalBudgetEntity> = emptyList(),
    val categoryBudgets: List<LocalCategoryBudgetEntity> = emptyList(),
    val goals: List<LocalGoalEntity> = emptyList(),
    val wallets: List<LocalWalletEntity> = emptyList(),
)

@androidx.annotation.Keep
private data class BareuangBackupEnvelope(
    val formatVersion: Int,
    val payload: BareuangBackupData,
    val sha256: String,
    val currencyCode: String? = null,
)

@Singleton
class BackupRestoreManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val currencyPreferences: CurrencyPreferencesPort,
) : BackupRestorePort {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val crypto = BackupCryptoCodec()
    private val preparedRestores = ConcurrentHashMap<String, PreparedRestore>()
    private val activeRestores = ConcurrentHashMap.newKeySet<String>()

    private data class ParsedBackup(
        val data: BareuangBackupData,
        val currencyCode: String?,
        val isEncrypted: Boolean,
    )

    private data class PreparedRestore(
        val backup: ParsedBackup,
    )

    private fun checksum(payload: String): String = MessageDigest.getInstance("SHA-256")
        .digest(payload.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private suspend fun createBackupJson(): ByteArray = withContext(Dispatchers.IO) {
        val payload = BareuangBackupData(
            appVersion = currentAppVersion(),
            transactions = db.transactionDao().getAllTransactions(),
            dueBills = db.dueBillDao().getAllDueBills(),
            budgets = db.budgetDao().getAllBudgets(),
            categoryBudgets = db.budgetDao().getAllCategoryBudgets(),
            goals = db.goalDao().getAllGoals(),
            wallets = db.walletDao().getAllWallets(),
        )
        val payloadJson = gson.toJson(payload)
        val envelope = BareuangBackupEnvelope(
            formatVersion = BACKUP_FORMAT_VERSION,
            payload = payload,
            sha256 = checksum(payloadJson),
            currencyCode = currencyPreferences.getCurrency().code,
        )
        gson.toJson(envelope).toByteArray(StandardCharsets.UTF_8)
    }

    private fun currentAppVersion(): String = runCatching {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    }.getOrDefault("unknown")

    override suspend fun exportBackup(uri: String, password: CharArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val plainText = createBackupJson()
            val encrypted = try {
                crypto.encrypt(plainText, password)
            } finally {
                plainText.fill(0)
                password.fill('\u0000')
            }
            try {
                context.contentResolver.openOutputStream(uri.toUri())?.use { output ->
                    output.write(encrypted)
                    output.flush()
                } ?: return@withContext Result.failure(
                    AppException.BackupOperationException(BackupOperationReason.DESTINATION_UNAVAILABLE)
                )
                Result.success(Unit)
            } finally {
                encrypted.fill(0)
            }
        } catch (e: CancellationException) {
            password.fill('\u0000')
            throw e
        } catch (e: Exception) {
            password.fill('\u0000')
            android.util.Log.e("Backup", "encrypted export failed", e)
            Result.failure(mapBackupError(e))
        }
    }

    private fun validateDate(value: String?): Boolean =
        value == null || Regex("^\\d{4}-\\d{2}-\\d{2}([ T].*)?$").matches(value)

    private fun requireValid(condition: Boolean) {
        if (!condition) throw AppException.BackupOperationException(BackupOperationReason.INVALID_FILE)
    }

    private suspend fun validateAndNormalize(input: BareuangBackupData): BareuangBackupData {
        requireValid(input.version in 1..BACKUP_FORMAT_VERSION)
        requireValid(
            input.transactions.size + input.dueBills.size + input.budgets.size +
                input.categoryBudgets.size + input.goals.size + input.wallets.size <= 100_000,
        )
        val walletIds = input.wallets.map { it.id }
        requireValid(walletIds.all { it.isNotBlank() } && walletIds.toSet().size == walletIds.size)
        val existingWallets = db.walletDao().getAllWallets()
        val knownWallets = walletIds.toSet() + existingWallets.map { it.id }.toSet()
        val txIds = input.transactions.map { it.id }
        requireValid(txIds.all { it.isNotBlank() } && txIds.toSet().size == txIds.size)
        requireValid(
            input.transactions.all {
                it.amount > 0 && validateDate(it.date) &&
                    (it.walletId == null || it.walletId in knownWallets) &&
                    (it.toWalletId == null || it.toWalletId in knownWallets)
            },
        )
        requireValid(input.dueBills.all { it.id.isNotBlank() && it.providerName.isNotBlank() && it.totalAmount > 0 && validateDate(it.dueDate) })
        requireValid(input.budgets.all { Regex("^\\d{4}-\\d{2}$").matches(it.monthYear) && it.monthlyLimit >= 0 })
        requireValid(input.categoryBudgets.all { Regex("^\\d{4}-\\d{2}$").matches(it.monthYear) && it.category.isNotBlank() && it.limitAmount >= 0 })
        requireValid(input.goals.all { it.id.isNotBlank() && it.name.isNotBlank() && it.targetAmount > 0 && it.currentAmount >= 0 })
        return input
    }

    override suspend fun previewBackup(uri: String, password: CharArray?): Result<BackupRestorePreview> = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri.toUri())?.use { input ->
                readBackupBytes(input, BackupCryptoCodec.MAX_ENCRYPTED_BYTES)
            } ?: return@withContext Result.failure(
                AppException.BackupOperationException(BackupOperationReason.SOURCE_UNAVAILABLE)
            )

            val encrypted = crypto.isEncrypted(bytes)
            val plainText = if (encrypted) {
                try {
                    crypto.decrypt(bytes, password ?: throw InvalidBackupPasswordOrCorruptFileException())
                } finally {
                    bytes.fill(0)
                }
            } else {
                if (bytes.size > MAX_LEGACY_BACKUP_BYTES) {
                    bytes.fill(0)
                    throw BackupTooLargeException()
                }
                bytes.copyOf().also { bytes.fill(0) }
            }
            val parsed = try {
                parseBackup(plainText, encrypted)
            } finally {
                plainText.fill(0)
                password?.fill('\u0000')
            }
            val normalized = validateAndNormalize(parsed.data)
            val backup = parsed.copy(data = normalized)
            val previewId = UUID.randomUUID().toString()
            val preview = createPreview(previewId, backup)
            preparedRestores.clear()
            preparedRestores[previewId] = PreparedRestore(backup)
            Result.success(preview)
        } catch (e: CancellationException) {
            password?.fill('\u0000')
            throw e
        } catch (e: Exception) {
            password?.fill('\u0000')
            android.util.Log.e("Backup", "restore preview failed", e)
            Result.failure(mapBackupError(e))
        }
    }

    override suspend fun confirmRestore(previewId: String): Result<BackupRestoreSummary> = withContext(Dispatchers.IO) {
        val prepared = preparedRestores[previewId]
            ?: return@withContext Result.failure(
                AppException.BackupOperationException(BackupOperationReason.PREVIEW_EXPIRED)
            )
        if (!activeRestores.add(previewId)) {
            return@withContext Result.failure(
                AppException.BackupOperationException(BackupOperationReason.RESTORE_IN_PROGRESS)
            )
        }
        try {
            val summary = db.withTransaction {
                val safe = validateAndNormalize(prepared.backup.data)
                val before = recordCounts(safe)
                db.walletDao().insertWallets(safe.wallets)
                db.transactionDao().insertTransactions(safe.transactions)
                db.dueBillDao().insertDueBills(safe.dueBills)
                db.budgetDao().insertBudgets(safe.budgets)
                db.budgetDao().insertCategoryBudgets(safe.categoryBudgets)
                db.goalDao().insertGoals(safe.goals)
                BackupRestoreSummary(
                    added = before.sumOf { it.new },
                    replaced = before.sumOf { it.replaced },
                    currencyCode = prepared.backup.currencyCode,
                )
            }
            val currencyApplied = try {
                summary.currencyCode?.let { currencyPreferences.setCurrency(AppCurrency.fromCode(it)) }
                true
            } catch (e: Exception) {
                android.util.Log.e("Backup", "restored records, but failed to update currency preference", e)
                false
            }
            discardRestore(previewId)
            Result.success(summary.copy(currencyApplied = currencyApplied))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("Backup", "restore apply failed", e)
            Result.failure(mapBackupError(e))
        } finally {
            activeRestores.remove(previewId)
        }
    }

    override fun discardRestore(previewId: String) {
        preparedRestores.remove(previewId)
    }

    private suspend fun parseBackup(bytes: ByteArray, encrypted: Boolean): ParsedBackup {
        val root = JsonParser.parseString(String(bytes, StandardCharsets.UTF_8)).asJsonObject
        val data: BareuangBackupData
        val currencyCode: String?
        if (root.has("payload") && root.has("sha256")) {
            val envelope = gson.fromJson(root, BareuangBackupEnvelope::class.java)
                ?: throw InvalidBackupFileException()
            val payloadJson = gson.toJson(envelope.payload)
            requireValid(
                envelope.formatVersion == BACKUP_FORMAT_VERSION &&
                    envelope.sha256.equals(checksum(payloadJson), ignoreCase = true),
            )
            data = envelope.payload
            currencyCode = validatedCurrencyCode(envelope.currencyCode)
        } else {
            // Keep importing the original unencrypted JSON format.
            data = gson.fromJson(root, BareuangBackupData::class.java) ?: throw InvalidBackupFileException()
            currencyCode = null
        }
        return ParsedBackup(data, currencyCode, encrypted)
    }

    private fun validatedCurrencyCode(code: String?): String? {
        if (code == null) return null
        val supported = AppCurrency.entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: throw AppException.BackupOperationException(BackupOperationReason.UNSUPPORTED_CURRENCY)
        return supported.code
    }

    private suspend fun createPreview(id: String, backup: ParsedBackup): BackupRestorePreview {
        val data = backup.data
        val wallets = db.walletDao().getAllWallets()
        val transactions = db.transactionDao().getAllTransactions()
        val dueBills = db.dueBillDao().getAllDueBills()
        val budgets = db.budgetDao().getAllBudgets()
        val categoryBudgets = db.budgetDao().getAllCategoryBudgets()
        val goals = db.goalDao().getAllGoals()
        val existingCategoryKeys = categoryBudgets.map { "${it.monthYear}|${it.category}" }.toSet()
        return BackupRestorePreview(
            id = id,
            backupDate = data.backupDate,
            appVersion = data.appVersion,
            currencyCode = backup.currencyCode,
            isEncrypted = backup.isEncrypted,
            hasReceiptReferences = data.transactions.any { !it.receiptUrl.isNullOrBlank() },
            wallets = compareRecords(data.wallets.map { it.id }, wallets.map { it.id }.toSet()),
            transactions = compareRecords(data.transactions.map { it.id }, transactions.map { it.id }.toSet()),
            dueBills = compareRecords(data.dueBills.map { it.id }, dueBills.map { it.id }.toSet()),
            budgets = compareRecords(data.budgets.map { it.monthYear }, budgets.map { it.monthYear }.toSet()),
            categoryBudgets = compareRecords(data.categoryBudgets.map { "${it.monthYear}|${it.category}" }, existingCategoryKeys),
            goals = compareRecords(data.goals.map { it.id }, goals.map { it.id }.toSet()),
        )
    }

    private fun compareRecords(incomingIds: List<String>, existingIds: Set<String>): BackupRecordPreview {
        val replaced = incomingIds.count(existingIds::contains)
        return BackupRecordPreview(
            total = incomingIds.size,
            new = incomingIds.size - replaced,
            replaced = replaced,
        )
    }

    private suspend fun recordCounts(data: BareuangBackupData): List<BackupRecordPreview> {
        val categoryKeys = db.budgetDao().getAllCategoryBudgets().map { "${it.monthYear}|${it.category}" }.toSet()
        return listOf(
            compareRecords(data.wallets.map { it.id }, db.walletDao().getAllWallets().map { it.id }.toSet()),
            compareRecords(data.transactions.map { it.id }, db.transactionDao().getAllTransactions().map { it.id }.toSet()),
            compareRecords(data.dueBills.map { it.id }, db.dueBillDao().getAllDueBills().map { it.id }.toSet()),
            compareRecords(data.budgets.map { it.monthYear }, db.budgetDao().getAllBudgets().map { it.monthYear }.toSet()),
            compareRecords(data.categoryBudgets.map { "${it.monthYear}|${it.category}" }, categoryKeys),
            compareRecords(data.goals.map { it.id }, db.goalDao().getAllGoals().map { it.id }.toSet()),
        )
    }

    private fun mapBackupError(error: Exception): AppException = when (error) {
        is AppException -> error
        is BackupTooLargeException -> AppException.BackupOperationException(BackupOperationReason.FILE_TOO_LARGE, error)
        is InvalidBackupPasswordOrCorruptFileException -> AppException.BackupOperationException(BackupOperationReason.INVALID_PASSWORD_OR_FILE, error)
        is InvalidBackupFileException -> AppException.BackupOperationException(BackupOperationReason.INVALID_FILE, error)
        else -> AppException.BackupOperationException(BackupOperationReason.INVALID_FILE, error)
    }
}
