package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionEntryTemplate
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.error.AppException
import javax.inject.Inject

data class TransactionEntrySaveResult(
    val transaction: Transaction,
    val favoriteSaved: Boolean,
)

/** Coordinates transaction creation with the entry preferences updated after a successful save. */
class SaveTransactionEntryUseCase @Inject constructor(
    private val createTransaction: CreateTransactionUseCase,
    private val entryPreferences: TransactionEntryPreferencesUseCase,
) {
    suspend operator fun invoke(
        request: CreateTransactionRequest,
        type: TransactionType,
        walletId: String?,
        destinationWalletId: String?,
        category: TransactionCategory,
        favoriteTemplate: TransactionEntryTemplate?,
    ): Result<TransactionEntrySaveResult> {
        val result = createTransaction(request)
        if (result.isSuccess) {
            walletId?.let { entryPreferences.rememberEntry(type, it, destinationWalletId, category) }
            val favoriteSaved = favoriteTemplate?.let { template ->
                runCatching { entryPreferences.saveFavoriteTemplate(template) }.isSuccess
            } ?: false
            return Result.success(
                TransactionEntrySaveResult(
                    transaction = result.getOrThrow(),
                    favoriteSaved = favoriteSaved,
                )
            )
        }
        return Result.failure(result.exceptionOrNull() ?: AppException.UnknownError())
    }
}
