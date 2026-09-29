package com.ssajudn.bareuang.ui.transaction

import androidx.annotation.StringRes
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.domain.error.TransactionValidationReason

enum class AddTransactionError(@StringRes val resId: Int) {
    INVALID_AMOUNT(R.string.tx_error_invalid_amount),
    WALLET_REQUIRED(R.string.tx_error_wallet_required),
    TO_WALLET_REQUIRED(R.string.tx_error_to_wallet_required),
    SAME_WALLET(R.string.tx_error_same_wallet),
    INSUFFICIENT_BALANCE(R.string.tx_error_insufficient_balance),
    DAILY_BUDGET_EXCEEDED(R.string.tx_error_daily_exceeded),
    SAVE_FAILED(R.string.tx_error_save_failed)
}

fun AddTransactionError.toUiText(argument: String? = null): UiText =
    if (this == AddTransactionError.INSUFFICIENT_BALANCE) {
        UiText.Res(resId, listOf(argument.orEmpty()))
    } else {
        UiText.Res(resId)
    }

fun TransactionValidationReason.toUiError(): AddTransactionError = when (this) {
    TransactionValidationReason.INVALID_AMOUNT -> AddTransactionError.INVALID_AMOUNT
    TransactionValidationReason.WALLET_REQUIRED -> AddTransactionError.WALLET_REQUIRED
    TransactionValidationReason.TO_WALLET_REQUIRED -> AddTransactionError.TO_WALLET_REQUIRED
    TransactionValidationReason.SAME_WALLET -> AddTransactionError.SAME_WALLET
    TransactionValidationReason.INSUFFICIENT_BALANCE -> AddTransactionError.INSUFFICIENT_BALANCE
}
