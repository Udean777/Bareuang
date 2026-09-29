package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.error.TransactionValidationReason
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CheckTransactionEntryUseCaseTest {
    private val entryBudget = mockk<TransactionEntryBudgetUseCase>()
    private val useCase = CheckTransactionEntryUseCase(
        validateTransaction = ValidateTransactionUseCase(),
        entryBudget = entryBudget,
    )
    private val wallets = listOf(Wallet(id = "wallet", name = "Cash", balance = 100_000L))

    @Test
    fun `rejects invalid transaction before checking daily budget`() = runTest {
        val result = useCase(
            type = TransactionType.EXPENSE,
            amount = 0,
            date = "2026-09-28",
            category = TransactionCategory.FOOD,
            sourceWalletId = "wallet",
            targetWalletId = null,
            wallets = wallets,
        ).getOrThrow()

        assertEquals(
            TransactionEntryCheck.Rejected(TransactionValidationReason.INVALID_AMOUNT),
            result,
        )
        coVerify(exactly = 0) { entryBudget.checkDailyBudget(any(), any(), any()) }
    }

    @Test
    fun `requires explicit override when daily budget is exceeded`() = runTest {
        val error = AppException.DailyBudgetExceededException(remainingAmount = 10_000L, requestedAmount = 50_000L)
        coEvery { entryBudget.checkDailyBudget(50_000L, "2026-09-28", TransactionCategory.FOOD) } returns
            Result.failure(error)

        val result = useCase(
            type = TransactionType.EXPENSE,
            amount = 50_000L,
            date = "2026-09-28",
            category = TransactionCategory.FOOD,
            sourceWalletId = "wallet",
            targetWalletId = null,
            wallets = wallets,
        ).getOrThrow()

        assertSame(error, (result as TransactionEntryCheck.RequiresDailyBudgetOverride).error)
    }

    @Test
    fun `skips daily budget check for income`() = runTest {
        val result = useCase(
            type = TransactionType.INCOME,
            amount = 50_000L,
            date = "2026-09-28",
            category = TransactionCategory.SALARY,
            sourceWalletId = "wallet",
            targetWalletId = null,
            wallets = wallets,
        ).getOrThrow()

        assertEquals(TransactionEntryCheck.Ready, result)
        coVerify(exactly = 0) { entryBudget.checkDailyBudget(any(), any(), any()) }
    }

    @Test
    fun `returns unexpected daily budget failure without converting it to override`() = runTest {
        val failure = AppException.DataException(cause = IllegalStateException("storage failure"))
        coEvery { entryBudget.checkDailyBudget(any(), any(), any()) } returns Result.failure(failure)

        val result = useCase(
            type = TransactionType.EXPENSE,
            amount = 50_000L,
            date = "2026-09-28",
            category = TransactionCategory.FOOD,
            sourceWalletId = "wallet",
            targetWalletId = null,
            wallets = wallets,
        )

        assertSame(failure, result.exceptionOrNull())
    }
}
