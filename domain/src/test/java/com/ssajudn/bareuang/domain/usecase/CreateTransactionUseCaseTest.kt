package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.DashboardTransactionData
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.error.AppException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class CreateTransactionUseCaseTest {
    @Test
    fun `rejects insufficient current wallet balance before repository write`() = runBlocking {
        val transactionRepository = mockk<TransactionRepository>(relaxed = true)
        val walletRepository = mockk<WalletRepository>()
        coEvery { walletRepository.getWallets() } returns Result.success(
            listOf(Wallet(id = "wallet-1", name = "Cash", balance = 10_000L))
        )
        val request = CreateTransactionRequest(
            amount = 25_000L,
            walletId = "wallet-1",
            merchant = "Coffee",
            date = "2026-09-03",
            category = com.ssajudn.bareuang.domain.model.TransactionCategory.FOOD
        )

        val result = CreateTransactionUseCase(
            transactionRepository,
            walletRepository,
            ValidateTransactionUseCase()
        )(request)

        assertTrue(result.exceptionOrNull() is AppException.InsufficientBalanceException)
        coVerify(exactly = 0) { transactionRepository.createTransaction(any()) }
    }

    @Test
    fun `delegates request to transaction repository`() = runBlocking {
        val expected = Transaction(id = "tx-1", amount = 25_000L, category = com.ssajudn.bareuang.domain.model.TransactionCategory.FOOD, date = "2026-09-03")
        var received: CreateTransactionRequest? = null
        val repository = object : TransactionRepository {
            override suspend fun getDashboardTransactions(monthYear: String, todayIso: String) =
                Result.success(
                    DashboardTransactionData(
                        totalSpent = 0L,
                        todaySpent = 0L,
                        topCategories = emptyList(),
                        recentTransactions = emptyList(),
                        recurringTransactions = emptyList(),
                    )
                )
            override suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> {
                received = request
                return Result.success(expected)
            }
            override suspend fun getTransactions(category: String?, page: Int, limit: Int) = Result.success(emptyList<Transaction>())
            override suspend fun bulkCreate(requests: List<CreateTransactionRequest>) = Result.success(requests.size)
            override suspend fun deleteTransaction(id: String) = Result.success(true)
            override fun observeTransactions(): Flow<List<Transaction>> = emptyFlow()
        }
        val request = CreateTransactionRequest(
            amount = 25_000L,
            category = com.ssajudn.bareuang.domain.model.TransactionCategory.FOOD,
            merchant = "Test",
            date = "2026-09-03",
            walletId = "wallet-1",
        )
        val wallets = object : WalletRepository {
            override suspend fun getWallets() = Result.success(listOf(Wallet(id = "wallet-1", name = "Cash", balance = 100_000L)))
            override suspend fun createWallet(request: com.ssajudn.bareuang.domain.model.CreateWalletRequest): Result<Wallet> = error("unused")
            override suspend fun updateWallet(wallet: Wallet): Result<Unit> = error("unused")
            override suspend fun deleteWallet(id: String): Result<Boolean> = error("unused")
            override fun observeWallets(): Flow<List<Wallet>> = emptyFlow()
        }

        val result = CreateTransactionUseCase(repository, wallets, ValidateTransactionUseCase())(request)

        assertEquals(expected, result.getOrThrow())
        assertEquals(request, received)
    }
}
