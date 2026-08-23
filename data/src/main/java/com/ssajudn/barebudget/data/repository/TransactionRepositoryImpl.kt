package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.domain.model.CreateTransactionRequest
import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.data.datasource.local.TransactionLocalDataSource
import com.ssajudn.barebudget.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val local: TransactionLocalDataSource
) : TransactionRepository {

    override suspend fun getTransactions(category: String?, page: Int, limit: Int): Result<List<Transaction>> =
        local.getTransactions(category, page, limit)

    override suspend fun createTransaction(request: CreateTransactionRequest): Result<Transaction> =
        local.createTransaction(request)

    override suspend fun deleteTransaction(id: String): Result<Boolean> =
        local.deleteTransaction(id)

    override fun observeTransactions(): Flow<List<Transaction>> =
        local.observeTransactions()
}
