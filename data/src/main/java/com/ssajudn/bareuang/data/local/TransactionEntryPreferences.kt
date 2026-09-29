package com.ssajudn.bareuang.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionEntryTemplate
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.port.TransactionEntryPreferencesPort
import dagger.hilt.android.qualifiers.ApplicationContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionEntryPreferences @Inject constructor(
    @ApplicationContext context: Context
) : TransactionEntryPreferencesPort {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("bareuang_transaction_entry", Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun lastWalletId(type: TransactionType): String? =
        preferences.getString(walletKey(type), null)

    override fun lastDestinationWalletId(): String? =
        preferences.getString(KEY_TRANSFER_DESTINATION_WALLET, null)

    override fun lastCategory(type: TransactionType): TransactionCategory? =
        preferences.getString(categoryKey(type), null)
            ?.let { stored -> TransactionCategory.entries.firstOrNull { it.name == stored } }

    override fun saveLastEntry(
        type: TransactionType,
        walletId: String,
        destinationWalletId: String?,
        category: TransactionCategory
    ) {
        preferences.edit {
            putString(walletKey(type), walletId)
            putString(categoryKey(type), category.name)
            if (type == TransactionType.TRANSFER && destinationWalletId != null) {
                putString(KEY_TRANSFER_DESTINATION_WALLET, destinationWalletId)
            }
        }
    }

    override fun favoriteTemplates(): List<TransactionEntryTemplate> = runCatching {
        val raw = preferences.getString(KEY_FAVORITE_TEMPLATES, null) ?: return emptyList()
        val type = object : TypeToken<List<TransactionEntryTemplate>>() {}.type
        gson.fromJson<List<TransactionEntryTemplate>>(raw, type).orEmpty()
    }.getOrDefault(emptyList())

    override fun saveFavoriteTemplate(template: TransactionEntryTemplate) {
        val templates = favoriteTemplates()
            .filterNot { it.id == template.id }
            .plus(template)
            .takeLast(MAX_FAVORITE_TEMPLATES)
        preferences.edit { putString(KEY_FAVORITE_TEMPLATES, gson.toJson(templates)) }
    }

    override fun deleteFavoriteTemplate(templateId: String) {
        val templates = favoriteTemplates().filterNot { it.id == templateId }
        preferences.edit { putString(KEY_FAVORITE_TEMPLATES, gson.toJson(templates)) }
    }

    private fun walletKey(type: TransactionType): String = "last_wallet_${type.name.lowercase()}"

    private fun categoryKey(type: TransactionType): String = "last_category_${type.name.lowercase()}"

    private companion object {
        const val KEY_TRANSFER_DESTINATION_WALLET = "last_transfer_destination_wallet"
        const val KEY_FAVORITE_TEMPLATES = "favorite_transaction_templates"
        const val MAX_FAVORITE_TEMPLATES = 20
    }
}
