package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.CategorySummary
import com.ssajudn.bareuang.domain.model.DashboardSummary
import com.ssajudn.bareuang.domain.model.DueBillStatus
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.DueBillRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import javax.inject.Inject

/**
 * Pure domain logic — menghitung dashboard dari data repo.
 * Menggantikan `LocalBudgetRepository.getDashboardSummary()` (SRP).
 * Dependency: domain ports, bukan Room/Retrofit langsung.
 */
class GetDashboardSummaryUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val dueBillRepository: DueBillRepository,
    private val dailyPacingPreferences: DailyPacingPreferencesPort
) {
    suspend operator fun invoke(): Result<DashboardSummary> {
        return try {
            val now = Calendar.getInstance()
            val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now.time)
            val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            val daysPassed = now.get(Calendar.DAY_OF_MONTH)

            val monthlyBudget = budgetRepository.getMonthlyBudget(monthYear).getOrElse { return Result.failure(it) }

            val allTx = transactionRepository.getAllTransactions().getOrElse { return Result.failure(it) }
            val recurringTemplates = allTx.filter { it.isRecurringParent }
            val executedTx = allTx.filter { !it.isRecurringParent }
            val currentMonthTx = executedTx.filter { it.date.startsWith(monthYear) }

            val expensesTx = currentMonthTx.filter { it.type == TransactionType.EXPENSE && it.category != com.ssajudn.bareuang.domain.model.TransactionCategory.BILLS }
            val totalSpent = expensesTx.sumTransactionAmountsOrThrow()

            val remainingBudget = Math.subtractExact(monthlyBudget, totalSpent)
            val avgDaily = if (daysPassed > 0) totalSpent / daysPassed else 0L

            val remainingDays = (daysInMonth - daysPassed + 1).coerceAtLeast(1)
            val automaticDailyAllowance = if (monthlyBudget > 0) {
                remainingBudget.coerceAtLeast(0L) / remainingDays
            } else 0L
            val dailyAllowance = dailyPacingPreferences.customTarget.value ?: automaticDailyAllowance
            val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
            val todaySpent = expensesTx.filter { it.date.take(10) == todayIso }.sumTransactionAmountsOrThrow()
            val remainingToday = Math.subtractExact(dailyAllowance, todaySpent)

            val wallets = walletRepository.getWallets().getOrElse { return Result.failure(it) }
            val currentNetWorth = wallets.map { it.balance }.sumLongsOrThrow()

            var estimatedDeathDay = daysInMonth
            var runwayMsg: String

            if (monthlyBudget <= 0) {
                runwayMsg = "Monthly budget not set yet. Tap here to set your target budget."
            } else if (remainingBudget <= 0) {
                estimatedDeathDay = daysPassed
                runwayMsg = "CRITICAL: You have exhausted your budget for this month! Stop all non-essential spending."
            } else if (avgDaily <= 0) {
                estimatedDeathDay = daysInMonth
                runwayMsg = "GREAT: No expenses recorded yet this month. Keep it up!"
            } else {
                val daysRemainingFromRunway = (remainingBudget / avgDaily).toInt()
                val calculatedDeathDay = daysPassed + daysRemainingFromRunway
                estimatedDeathDay = calculatedDeathDay.coerceAtMost(daysInMonth)
                runwayMsg = if (calculatedDeathDay < daysInMonth) {
                    "WARNING: At your current burn rate, your money runs out on day $calculatedDeathDay ($daysRemainingFromRunway days left)!"
                } else {
                    "HEALTHY: Your financial runway is safe until the end of the month."
                }
            }

            val catMap = expensesTx.groupBy { it.category }
            val topCategories = catMap.map { (cat, list) ->
                CategorySummary(
                    category = cat,
                    total = list.map { it.amount }.sumLongsOrThrow(),
                    count = list.size.toLong()
                )
            }.sortedByDescending { it.total }

            val allBills = dueBillRepository.getDueBills().getOrElse { return Result.failure(it) }
            val unpaidSum = allBills.filter { it.status == DueBillStatus.UNPAID }
                .map { it.totalAmount }.sumLongsOrThrow()

            val summary = DashboardSummary(
                monthlyBudget = monthlyBudget,
                totalSpent = totalSpent,
                remainingBudget = remainingBudget,
                daysPassed = daysPassed,
                daysInMonth = daysInMonth,
                averageDailySpend = avgDaily,
                estimatedDeathDay = estimatedDeathDay,
                runwayMessage = runwayMsg,
                topCategories = topCategories,
                unpaidDueBillsSum = unpaidSum,
                netWorth = currentNetWorth,
                recentTransactions = currentMonthTx
                    .reversed()
                    .sortedByDescending { it.date }
                    .take(5),
                recurringTransactions = recurringTemplates,
                dailyAllowance = dailyAllowance,
                todaySpent = todaySpent,
                remainingToday = remainingToday,
                remainingDays = remainingDays
            )
            Result.success(summary)
        } catch (e: ArithmeticException) {
            Result.failure(AppException.DataException("Nominal transaksi terlalu besar untuk dihitung", e))
        } catch (e: Exception) {
            Result.failure(AppException.UnknownError(cause = e))
        }
    }

    private fun List<com.ssajudn.bareuang.domain.model.Transaction>.sumTransactionAmountsOrThrow(): Long =
        fold(0L) { total, transaction -> Math.addExact(total, transaction.amount) }

    private fun List<Long>.sumLongsOrThrow(): Long =
        fold(0L) { total, amount -> Math.addExact(total, amount) }
}
