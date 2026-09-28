package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Clock

class BulkCreateTransactionsUseCaseTest {
    @Test
    fun `an explicitly selected duplicate is included in batch import`() = runTest {
        val transactions = mockk<TransactionRepository>(relaxed = true)
        val wallets = mockk<WalletRepository>()
        val daily = mockk<CheckDailyBudgetUseCase>()
        coEvery { wallets.getWallets() } returns Result.success(listOf(Wallet("w1", "Cash", 500_000L)))
        coEvery { transactions.bulkCreate(any()) } returns Result.success(1)
        val useCase = BulkCreateTransactionsUseCase(transactions, wallets, daily, Clock.systemDefaultZone())
        val duplicate = ImportDraft(
            id = "duplicate",
            amount = 25_000L,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.FOOD,
            merchant = "Coffee",
            date = "2020-01-01",
            rawLine = "2020-01-01,Coffee,25000",
            isSelected = true,
            isDuplicate = true,
        )

        val result = useCase(listOf(duplicate), "w1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow())
        coVerify(exactly = 1) { transactions.bulkCreate(match { it.size == 1 }) }
    }

    @Test
    fun `import works without monthly budget and daily gate checks full batch once`() = runTest {
        val transactions = mockk<TransactionRepository>(relaxed = true)
        val wallets = mockk<WalletRepository>()
        val daily = mockk<CheckDailyBudgetUseCase>()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        coEvery { wallets.getWallets() } returns Result.success(listOf(Wallet("w1", "Cash", 500_000L)))
        coEvery { daily.invoke(any(), any(), any()) } returns Result.success(Unit)
        coEvery { transactions.bulkCreate(any()) } returns Result.success(2)

        val useCase = BulkCreateTransactionsUseCase(
            transactions,
            wallets,
            daily,
            Clock.systemDefaultZone(),
        )
        val drafts = listOf(
            ImportDraft("1", 40_000L, TransactionType.EXPENSE, TransactionCategory.FOOD, "A", today, today),
            ImportDraft("2", 60_000L, TransactionType.EXPENSE, TransactionCategory.TRANSPORT, "B", today, today)
        )

        assertTrue(useCase(drafts, "w1").isSuccess)
        coVerify(exactly = 1) { daily.invoke(100_000L, today, TransactionCategory.OTHER) }
    }
}
