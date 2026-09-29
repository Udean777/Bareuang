package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.error.BulkTransactionReason
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.repository.TransactionCommandRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class BulkCreateTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionCommandRepository,
    private val walletRepository: WalletRepository,
    private val checkDailyBudget: CheckDailyBudgetUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        drafts: List<ImportDraft>,
        walletId: String,
        // Soft daily-budget nudge: when false the daily gate trips and returns a
        // DailyBudgetExceededException so the UI can ask before overriding; when
        // true (user confirmed) the gate is skipped.
        force: Boolean = false
    ): Result<Int> {
        if (walletId.isBlank()) return Result.failure(AppException.BulkTransactionException(BulkTransactionReason.WALLET_REQUIRED))
        // Duplicate drafts start deselected. Keep an explicit user selection so
        // the review screen can offer a deliberate "import anyway" action.
        val selected = drafts.filter { it.isSelected }
        if (selected.isEmpty()) return Result.failure(AppException.BulkTransactionException(BulkTransactionReason.NOTHING_SELECTED))
        if (selected.any { it.amount <= 0 }) return Result.failure(AppException.BulkTransactionException(BulkTransactionReason.INVALID_AMOUNT))
        // Daily gate — check the complete today's batch once so each item cannot
        // reuse the same remaining allowance independently.
        if (!force) {
            val todayIso = LocalDate.now(clock).toString()
            val todayExpense = try {
                selected.filter {
                    it.type == TransactionType.EXPENSE &&
                        it.category != TransactionCategory.BILLS &&
                        it.date.take(10) == todayIso
                }.fold(0L) { total, draft -> Math.addExact(total, draft.amount) }
            } catch (e: ArithmeticException) {
                return Result.failure(AppException.DataException(cause = e))
            }
            if (todayExpense > 0L) {
                val dailyCheck = checkDailyBudget(todayExpense, todayIso)
                if (dailyCheck.isFailure) {
                    val error = dailyCheck.exceptionOrNull()
                    if (error is AppException.DailyBudgetExceededException) return Result.failure(error)
                    return Result.failure(error ?: AppException.DataException())
                }
            }
        }
        // Saldo check — sum of expenses vs wallet balance
        val totalExpense = try {
            selected.filter { it.type != TransactionType.INCOME }
                .fold(0L) { total, draft -> Math.addExact(total, draft.amount) }
        } catch (e: ArithmeticException) {
            return Result.failure(AppException.DataException(cause = e))
        }
        if (totalExpense > 0) {
            val wallet = walletRepository.getWallets().getOrNull()?.find { it.id == walletId }
            if (wallet != null && wallet.balance < totalExpense) {
                return Result.failure(
                    AppException.InsufficientBalanceException(
                        availableBalance = wallet.balance,
                        requestedAmount = totalExpense
                    )
                )
            }
        }
        val requests = selected.map { d ->
            CreateTransactionRequest(
                amount = d.amount,
                type = d.type,
                category = d.category,
                merchant = d.merchant,
                date = d.date,
                walletId = walletId
            )
        }
        return try {
            transactionRepository.bulkCreate(requests)
        } catch (e: ArithmeticException) {
            Result.failure(AppException.DataException(cause = e))
        }
    }
}
