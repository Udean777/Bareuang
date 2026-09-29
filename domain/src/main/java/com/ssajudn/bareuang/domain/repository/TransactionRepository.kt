package com.ssajudn.bareuang.domain.repository

import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.DashboardTransactionData
import kotlinx.coroutines.flow.Flow

/**
 * Read-side transaction capabilities implemented by `:data`.
 *
 * `Result` carries expected operation failures. Coroutine cancellation is not
 * an operation failure and must be rethrown by implementations. Observation
 * flows emit domain models and propagate upstream failures to their collector.
 */
interface TransactionQueryRepository {
    suspend fun getDashboardTransactions(monthYear: String, todayIso: String): Result<DashboardTransactionData>
    suspend fun getTransactions(category: String? = null, page: Int = 1, limit: Int = 50): Result<List<Transaction>>
    /** Full dataset for calculations/import dedup; UI lists must use pagination. */
    suspend fun getAllTransactions(): Result<List<Transaction>> = getTransactions(page = 1, limit = Int.MAX_VALUE)
    fun observeTransactions(): Flow<List<Transaction>>
}

interface TransactionCommandRepository {
    suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction>
    suspend fun bulkCreate(requests: List<CreateTransactionRequest>): Result<Int>
    suspend fun deleteTransaction(id: String): Result<Boolean>
}

/** Combined contract implemented by the transaction adapter; application clients should depend on the narrower port they use. */
interface TransactionRepository : TransactionQueryRepository, TransactionCommandRepository
