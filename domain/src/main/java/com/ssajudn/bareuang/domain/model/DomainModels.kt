package com.ssajudn.bareuang.domain.model

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class TransactionCategory(val displayName: String, val iconName: String) {
    FOOD("Food & Beverage", "restaurant"),
    TRANSPORT("Transportation", "directions_car"),
    BILLS("Bills & Utilities", "receipt_long"),
    SHOPPING("Shopping & Groceries", "shopping_bag"),
    ENTERTAINMENT("Entertainment & Gaming", "sports_esports"),
    SOCIAL("Social & Gatherings", "groups"),
    SALARY("Salary & Wage", "payments"),
    BONUS("Bonus & Reward", "redeem"),
    INVESTMENT("Investment Returns", "trending_up"),
    TRANSFER("Wallet Transfer", "swap_horiz"),
    OTHER("Other", "category")
}

data class Wallet(
    val id: String? = null,
    val name: String,
    val balance: Long = 0L,
    val colorHex: String = "#4E73DF",
    val iconName: String = "account_balance_wallet",
    val createdAt: String? = null
)

data class Transaction(
    val id: String? = null,
    val amount: Long,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory,
    val merchant: String? = null,
    val date: String,
    val notes: String? = null,
    val receiptUrl: String? = null,
    val walletId: String? = null,
    val toWalletId: String? = null,
    val createdAt: String? = null,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    val isRecurringParent: Boolean = false,
    val parentRecurringId: String? = null,
    val nextOccurrenceDate: String? = null
)

enum class DueBillStatus {
    UNPAID,
    PAID
}

enum class RecurringInterval(val displayName: String) {
    NONE("One-time"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

data class DueBill(
    val id: String? = null,
    val providerName: String,
    val providerIconUrl: String? = null,
    val totalAmount: Long,
    val dueDate: String,
    val status: DueBillStatus = DueBillStatus.UNPAID,
    val paidWalletId: String? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    val notes: String? = null,
    val createdAt: String? = null
)

data class CategorySummary(
    val category: TransactionCategory,
    val total: Long,
    val count: Long
)

data class CategoryBudget(
    val category: TransactionCategory,
    val limitAmount: Long,
    val spentAmount: Long = 0L,
    val monthYear: String = ""
) {
    val remainingAmount: Long get() = (limitAmount - spentAmount).coerceAtLeast(0L)
    val progressPercentage: Float get() = if (limitAmount > 0) (spentAmount.toFloat() / limitAmount).coerceIn(0f, 1f) else 0f
    val isOverspent: Boolean get() = spentAmount > limitAmount
    val isWarning: Boolean get() = progressPercentage >= 0.8f && !isOverspent
}

data class DashboardSummary(
    val monthlyBudget: Long,
    val totalSpent: Long,
    val remainingBudget: Long,
    val daysPassed: Int,
    val daysInMonth: Int,
    val averageDailySpend: Long,
    val estimatedDeathDay: Int,
    val runwayMessage: String,
    val topCategories: List<CategorySummary>?,
    val unpaidDueBillsSum: Long,
    val netWorth: Long = 0L,
    val recentTransactions: List<Transaction>?,
    val recurringTransactions: List<Transaction> = emptyList(),
    val dailyAllowance: Long = 0L,
    val todaySpent: Long = 0L,
    val remainingToday: Long = 0L,
    val remainingDays: Int = 0
) {
    val isDailyExceeded: Boolean get() = monthlyBudget > 0 && remainingToday < 0
    val dailyProgress: Float get() = if (dailyAllowance > 0) (todaySpent.toFloat() / dailyAllowance).coerceIn(0f, 1f) else 0f
}

data class Goal(
    val id: String? = null,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long = 0L,
    val targetDate: String? = null,
    val colorHex: String = "#4E73DF",
    val notes: String? = null,
    val createdAt: String? = null
) {
    val progressPercentage: Float
        get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f) else 0f

    val remainingAmount: Long
        get() = (targetAmount - currentAmount).coerceAtLeast(0L)

    /**
     * Days until [targetDate] (ISO yyyy-MM-dd…), or null when unset/unparseable.
     */
    fun daysLeftUntilTarget(): Int? {
        val isoDate = targetDate?.takeIf { it.length >= 10 }?.substring(0, 10) ?: return null
        return runCatching {
            java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), java.time.LocalDate.parse(isoDate))
        }.getOrNull()?.toInt()
    }

    /**
     * Suggested savings pace to reach the target date: per-month and per-day
     * amounts, or null when there is no future target date.
     */
    fun suggestedSavingsPace(): Pair<Long, Long>? {
        val daysLeft = daysLeftUntilTarget() ?: return null
        if (daysLeft <= 0 || remainingAmount <= 0L) return null
        val monthsLeft = maxOf(1L, daysLeft / 30L)
        return (remainingAmount / monthsLeft) to (remainingAmount / daysLeft)
    }
}

data class CashflowDataPoint(
    val month: String,
    val label: String,
    val income: Long,
    val expense: Long
)

data class NetWorthDataPoint(
    val month: String,
    val label: String,
    val netWorth: Long
)

data class CreateWalletRequest(
    val name: String,
    val balance: Long = 0L,
    val colorHex: String = "#4E73DF",
    val iconName: String = "account_balance_wallet"
)

data class CreateTransactionRequest(
    val amount: Long,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory,
    val merchant: String,
    val date: String,
    val notes: String = "",
    val receiptUrl: String = "",
    val walletId: String? = null,
    val toWalletId: String? = null,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE
)

data class CreateDueBillRequest(
    val providerName: String,
    val providerIconUrl: String? = null,
    val totalAmount: Long,
    val dueDate: String,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    val notes: String = ""
)

data class UpdateDueBillRequest(
    val providerName: String,
    val providerIconUrl: String? = null,
    val totalAmount: Long,
    val dueDate: String,
    val isRecurring: Boolean = false,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE,
    val notes: String = ""
)

data class CreateGoalRequest(
    val name: String,
    val targetAmount: Long,
    val targetDate: String = "",
    val colorHex: String = "#4E73DF",
    val notes: String = ""
)

data class UpdateGoalRequest(
    val name: String,
    val targetAmount: Long,
    val targetDate: String = "",
    val colorHex: String = "#4E73DF",
    val notes: String = ""
)
