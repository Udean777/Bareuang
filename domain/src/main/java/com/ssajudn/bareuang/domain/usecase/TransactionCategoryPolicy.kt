package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import javax.inject.Inject

class TransactionCategoryPolicy @Inject constructor() {
    fun defaultFor(type: TransactionType): TransactionCategory = when (type) {
        TransactionType.INCOME -> TransactionCategory.SALARY
        TransactionType.EXPENSE -> TransactionCategory.FOOD
        TransactionType.TRANSFER -> TransactionCategory.TRANSFER
    }

    fun normalize(type: TransactionType, category: TransactionCategory): TransactionCategory {
        val valid = when (type) {
            TransactionType.INCOME -> category in incomeCategories
            TransactionType.EXPENSE -> category != TransactionCategory.TRANSFER && category !in incomeCategories
            TransactionType.TRANSFER -> category == TransactionCategory.TRANSFER
        }
        return if (valid) category else defaultFor(type)
    }

    private companion object {
        val incomeCategories = setOf(
            TransactionCategory.SALARY,
            TransactionCategory.BONUS,
            TransactionCategory.INVESTMENT
        )
    }
}
