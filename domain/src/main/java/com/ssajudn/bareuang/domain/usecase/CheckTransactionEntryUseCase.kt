package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.error.TransactionValidationReason
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import javax.inject.Inject

sealed interface TransactionEntryCheck {
    data object Ready : TransactionEntryCheck
    data class Rejected(val reason: TransactionValidationReason) : TransactionEntryCheck
    data class RequiresDailyBudgetOverride(
        val error: AppException.DailyBudgetExceededException,
    ) : TransactionEntryCheck
}

/** Applies entry validation and the daily-budget confirmation policy before saving. */
class CheckTransactionEntryUseCase @Inject constructor(
    private val validateTransaction: ValidateTransactionUseCase,
    private val entryBudget: TransactionEntryBudgetUseCase,
) {
    suspend fun hasMonthlyBudget(): Boolean = entryBudget.hasMonthlyBudget()

    suspend operator fun invoke(
        type: TransactionType,
        amount: Long,
        date: String,
        category: TransactionCategory,
        sourceWalletId: String?,
        targetWalletId: String?,
        wallets: List<Wallet>,
    ): Result<TransactionEntryCheck> {
        val validation = validateTransaction(
            type = type,
            amount = amount,
            sourceWalletId = sourceWalletId,
            targetWalletId = targetWalletId,
            wallets = wallets,
        )
        if (validation != null) {
            return Result.success(TransactionEntryCheck.Rejected(validation))
        }

        if (type != TransactionType.EXPENSE) return Result.success(TransactionEntryCheck.Ready)

        return entryBudget.checkDailyBudget(amount, date, category).fold(
            onSuccess = { Result.success(TransactionEntryCheck.Ready) },
            onFailure = { error ->
                if (error is AppException.DailyBudgetExceededException) {
                    Result.success(TransactionEntryCheck.RequiresDailyBudgetOverride(error))
                } else {
                    Result.failure(error)
                }
            },
        )
    }
}
