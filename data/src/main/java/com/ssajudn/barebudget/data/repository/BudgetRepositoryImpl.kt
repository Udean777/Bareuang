package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.data.datasource.local.BudgetLocalDataSource
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val local: BudgetLocalDataSource
) : BudgetRepository {

    override suspend fun setBudget(monthlyLimit: Long, monthYear: String): Result<Boolean> =
        local.setBudget(monthlyLimit, monthYear)

    override suspend fun getMonthlyBudget(monthYear: String): Result<Long> =
        local.getMonthlyBudget(monthYear)

    override fun getCategoryBudgets(monthYear: String): kotlinx.coroutines.flow.Flow<List<com.ssajudn.barebudget.domain.model.CategoryBudget>> =
        local.observeCategoryBudgets(monthYear)

    override suspend fun setCategoryBudget(
        category: com.ssajudn.barebudget.domain.model.TransactionCategory,
        limitAmount: Long,
        monthYear: String
    ): Result<Boolean> =
        local.setCategoryBudget(category, limitAmount, monthYear)

    override suspend fun deleteCategoryBudget(
        category: com.ssajudn.barebudget.domain.model.TransactionCategory,
        monthYear: String
    ): Result<Boolean> =
        local.deleteCategoryBudget(category, monthYear)
}
