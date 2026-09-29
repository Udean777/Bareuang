package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.TransactionCategory
import javax.inject.Inject

/** Budget decisions needed by the transaction-entry flow. */
class TransactionEntryBudgetUseCase @Inject constructor(
    private val hasMonthlyBudgetUseCase: HasMonthlyBudgetUseCase,
    private val checkDailyBudgetUseCase: CheckDailyBudgetUseCase,
) {
    suspend fun hasMonthlyBudget(): Boolean = hasMonthlyBudgetUseCase()

    suspend fun checkDailyBudget(
        amount: Long,
        date: String,
        category: TransactionCategory,
    ): Result<Unit> = checkDailyBudgetUseCase(amount, date, category)
}
