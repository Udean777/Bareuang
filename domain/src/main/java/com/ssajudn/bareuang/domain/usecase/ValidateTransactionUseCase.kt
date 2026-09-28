package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.error.TransactionValidationReason
import javax.inject.Inject

class ValidateTransactionUseCase @Inject constructor() {
    operator fun invoke(
        type: TransactionType,
        amount: Long,
        sourceWalletId: String?,
        targetWalletId: String?,
        wallets: List<Wallet>
    ): TransactionValidationReason? {
        if (amount <= 0) return TransactionValidationReason.INVALID_AMOUNT
        if (sourceWalletId == null) return TransactionValidationReason.WALLET_REQUIRED
        if (type == TransactionType.TRANSFER) {
            if (targetWalletId == null) return TransactionValidationReason.TO_WALLET_REQUIRED
            if (sourceWalletId == targetWalletId) return TransactionValidationReason.SAME_WALLET
            if (wallets.none { it.id == targetWalletId }) return TransactionValidationReason.TO_WALLET_REQUIRED
        }
        val sourceWallet = wallets.firstOrNull { it.id == sourceWalletId }
            ?: return TransactionValidationReason.WALLET_REQUIRED
        if (type == TransactionType.EXPENSE || type == TransactionType.TRANSFER) {
            if (sourceWallet.balance < amount) return TransactionValidationReason.INSUFFICIENT_BALANCE
        }
        return null
    }
}
