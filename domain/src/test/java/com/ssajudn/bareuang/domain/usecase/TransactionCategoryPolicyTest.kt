package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionCategoryPolicyTest {
    private val policy = TransactionCategoryPolicy()

    @Test
    fun `default category follows transaction type`() {
        assertEquals(TransactionCategory.SALARY, policy.defaultFor(TransactionType.INCOME))
        assertEquals(TransactionCategory.FOOD, policy.defaultFor(TransactionType.EXPENSE))
        assertEquals(TransactionCategory.TRANSFER, policy.defaultFor(TransactionType.TRANSFER))
    }

    @Test
    fun `income category is preserved for income`() {
        assertEquals(
            TransactionCategory.INVESTMENT,
            policy.normalize(TransactionType.INCOME, TransactionCategory.INVESTMENT)
        )
    }

    @Test
    fun `non income category falls back for income`() {
        assertEquals(
            TransactionCategory.SALARY,
            policy.normalize(TransactionType.INCOME, TransactionCategory.FOOD)
        )
    }

    @Test
    fun `income and transfer categories fall back for expense`() {
        assertEquals(
            TransactionCategory.FOOD,
            policy.normalize(TransactionType.EXPENSE, TransactionCategory.BONUS)
        )
        assertEquals(
            TransactionCategory.FOOD,
            policy.normalize(TransactionType.EXPENSE, TransactionCategory.TRANSFER)
        )
    }

    @Test
    fun `transfer always uses transfer category`() {
        assertEquals(
            TransactionCategory.TRANSFER,
            policy.normalize(TransactionType.TRANSFER, TransactionCategory.FOOD)
        )
    }
}
