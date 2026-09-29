package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.repository.TransactionQueryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class TransactionEntryHistory(
    val recentTransactions: List<Transaction>,
    val recurringIntervalsById: Map<String, RecurringInterval>
)

class ObserveTransactionEntryHistoryUseCase @Inject constructor(
    private val transactionRepository: TransactionQueryRepository
) {
    operator fun invoke(): Flow<TransactionEntryHistory> =
        transactionRepository.observeTransactions().map { transactions ->
            TransactionEntryHistory(
                recentTransactions = transactions
                    .asSequence()
                    .filterNot { it.isRecurringParent }
                    .sortedWith(
                        compareByDescending<Transaction> { it.date }
                            .thenByDescending { it.createdAt.orEmpty() }
                    )
                    .take(5)
                    .toList(),
                recurringIntervalsById = transactions
                    .filter { it.isRecurringParent && it.id != null }
                    .associate { it.id!! to it.recurringInterval }
            )
        }
}
