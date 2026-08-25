package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.repository.BudgetRepository
import javax.inject.Inject

/**
 * Budget gate: income/expense transactions require a budget for the current
 * month; transfers are exempt. Centralizes the "current month = blank key"
 * convention of [BudgetRepository].
 */
class HasMonthlyBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(monthYear: String = ""): Boolean =
        budgetRepository.getMonthlyBudget(monthYear).getOrDefault(0L) > 0L
}
