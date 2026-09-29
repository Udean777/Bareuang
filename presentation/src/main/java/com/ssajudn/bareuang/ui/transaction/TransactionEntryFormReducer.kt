package com.ssajudn.bareuang.ui.transaction

import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType

/** Pure form-state transitions; asynchronous loading and transaction workflows stay in the ViewModel. */
internal object TransactionEntryFormReducer {
    fun transactionType(
        state: AddTransactionUiState,
        type: TransactionType,
        walletId: String?,
        destinationWalletId: String?,
        category: TransactionCategory,
    ) = state.copy(
        transactionType = type,
        selectedWalletId = walletId,
        selectedCategory = category,
        selectedToWalletId = destinationWalletId,
    )

    fun selectWallet(state: AddTransactionUiState, walletId: String): AddTransactionUiState {
        var destinationWalletId = state.selectedToWalletId

        if (state.transactionType == TransactionType.TRANSFER && walletId == state.selectedToWalletId) {
            val previousSource = state.selectedWalletId
            val alternateWalletId = if (
                previousSource != null && previousSource != walletId &&
                state.wallets.any { it.id == previousSource }
            ) {
                previousSource
            } else {
                state.wallets.firstOrNull { it.id != walletId }?.id
            }
            if (alternateWalletId != null) destinationWalletId = alternateWalletId
        }

        return state.copy(selectedWalletId = walletId, selectedToWalletId = destinationWalletId)
    }

    fun selectDestinationWallet(state: AddTransactionUiState, walletId: String): AddTransactionUiState {
        var sourceWalletId = state.selectedWalletId

        if (walletId == state.selectedWalletId) {
            val previousDestination = state.selectedToWalletId
            val alternateWalletId = if (
                previousDestination != null && previousDestination != walletId &&
                state.wallets.any { it.id == previousDestination }
            ) {
                previousDestination
            } else {
                state.wallets.firstOrNull { it.id != walletId }?.id
            }
            if (alternateWalletId != null) sourceWalletId = alternateWalletId
        }

        return state.copy(selectedWalletId = sourceWalletId, selectedToWalletId = walletId)
    }

    fun swapWallets(state: AddTransactionUiState): AddTransactionUiState {
        val source = state.selectedWalletId
        val destination = state.selectedToWalletId
        return if (source != null && destination != null && source != destination) {
            state.copy(selectedWalletId = destination, selectedToWalletId = source)
        } else {
            state
        }
    }

    fun amount(state: AddTransactionUiState, input: String): AddTransactionUiState {
        val digitsOnly = input.filter(Char::isDigit).take(12)
        return state.copy(rawAmount = digitsOnly, parsedAmount = digitsOnly.toLongOrNull() ?: 0L)
    }

    fun merchant(state: AddTransactionUiState, value: String) = state.copy(merchant = value.take(100))

    fun category(state: AddTransactionUiState, value: TransactionCategory) =
        state.copy(selectedCategory = value)

    fun date(state: AddTransactionUiState, value: String) = state.copy(date = value)

    fun notes(state: AddTransactionUiState, value: String) = state.copy(notes = value.take(500))

    fun recurring(state: AddTransactionUiState, enabled: Boolean) = state.copy(isRecurring = enabled)

    fun recurringInterval(state: AddTransactionUiState, interval: RecurringInterval) =
        state.copy(recurringInterval = interval)

    fun saveAsFavorite(state: AddTransactionUiState, enabled: Boolean) = state.copy(
        saveAsFavorite = enabled,
        includeFavoriteAmount = if (enabled) state.includeFavoriteAmount else false,
    )

    fun includeFavoriteAmount(state: AddTransactionUiState, enabled: Boolean) =
        state.copy(includeFavoriteAmount = enabled)
}
