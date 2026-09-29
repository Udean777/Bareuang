package com.ssajudn.bareuang.ui.transaction

import com.ssajudn.bareuang.domain.model.CategoryBudget
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.CheckTransactionEntryUseCase
import com.ssajudn.bareuang.domain.usecase.ObserveTransactionEntryHistoryUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

internal sealed interface TransactionEntryDataUpdate {
    data class Wallets(val value: List<Wallet>) : TransactionEntryDataUpdate
    data class CategoryBudgets(val value: List<CategoryBudget>) : TransactionEntryDataUpdate
    data class RecentHistory(
        val transactions: List<Transaction>,
        val recurringIntervalsById: Map<String, RecurringInterval>,
    ) : TransactionEntryDataUpdate
    data class MonthlyBudgetStatus(val isMissing: Boolean) : TransactionEntryDataUpdate
    data class Failure(val cause: Throwable) : TransactionEntryDataUpdate
}

/** Owns the reference-data streams used to populate the transaction entry form. */
@ViewModelScoped
class TransactionEntryDataCoordinator @Inject constructor(
    private val walletRepository: WalletRepository,
    private val budgetRepository: BudgetRepository,
    private val checkTransactionEntry: CheckTransactionEntryUseCase,
    private val observeEntryHistory: ObserveTransactionEntryHistoryUseCase,
) {
    internal fun observe(): Flow<TransactionEntryDataUpdate> = merge(
        observeWallets(),
        budgetRepository.getCategoryBudgets("")
            .map { TransactionEntryDataUpdate.CategoryBudgets(it) as TransactionEntryDataUpdate }
            .catch { emit(TransactionEntryDataUpdate.Failure(it)) },
        observeEntryHistory().map { history ->
            TransactionEntryDataUpdate.RecentHistory(
                transactions = history.recentTransactions,
                recurringIntervalsById = history.recurringIntervalsById,
            ) as TransactionEntryDataUpdate
        }.catch { emit(TransactionEntryDataUpdate.Failure(it)) },
        observeMonthlyBudgetStatus()
            .catch { emit(TransactionEntryDataUpdate.Failure(it)) },
    )

    private fun observeWallets(): Flow<TransactionEntryDataUpdate> = flow {
        walletRepository.getWallets().fold(
            onSuccess = { wallets ->
                if (wallets.isNotEmpty()) emit(TransactionEntryDataUpdate.Wallets(wallets))
            },
            onFailure = { emit(TransactionEntryDataUpdate.Failure(it)) },
        )

        emitAll(walletRepository.observeWallets().map(TransactionEntryDataUpdate::Wallets))
    }.catch { emit(TransactionEntryDataUpdate.Failure(it)) }

    private fun observeMonthlyBudgetStatus(): Flow<TransactionEntryDataUpdate> = flow {
        emit(TransactionEntryDataUpdate.MonthlyBudgetStatus(
            isMissing = !checkTransactionEntry.hasMonthlyBudget(),
        ))
    }
}
