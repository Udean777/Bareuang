package com.ssajudn.bareuang.data.datasource.local

import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.CategorySummary
import com.ssajudn.bareuang.domain.model.DashboardTransactionData
import com.ssajudn.bareuang.data.service.WalletBalanceService
import com.ssajudn.bareuang.data.error.ApiErrorParser
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.error.TransactionValidationReason
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
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
    private val balanceService: WalletBalanceService
) {

    suspend fun getDashboardTransactions(monthYear: String, todayIso: String): Result<DashboardTransactionData> =
        withContext(Dispatchers.IO) {
            try {
                val monthStart = "$monthYear-01"
                val nextMonth = java.time.YearMonth.parse(monthYear).plusMonths(1).toString() + "-01"
                val todayEnd = java.time.LocalDate.parse(todayIso).plusDays(1).toString()
                val dao = db.transactionDao()
                val categories = dao.getExpenseByCategory(monthStart, nextMonth).map {
                    CategorySummary(
                        category = com.ssajudn.bareuang.data.mapper.PersistenceMappers.safeCategory(it.category),
                        total = it.total ?: 0L,
                        count = it.count,
                    )
                }
                Result.success(
                    DashboardTransactionData(
                        totalSpent = dao.getExpenseTotal(monthStart, nextMonth),
                        todaySpent = dao.getExpenseTotalForDay(todayIso, todayEnd),
                        topCategories = categories,
                        recentTransactions = dao.getRecentTransactions(5).map { it.toTransaction() },
                        recurringTransactions = dao.getRecurringTemplates().map { it.toTransaction() },
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun getAllTransactions(): Result<List<Transaction>> = withContext(Dispatchers.IO) {
        try {
            Result.success(db.transactionDao().getAllTransactions().map { it.toTransaction() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> =
        withContext(Dispatchers.IO) {
            try {
                requireValidPersistenceRequest(request)
                val dateStr = request.date.ifBlank {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                }
                val isRecurring = request.recurringInterval != com.ssajudn.bareuang.domain.model.RecurringInterval.NONE
                val nextDate = if (isRecurring) {
                    com.ssajudn.bareuang.domain.utils.DateUtils.calculateNextDueDate(dateStr, request.recurringInterval.name)
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
                    validateCurrentBalance(request)
                    if (!isRecurring) {
                        balanceService.adjustForCreate(request)
                    }
                    db.transactionDao().insertTransaction(LocalTransactionEntity.fromTransaction(newTx, isSynced = false))
                }
                Result.success(newTx)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(ApiErrorParser.fromThrowable(e))
            }
        }

    suspend fun bulkCreate(requests: List<CreateTransactionRequest>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var inserted = 0
            db.withTransaction {
                for (req in requests) {
                    requireValidPersistenceRequest(req)
                    validateCurrentBalance(req)
                    val dateStr = req.date.ifBlank { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()) }
                    val isRecurring = req.recurringInterval != com.ssajudn.bareuang.domain.model.RecurringInterval.NONE
                    val nextDate = if (isRecurring) com.ssajudn.bareuang.domain.utils.DateUtils.calculateNextDueDate(dateStr, req.recurringInterval.name) else null
                    val newTx = Transaction(
                        id = UUID.randomUUID().toString(),
                        amount = req.amount,
                        type = req.type,
                        category = req.category,
                        merchant = req.merchant,
                        date = dateStr,
                        notes = req.notes,
                        receiptUrl = req.receiptUrl,
                        walletId = req.walletId,
                        toWalletId = req.toWalletId,
                        recurringInterval = req.recurringInterval,
                        isRecurringParent = isRecurring,
                        parentRecurringId = null,
                        nextOccurrenceDate = nextDate
                    )
                    if (!isRecurring) balanceService.adjustForCreate(req)
                    db.transactionDao().insertTransaction(LocalTransactionEntity.fromTransaction(newTx, isSynced = false))
                    inserted++
                }
            }
            Result.success(inserted)
        } catch (e: CancellationException) {
            throw e
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    private fun requireValidPersistenceRequest(request: CreateTransactionRequest) {
        // Defensive adapter guards mirror the domain validation boundary for direct repository callers.
        // Balance checks stay below because only the persistence transaction sees the latest stored balance.
        if (request.amount <= 0) {
            throw AppException.TransactionValidationException(TransactionValidationReason.INVALID_AMOUNT)
        }
        if (request.walletId.isNullOrBlank()) {
            throw AppException.TransactionValidationException(TransactionValidationReason.WALLET_REQUIRED)
        }
        if (request.type == TransactionType.TRANSFER) {
            if (request.toWalletId.isNullOrBlank()) {
                throw AppException.TransactionValidationException(TransactionValidationReason.TO_WALLET_REQUIRED)
            }
            if (request.walletId == request.toWalletId) {
                throw AppException.TransactionValidationException(TransactionValidationReason.SAME_WALLET)
            }
        }
    }

    private suspend fun validateCurrentBalance(request: CreateTransactionRequest) {
        val walletId = request.walletId
            ?: throw AppException.TransactionValidationException(TransactionValidationReason.WALLET_REQUIRED)
        val wallet = db.walletDao().getWalletById(walletId)
            ?: throw AppException.TransactionValidationException(TransactionValidationReason.WALLET_REQUIRED)
        if (request.type == TransactionType.TRANSFER &&
            db.walletDao().getWalletById(request.toWalletId.orEmpty()) == null
        ) {
            throw AppException.TransactionValidationException(TransactionValidationReason.TO_WALLET_REQUIRED)
        }
        if ((request.type == TransactionType.EXPENSE || request.type == TransactionType.TRANSFER) &&
            wallet.balance < request.amount
        ) {
            throw AppException.InsufficientBalanceException(
                availableBalance = wallet.balance,
                requestedAmount = request.amount
            )
        }
    }

    fun observeTransactions(): Flow<List<Transaction>> =
        db.transactionDao().observeAllTransactions().map { list -> list.map { it.toTransaction() } }
}
