package com.ssajudn.bareuang.domain.error

enum class TransactionValidationReason {
    INVALID_AMOUNT,
    WALLET_REQUIRED,
    TO_WALLET_REQUIRED,
    SAME_WALLET,
    INSUFFICIENT_BALANCE
}

enum class GoalOperationReason {
    INVALID_NAME,
    INVALID_TARGET,
    INVALID_AMOUNT,
    WALLET_REQUIRED,
    GOAL_NOT_FOUND,
    INSUFFICIENT_GOAL_BALANCE,
}

enum class DueBillOperationReason {
    INVALID_PROVIDER,
    INVALID_AMOUNT,
    BILL_NOT_FOUND,
    WALLET_NOT_FOUND,
}

enum class WalletOperationReason {
    INVALID_NAME,
    NEGATIVE_BALANCE,
    NOT_FOUND,
}

enum class BudgetOperationReason {
    INVALID_MONTHLY_AMOUNT,
    MONTH_ALREADY_CONFIGURED,
    INVALID_CATEGORY_AMOUNT,
}

enum class BulkTransactionReason {
    WALLET_REQUIRED,
    NOTHING_SELECTED,
    INVALID_AMOUNT,
}

enum class BackupOperationReason {
    DESTINATION_UNAVAILABLE,
    SOURCE_UNAVAILABLE,
    PREVIEW_EXPIRED,
    RESTORE_IN_PROGRESS,
    UNSUPPORTED_CURRENCY,
    FILE_TOO_LARGE,
    INVALID_PASSWORD_OR_FILE,
    INVALID_FILE,
}

/**
 * Domain-level typed errors — tidak bocor HttpException/IOException ke ui.
 * Clean Architecture: error type milik domain, data map ke sini via ApiErrorParser.
 */
sealed class AppException(
    override val message: String?,
    override val cause: Throwable? = null,
    open val code: AppErrorCode = AppErrorCode.UNKNOWN,
) : Exception(message, cause) {

    class NetworkException(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.NETWORK)

    class AuthException(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.AUTH)

    class DataException(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.DATA)

    /**
     * Distinct marker for the daily-budget soft nudge: lets callers distinguish
     * "exceeds today's allowance" from generic data failures so they can offer a
     * "save anyway" confirmation instead of a hard error.
     */
    class DailyBudgetExceededException(
        val remainingAmount: Long,
        val requestedAmount: Long,
        cause: Throwable? = null
    ) : AppException(null, cause, AppErrorCode.DAILY_BUDGET_EXCEEDED)

    class GoalOperationException(
        val reason: GoalOperationReason,
        cause: Throwable? = null,
    ) : AppException(null, cause, AppErrorCode.GOAL_OPERATION)

    class DueBillOperationException(
        val reason: DueBillOperationReason,
        cause: Throwable? = null,
    ) : AppException(null, cause, AppErrorCode.DUE_BILL_OPERATION)

    class WalletOperationException(
        val reason: WalletOperationReason,
        cause: Throwable? = null,
    ) : AppException(null, cause, AppErrorCode.WALLET_OPERATION)

    class BudgetOperationException(
        val reason: BudgetOperationReason,
        cause: Throwable? = null,
    ) : AppException(null, cause, AppErrorCode.BUDGET_OPERATION)

    class BulkTransactionException(
        val reason: BulkTransactionReason,
        cause: Throwable? = null,
    ) : AppException(null, cause, AppErrorCode.BULK_TRANSACTION)

    class BackupOperationException(
        val reason: BackupOperationReason,
        cause: Throwable? = null,
    ) : AppException(null, cause, AppErrorCode.BACKUP_OPERATION)

    class InsufficientBalanceException(
        val availableBalance: Long,
        val requestedAmount: Long,
        cause: Throwable? = null
    ) : AppException(null, cause, AppErrorCode.INSUFFICIENT_BALANCE)

    class TransactionValidationException(
        val reason: TransactionValidationReason,
        cause: Throwable? = null
    ) : AppException(null, cause, AppErrorCode.TRANSACTION_VALIDATION)

    class SyncException(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.SYNC)

    class UnknownError(
        message: String? = null,
        cause: Throwable? = null
    ) : AppException(message, cause, AppErrorCode.UNKNOWN)
}

enum class AppErrorCode {
    NETWORK,
    AUTH,
    DATA,
    DAILY_BUDGET_EXCEEDED,
    GOAL_OPERATION,
    DUE_BILL_OPERATION,
    WALLET_OPERATION,
    BUDGET_OPERATION,
    BULK_TRANSACTION,
    BACKUP_OPERATION,
    INSUFFICIENT_BALANCE,
    TRANSACTION_VALIDATION,
    SYNC,
    UNKNOWN,
}
