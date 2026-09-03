package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private class AnalyticsTxRepo(private val result: Result<List<Transaction>>) : TransactionRepository {
    var requestedLimit: Int = 0
    override suspend fun getTransactions(category: String?, page: Int, limit: Int): Result<List<Transaction>> {
        requestedLimit = limit
        return result
    }
    override suspend fun createTransaction(request: CreateTransactionRequest) = Result.failure<Transaction>(UnsupportedOperationException())
    override suspend fun bulkCreate(requests: List<CreateTransactionRequest>) = Result.success(0)
    override suspend fun deleteTransaction(id: String) = Result.success(false)
    override fun observeTransactions(): Flow<List<Transaction>> = flowOf(emptyList())
}

class GetCashflowAnalyticsUseCaseTest {
    @Test
    fun `cashflow includes transactions beyond previous 500 row cap`() = runTest {
        val month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)
        val transactions = (1..600).map { index ->
            Transaction(
                id = index.toString(), amount = 1L, type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD, date = "$month-01"
            )
        }
        val repo = AnalyticsTxRepo(Result.success(transactions))
        val result = GetCashflowAnalyticsUseCase(repo)()

        assertTrue(result.isSuccess)
        assertEquals(600L, result.getOrThrow().last().expense)
        assertEquals(Int.MAX_VALUE, repo.requestedLimit)
    }

    @Test
    fun `cashflow propagates repository failure`() = runTest {
        val failure = AppException.DataException("database unavailable")
        val result = GetCashflowAnalyticsUseCase(AnalyticsTxRepo(Result.failure(failure)))()

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
    }
}
