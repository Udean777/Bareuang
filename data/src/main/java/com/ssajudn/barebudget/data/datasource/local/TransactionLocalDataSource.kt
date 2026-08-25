package com.ssajudn.barebudget.data.datasource.local

import com.ssajudn.barebudget.data.local.room.AppDatabase
import com.ssajudn.barebudget.data.local.room.LocalTransactionEntity
import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.TransactionType
import com.ssajudn.barebudget.data.service.WalletBalanceService
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import com.ssajudn.barebudget.data.error.ApiErrorParser
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionLocalDataSource @Inject constructor(
    private val db: AppDatabase,
    private val balanceService: WalletBalanceService,
    private val sessionManager: com.ssajudn.barebudget.data.local.UserSessionManager
) {

    suspend fun getTransactions(category: String?, page: Int, limit: Int): Result<List<Transaction>> =
        withContext(Dispatchers.IO) {
            try {
                val safePage = page.coerceAtLeast(1)
                val safeLimit = limit.coerceAtLeast(0)
                val offset = (safePage - 1) * safeLimit
                val entities = if (category.isNullOrBlank()) {
                    db.transactionDao().getTransactionsPaged(safeLimit, offset)
                } else {
                    db.transactionDao().getTransactionsByCategoryPaged(category, safeLimit, offset)
                }
                Result.success(entities.map { it.toTransaction() })
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> =
        withContext(Dispatchers.IO) {
            try {
                // Validasi: semua tipe wajib pakai dompet
                if (request.walletId.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("Dompet wajib dipilih untuk transaksi"))
                }
                if (request.type == TransactionType.TRANSFER && request.toWalletId.isNullOrBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("Dompet tujuan wajib dipilih untuk transfer"))
                }
                // Validasi saldo untuk pengeluaran & transfer
                if (request.type == TransactionType.EXPENSE) {
                    val w = db.walletDao().getWalletById(request.walletId!!)
                        ?: return@withContext Result.failure(IllegalArgumentException("Dompet tidak ditemukan"))
                    if (w.balance < request.amount) {
                        return@withContext Result.failure(IllegalStateException("Saldo dompet tidak cukup. Saldo: ${w.balance}, dibutuhkan: ${request.amount}"))
                    }
                } else if (request.type == TransactionType.TRANSFER) {
                    val w = db.walletDao().getWalletById(request.walletId!!)
                        ?: return@withContext Result.failure(IllegalArgumentException("Dompet asal tidak ditemukan"))
                    if (w.balance < request.amount) {
                        return@withContext Result.failure(IllegalStateException("Saldo dompet asal tidak cukup"))
                    }
                }
                val dateStr = request.date.ifBlank {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                }
                val isRecurring = request.recurringInterval != com.ssajudn.barebudget.domain.model.RecurringInterval.NONE
                val nextDate = if (isRecurring) {
                    com.ssajudn.barebudget.utils.DateUtils.calculateNextDueDate(dateStr, request.recurringInterval.name)
                } else null

                val newTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = request.amount,
                    type = request.type,
                    category = request.category,
                    merchant = request.merchant,
                    date = dateStr,
                    notes = request.notes,
                    receiptUrl = request.receiptUrl,
                    walletId = request.walletId,
                    toWalletId = request.toWalletId,
                    recurringInterval = request.recurringInterval,
                    isRecurringParent = isRecurring,
                    parentRecurringId = null,
                    nextOccurrenceDate = nextDate
                )

                db.withTransaction {
                    if (!isRecurring) {
                        balanceService.adjustForCreate(request)
                    }
                    val entity = LocalTransactionEntity.fromTransaction(newTx, isSynced = false).copy(ownerId = sessionManager.userId)
                    db.transactionDao().insertTransaction(entity)
                }
                Result.success(newTx)
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun deleteTransaction(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                val tx = db.transactionDao().getTransactionById(id)
                if (tx != null) balanceService.revert(tx)
                db.transactionDao().deleteTransaction(id)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    fun observeTransactions(): Flow<List<Transaction>> =
        db.transactionDao().observeAllTransactions().map { list -> list.map { it.toTransaction() } }
}