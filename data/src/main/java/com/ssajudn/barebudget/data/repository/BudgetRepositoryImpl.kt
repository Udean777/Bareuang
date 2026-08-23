package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.domain.model.DashboardSummary
import com.ssajudn.barebudget.data.datasource.local.BudgetLocalDataSource
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val local: BudgetLocalDataSource
) : BudgetRepository {

    @Suppress("DEPRECATION")
    override suspend fun getDashboardSummary(): Result<DashboardSummary> =
        local.getDashboardSummary()

    override suspend fun setBudget(monthlyLimit: Long, monthYear: String): Result<Boolean> =
        local.setBudget(monthlyLimit, monthYear)

    override suspend fun getMonthlyBudget(monthYear: String): Result<Long> =
        local.getMonthlyBudget(monthYear)
}
