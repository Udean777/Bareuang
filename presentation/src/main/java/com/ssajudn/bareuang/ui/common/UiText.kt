package com.ssajudn.bareuang.ui.common

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
    data class PluralRes(
        @PluralsRes val id: Int,
        val quantity: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    data class Dyn(val message: String) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Res -> if (args.isEmpty()) stringResource(id) else stringResource(
        id,
        *args.toTypedArray()
    )

    is UiText.PluralRes -> pluralStringResource(id, quantity, *args.toTypedArray())
    is UiText.Dyn -> message
}

fun UiText.asString(context: android.content.Context): String = when (this) {
    is UiText.Res -> if (args.isEmpty()) context.getString(id) else context.getString(
        id,
        *args.toTypedArray()
    )

    is UiText.PluralRes -> context.resources.getQuantityString(id, quantity, *args.toTypedArray())
    is UiText.Dyn -> message
}

fun UiText.resolveFallback(fallback: String = ""): String = when (this) {
    is UiText.Dyn -> message
    is UiText.Res, is UiText.PluralRes -> fallback
}

fun String.toUiText(): UiText = UiText.Dyn(this)

/** Maps AppException to UiText with i18n stringRes — never leak raw system message, per-feature granular */
fun com.ssajudn.bareuang.domain.error.AppException.toUiText(): UiText = when (this) {
    is com.ssajudn.bareuang.domain.error.AppException.NetworkException -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_network)
    is com.ssajudn.bareuang.domain.error.AppException.AuthException -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_auth)
    is com.ssajudn.bareuang.domain.error.AppException.DataException -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_generic)
    is com.ssajudn.bareuang.domain.error.AppException.InsufficientBalanceException -> UiText.Res(
        com.ssajudn.bareuang.presentation.R.string.tx_error_insufficient_balance,
        listOf(com.ssajudn.bareuang.utils.CurrencyFormatter.formatCurrency(availableBalance))
    )

    is com.ssajudn.bareuang.domain.error.AppException.TransactionValidationException -> UiText.Res(
        when (reason) {
            com.ssajudn.bareuang.domain.error.TransactionValidationReason.INVALID_AMOUNT -> com.ssajudn.bareuang.presentation.R.string.tx_error_invalid_amount
            com.ssajudn.bareuang.domain.error.TransactionValidationReason.WALLET_REQUIRED -> com.ssajudn.bareuang.presentation.R.string.tx_error_wallet_required
            com.ssajudn.bareuang.domain.error.TransactionValidationReason.TO_WALLET_REQUIRED -> com.ssajudn.bareuang.presentation.R.string.tx_error_to_wallet_required
            com.ssajudn.bareuang.domain.error.TransactionValidationReason.SAME_WALLET -> com.ssajudn.bareuang.presentation.R.string.tx_error_same_wallet
            com.ssajudn.bareuang.domain.error.TransactionValidationReason.INSUFFICIENT_BALANCE -> com.ssajudn.bareuang.presentation.R.string.error_generic
        }
    )

    is com.ssajudn.bareuang.domain.error.AppException.SyncException -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_generic)
    is com.ssajudn.bareuang.domain.error.AppException.UnknownError -> UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_generic)
    is com.ssajudn.bareuang.domain.error.AppException.DailyBudgetExceededException -> UiText.Res(
        com.ssajudn.bareuang.presentation.R.string.tx_error_daily_exceeded_with_remaining,
        listOf(com.ssajudn.bareuang.utils.CurrencyFormatter.formatCurrency(remainingAmount))
    )
    is com.ssajudn.bareuang.domain.error.AppException.GoalOperationException -> UiText.Res(
        when (reason) {
            com.ssajudn.bareuang.domain.error.GoalOperationReason.INVALID_NAME -> com.ssajudn.bareuang.presentation.R.string.goals_error_invalid_name
            com.ssajudn.bareuang.domain.error.GoalOperationReason.INVALID_TARGET -> com.ssajudn.bareuang.presentation.R.string.goals_error_invalid_target
            com.ssajudn.bareuang.domain.error.GoalOperationReason.INVALID_AMOUNT -> com.ssajudn.bareuang.presentation.R.string.goals_error_invalid_amount
            com.ssajudn.bareuang.domain.error.GoalOperationReason.WALLET_REQUIRED -> com.ssajudn.bareuang.presentation.R.string.tx_error_wallet_required
            com.ssajudn.bareuang.domain.error.GoalOperationReason.GOAL_NOT_FOUND -> com.ssajudn.bareuang.presentation.R.string.goals_error_not_found
            com.ssajudn.bareuang.domain.error.GoalOperationReason.INSUFFICIENT_GOAL_BALANCE -> com.ssajudn.bareuang.presentation.R.string.goals_error_insufficient_savings
        }
    )
    is com.ssajudn.bareuang.domain.error.AppException.DueBillOperationException -> UiText.Res(
        when (reason) {
            com.ssajudn.bareuang.domain.error.DueBillOperationReason.INVALID_PROVIDER -> com.ssajudn.bareuang.presentation.R.string.bills_error_invalid_provider
            com.ssajudn.bareuang.domain.error.DueBillOperationReason.INVALID_AMOUNT -> com.ssajudn.bareuang.presentation.R.string.bills_error_invalid_amount
            com.ssajudn.bareuang.domain.error.DueBillOperationReason.BILL_NOT_FOUND -> com.ssajudn.bareuang.presentation.R.string.bills_error_not_found
            com.ssajudn.bareuang.domain.error.DueBillOperationReason.WALLET_NOT_FOUND -> com.ssajudn.bareuang.presentation.R.string.wallets_error_not_found
        }
    )
    is com.ssajudn.bareuang.domain.error.AppException.WalletOperationException -> UiText.Res(
        when (reason) {
            com.ssajudn.bareuang.domain.error.WalletOperationReason.INVALID_NAME -> com.ssajudn.bareuang.presentation.R.string.wallet_error_invalid_name
            com.ssajudn.bareuang.domain.error.WalletOperationReason.NEGATIVE_BALANCE -> com.ssajudn.bareuang.presentation.R.string.wallet_error_negative_balance
            com.ssajudn.bareuang.domain.error.WalletOperationReason.NOT_FOUND -> com.ssajudn.bareuang.presentation.R.string.wallets_error_not_found
        }
    )
    is com.ssajudn.bareuang.domain.error.AppException.BudgetOperationException -> UiText.Res(
        when (reason) {
            com.ssajudn.bareuang.domain.error.BudgetOperationReason.INVALID_MONTHLY_AMOUNT -> com.ssajudn.bareuang.presentation.R.string.budget_error_invalid
            com.ssajudn.bareuang.domain.error.BudgetOperationReason.MONTH_ALREADY_CONFIGURED -> com.ssajudn.bareuang.presentation.R.string.budget_error_locked
            com.ssajudn.bareuang.domain.error.BudgetOperationReason.INVALID_CATEGORY_AMOUNT -> com.ssajudn.bareuang.presentation.R.string.budget_error_invalid
        }
    )
    is com.ssajudn.bareuang.domain.error.AppException.BulkTransactionException -> UiText.Res(
        when (reason) {
            com.ssajudn.bareuang.domain.error.BulkTransactionReason.WALLET_REQUIRED -> com.ssajudn.bareuang.presentation.R.string.tx_error_wallet_required
            com.ssajudn.bareuang.domain.error.BulkTransactionReason.NOTHING_SELECTED -> com.ssajudn.bareuang.presentation.R.string.import_error_no_selection
            com.ssajudn.bareuang.domain.error.BulkTransactionReason.INVALID_AMOUNT -> com.ssajudn.bareuang.presentation.R.string.tx_error_invalid_amount
        }
    )
    is com.ssajudn.bareuang.domain.error.AppException.BackupOperationException -> UiText.Res(
        when (reason) {
            com.ssajudn.bareuang.domain.error.BackupOperationReason.DESTINATION_UNAVAILABLE -> com.ssajudn.bareuang.presentation.R.string.settings_backup_destination_unavailable
            com.ssajudn.bareuang.domain.error.BackupOperationReason.SOURCE_UNAVAILABLE -> com.ssajudn.bareuang.presentation.R.string.settings_backup_source_unavailable
            com.ssajudn.bareuang.domain.error.BackupOperationReason.PREVIEW_EXPIRED -> com.ssajudn.bareuang.presentation.R.string.settings_restore_preview_expired
            com.ssajudn.bareuang.domain.error.BackupOperationReason.RESTORE_IN_PROGRESS -> com.ssajudn.bareuang.presentation.R.string.settings_restore_in_progress
            com.ssajudn.bareuang.domain.error.BackupOperationReason.UNSUPPORTED_CURRENCY -> com.ssajudn.bareuang.presentation.R.string.settings_restore_unsupported_currency
            com.ssajudn.bareuang.domain.error.BackupOperationReason.FILE_TOO_LARGE -> com.ssajudn.bareuang.presentation.R.string.settings_restore_file_too_large
            com.ssajudn.bareuang.domain.error.BackupOperationReason.INVALID_PASSWORD_OR_FILE -> com.ssajudn.bareuang.presentation.R.string.settings_restore_invalid_password_or_file
            com.ssajudn.bareuang.domain.error.BackupOperationReason.INVALID_FILE -> com.ssajudn.bareuang.presentation.R.string.settings_restore_invalid_file
        }
    )
}
