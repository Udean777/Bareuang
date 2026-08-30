package com.ssajudn.bareuang.data.local

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssajudn.bareuang.data.local.room.*
import androidx.room.withTransaction
import com.ssajudn.bareuang.domain.error.AppException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@androidx.annotation.Keep
data class BareuangBackupData(
    val version: Int = 1,
    val appVersion: String = "1.0.0",
    val backupDate: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val transactions: List<LocalTransactionEntity> = emptyList(),
    val dueBills: List<LocalDueBillEntity> = emptyList(),
    val budgets: List<LocalBudgetEntity> = emptyList(),
    val goals: List<LocalGoalEntity> = emptyList(),
    val wallets: List<LocalWalletEntity> = emptyList()
)

@Singleton
class BackupRestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val backup = BareuangBackupData(
            version = 1,
            appVersion = "1.0.0",
            backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            transactions = db.transactionDao().getAllTransactions(),
            dueBills = db.dueBillDao().getAllDueBills(),
            budgets = db.budgetDao().getAllBudgets(),
            goals = db.goalDao().getAllGoals(),
            wallets = db.walletDao().getAllWallets()
        )
        gson.toJson(backup)
    }

    suspend fun exportBackupToUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = createBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(AppException.DataException("Cannot open file for writing"))
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("Backup", "export failed", e)
            Result.failure(if (e is AppException) e else AppException.DataException(cause = e))
        }
    }

    suspend fun importBackupFromUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Guard size: reject > 5MB (prevent OOM)
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                if (pfd.statSize > 5 * 1024 * 1024) return@withContext Result.failure(AppException.DataException("File backup terlalu besar (maks 5MB)"))
            }
            val raw = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                if (bytes.size > 5 * 1024 * 1024) return@withContext Result.failure(AppException.DataException("File backup terlalu besar (maks 5MB)"))
                String(bytes, Charsets.UTF_8)
            } ?: return@withContext Result.failure(AppException.DataException("Cannot open file for reading"))

            val backup = gson.fromJson(raw, BareuangBackupData::class.java)
                ?: return@withContext Result.failure(AppException.DataException("Invalid backup format"))
            if (backup.version != 1) return@withContext Result.failure(AppException.DataException("Versi backup tidak didukung"))
            // Light sanitize before insert (reject negatives / blank names)
            val sanitizedWallets = backup.wallets.filter { it.name.isNotBlank() && it.balance >= 0 }

            var totalRestored = 0

            val sanitizedTx = backup.transactions.filter { it.amount > 0 }
            val sanitizedBills = backup.dueBills.filter { it.providerName.isNotBlank() && it.totalAmount > 0 }
            val sanitizedGoals = backup.goals.filter { it.name.isNotBlank() && it.targetAmount > 0 }
            db.withTransaction {
                if (sanitizedWallets.isNotEmpty()) {
                    db.walletDao().insertWallets(sanitizedWallets)
                    totalRestored += sanitizedWallets.size
                }
                if (sanitizedTx.isNotEmpty()) {
                    db.transactionDao().insertTransactions(sanitizedTx)
                    totalRestored += sanitizedTx.size
                }
                if (sanitizedBills.isNotEmpty()) {
                    db.dueBillDao().insertDueBills(sanitizedBills)
                    totalRestored += sanitizedBills.size
                }
                if (backup.budgets.isNotEmpty()) {
                    db.budgetDao().insertBudgets(backup.budgets)
                    totalRestored += backup.budgets.size
                }
                if (sanitizedGoals.isNotEmpty()) {
                    db.goalDao().insertGoals(sanitizedGoals)
                    totalRestored += sanitizedGoals.size
                }
            }

            Result.success(totalRestored)
        } catch (e: Exception) {
            android.util.Log.e("Backup", "import failed", e)
            Result.failure(if (e is AppException) e else AppException.UnknownError(cause = e))
        }
    }
}
