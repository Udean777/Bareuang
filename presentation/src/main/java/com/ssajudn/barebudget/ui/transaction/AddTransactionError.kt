package com.ssajudn.barebudget.ui.transaction

import androidx.annotation.StringRes
import com.ssajudn.barebudget.presentation.R

enum class AddTransactionError(@StringRes val resId: Int) {
    INVALID_AMOUNT(R.string.tx_error_invalid_amount),
    WALLET_REQUIRED(R.string.tx_error_wallet_required),
    TO_WALLET_REQUIRED(R.string.tx_error_to_wallet_required),
    SAME_WALLET(R.string.tx_error_same_wallet),
    INSUFFICIENT_BALANCE(R.string.tx_error_insufficient_balance),
    BUDGET_REQUIRED(R.string.tx_error_budget_required),
    SAVE_FAILED(R.string.tx_error_save_failed)
}
