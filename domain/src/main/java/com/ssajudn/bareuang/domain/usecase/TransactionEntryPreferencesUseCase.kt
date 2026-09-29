package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionEntryTemplate
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.port.TransactionEntryPreferencesPort
import javax.inject.Inject

/** Owns preference-backed defaults and reusable templates for transaction entry. */
class TransactionEntryPreferencesUseCase @Inject constructor(
    private val preferences: TransactionEntryPreferencesPort,
    private val categoryPolicy: TransactionCategoryPolicy
) {
    fun favoriteTemplates(): List<TransactionEntryTemplate> = preferences.favoriteTemplates()

    fun preferredWallet(wallets: List<Wallet>, type: TransactionType): String? {
        val preferredId = preferences.lastWalletId(type)
        return wallets.firstOrNull { it.id == preferredId }?.id ?: wallets.firstOrNull()?.id
    }

    fun preferredDestinationWallet(wallets: List<Wallet>, sourceWalletId: String?): String? {
        val preferredId = preferences.lastDestinationWalletId()
        return wallets.firstOrNull { it.id == preferredId && it.id != sourceWalletId }?.id
            ?: wallets.firstOrNull { it.id != sourceWalletId }?.id
            ?: sourceWalletId
    }

    fun preferredCategory(type: TransactionType): TransactionCategory {
        if (type == TransactionType.TRANSFER) return TransactionCategory.TRANSFER
        return preferences.lastCategory(type)?.let { categoryPolicy.normalize(type, it) }
            ?: categoryPolicy.defaultFor(type)
    }

    fun normalizeCategory(type: TransactionType, category: TransactionCategory): TransactionCategory =
        categoryPolicy.normalize(type, category)

    fun rememberEntry(
        type: TransactionType,
        walletId: String,
        destinationWalletId: String?,
        category: TransactionCategory
    ) = preferences.saveLastEntry(type, walletId, destinationWalletId, category)

    fun saveFavoriteTemplate(template: TransactionEntryTemplate) =
        preferences.saveFavoriteTemplate(template)

    fun deleteFavoriteTemplate(templateId: String) = preferences.deleteFavoriteTemplate(templateId)
}
