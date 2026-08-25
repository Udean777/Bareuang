package com.ssajudn.bareuang.domain.repository

interface BudgetRepository {
    suspend fun setBudget(monthlyLimit: Long, monthYear: String = ""): Result<Boolean>

    suspend fun getMonthlyBudget(monthYear: String = ""): Result<Long>

    fun getCategoryBudgets(monthYear: String = ""): kotlinx.coroutines.flow.Flow<List<com.ssajudn.bareuang.domain.model.CategoryBudget>>

    suspend fun setCategoryBudget(category: com.ssajudn.bareuang.domain.model.TransactionCategory, limitAmount: Long, monthYear: String = ""): Result<Boolean>

    suspend fun deleteCategoryBudget(category: com.ssajudn.bareuang.domain.model.TransactionCategory, monthYear: String = ""): Result<Boolean>
}
