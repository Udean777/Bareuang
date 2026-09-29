package com.ssajudn.bareuang.ui.transaction

import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionEntryTemplate
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.usecase.TransactionEntryPreferencesUseCase
import com.ssajudn.bareuang.domain.utils.DateUtils

internal data class TransactionEntrySelection(
    val type: TransactionType,
    val walletId: String?,
    val destinationWalletId: String?,
    val amount: Long?,
    val merchant: String,
    val category: TransactionCategory,
    val notes: String,
    val recurringInterval: RecurringInterval,
)

internal data class TransactionEntrySubmission(
    val request: CreateTransactionRequest,
    val favoriteTemplate: TransactionEntryTemplate?,
)

/** Maps reusable transaction data to form state and maps a completed form to domain requests. */
internal object TransactionEntryFormMapper {
    fun fromFavorite(
        state: AddTransactionUiState,
        template: TransactionEntryTemplate,
        preferences: TransactionEntryPreferencesUseCase,
    ): TransactionEntrySelection {
        val walletId = state.wallets.firstOrNull { it.id == template.walletId }?.id
            ?: preferences.preferredWallet(state.wallets, template.type)
        val destinationWalletId = state.wallets.firstOrNull {
            it.id == template.destinationWalletId && it.id != walletId
        }?.id ?: preferences.preferredDestinationWallet(state.wallets, walletId)
        return TransactionEntrySelection(
            type = template.type,
            walletId = walletId,
            destinationWalletId = destinationWalletId,
            amount = template.amount,
            merchant = template.merchant,
            category = preferences.normalizeCategory(template.type, template.category),
            notes = template.notes,
            recurringInterval = template.recurringInterval,
        )
    }

    fun fromRecent(
        state: AddTransactionUiState,
        transaction: Transaction,
        preferences: TransactionEntryPreferencesUseCase,
    ): TransactionEntrySelection {
        val walletId = state.wallets.firstOrNull { it.id == transaction.walletId }?.id
            ?: preferences.preferredWallet(state.wallets, transaction.type)
        val destinationWalletId = state.wallets.firstOrNull {
            it.id == transaction.toWalletId && it.id != walletId
        }?.id ?: preferences.preferredDestinationWallet(state.wallets, walletId)
        val recurringInterval = when {
            transaction.recurringInterval != RecurringInterval.NONE -> transaction.recurringInterval
            transaction.parentRecurringId != null -> state.recurringIntervalsById[transaction.parentRecurringId]
                ?: RecurringInterval.NONE
            else -> RecurringInterval.NONE
        }
        return TransactionEntrySelection(
            type = transaction.type,
            walletId = walletId,
            destinationWalletId = destinationWalletId,
            amount = transaction.amount,
            merchant = transaction.merchant.orEmpty(),
            category = preferences.normalizeCategory(transaction.type, transaction.category),
            notes = transaction.notes.orEmpty(),
            recurringInterval = recurringInterval,
        )
    }

    fun applySelection(
        state: AddTransactionUiState,
        selection: TransactionEntrySelection,
    ): AddTransactionUiState = state.copy(
        transactionType = selection.type,
        selectedWalletId = selection.walletId,
        selectedToWalletId = selection.destinationWalletId,
        rawAmount = selection.amount?.toString().orEmpty(),
        parsedAmount = selection.amount ?: 0L,
        merchant = selection.merchant.take(100),
        selectedCategory = selection.category,
        date = DateUtils.getCurrentDateISO(),
        notes = selection.notes.take(500),
        isRecurring = selection.recurringInterval != RecurringInterval.NONE,
        recurringInterval = selection.recurringInterval.takeIf { it != RecurringInterval.NONE }
            ?: RecurringInterval.MONTHLY,
        saveAsFavorite = false,
        includeFavoriteAmount = false,
        errorMessage = null,
        validationError = null,
    )

    fun submission(state: AddTransactionUiState, favoriteTemplateId: String): TransactionEntrySubmission {
        val sourceWalletName = state.wallets.find { it.id == state.selectedWalletId }?.name.orEmpty()
        val targetWalletName = state.wallets.find { it.id == state.selectedToWalletId }?.name.orEmpty()
        val defaultMerchant = if (state.transactionType == TransactionType.TRANSFER) {
            if (sourceWalletName.isNotBlank() && targetWalletName.isNotBlank()) {
                "$sourceWalletName → $targetWalletName"
            } else {
                state.selectedCategory.name
            }
        } else {
            state.selectedCategory.name
        }
        val merchant = state.merchant.ifBlank { defaultMerchant }
        val request = CreateTransactionRequest(
            amount = state.parsedAmount,
            type = state.transactionType,
            walletId = state.selectedWalletId,
            toWalletId = state.selectedToWalletId.takeIf { state.transactionType == TransactionType.TRANSFER },
            category = state.selectedCategory,
            merchant = merchant,
            date = state.date,
            notes = state.notes,
            recurringInterval = if (state.isRecurring) state.recurringInterval else RecurringInterval.NONE,
        )
        val favorite = if (state.saveAsFavorite && state.selectedWalletId != null) {
            TransactionEntryTemplate(
                id = favoriteTemplateId,
                name = merchant.trim().take(48),
                type = state.transactionType,
                walletId = state.selectedWalletId,
                destinationWalletId = state.selectedToWalletId,
                category = state.selectedCategory,
                merchant = merchant,
                notes = state.notes,
                amount = state.parsedAmount.takeIf { state.includeFavoriteAmount },
                recurringInterval = if (state.isRecurring) state.recurringInterval else RecurringInterval.NONE,
            )
        } else null
        return TransactionEntrySubmission(request, favorite)
    }
}
