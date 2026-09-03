package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.utils.DomainCurrencyFormatter
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Hanya enforce untuk transaksi dengan date == hari ini.
 */
class CheckDailyBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val dailyPacingPreferences: DailyPacingPreferencesPort
) {
    suspend operator fun invoke(
        amount: Long,
        date: String,
        currency: AppCurrency,
        category: TransactionCategory = TransactionCategory.OTHER
    ): Result<Unit> {
        if (amount <= 0) return Result.failure(AppException.DataException("Jumlah harus lebih dari 0"))
        return try {
            val now = Calendar.getInstance()
            val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now.time)
            val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
            // only enforce for today
            if (date.length < 10 || date.take(10) != todayIso) return Result.success(Unit)

            val monthlyBudget = budgetRepository.getMonthlyBudget(monthYear).getOrElse { return Result.failure(it) }
            if (monthlyBudget <= 0) return Result.success(Unit) // monthly gate handles this

            val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            val daysPassed = now.get(Calendar.DAY_OF_MONTH)
            val remainingDays = (daysInMonth - daysPassed + 1).coerceAtLeast(1)
            val allTx = transactionRepository.getAllTransactions().getOrElse { return Result.failure(it) }
            val currentMonthTx = allTx.filter { !it.isRecurringParent && it.date.startsWith(monthYear) }
            val discretionaryExpenses = currentMonthTx.filter {
                it.type == TransactionType.EXPENSE &&
                    it.category != TransactionCategory.BILLS
            }
            val totalSpent = discretionaryExpenses.sumAmountsOrThrow()
            val remainingBudget = Math.subtractExact(monthlyBudget, totalSpent)
            val automaticDailyAllowance = remainingBudget.coerceAtLeast(0L) / remainingDays
            val dailyAllowance = dailyPacingPreferences.customTarget.value ?: automaticDailyAllowance
            val todaySpent = discretionaryExpenses.filter { it.date.take(10) == todayIso }.sumAmountsOrThrow()
            val remainingToday = Math.subtractExact(dailyAllowance, todaySpent)

            if (category != TransactionCategory.BILLS && amount > remainingToday) {
                val formatted = DomainCurrencyFormatter.format(remainingToday.coerceAtLeast(0L), currency)
                val msg = if (remainingToday <= 0) "Target pacing hari ini sudah terlampaui."
                else "Melebihi target pacing hari ini. Sisa target $formatted."
                return Result.failure(AppException.DataException(msg))
            }
            Result.success(Unit)
        } catch (e: ArithmeticException) {
            Result.failure(AppException.DataException("Nominal transaksi terlalu besar untuk dihitung", e))
        } catch (e: Exception) {
            if (e is AppException) Result.failure(e) else Result.failure(AppException.UnknownError(cause = e))
        }
    }

    private fun List<com.ssajudn.bareuang.domain.model.Transaction>.sumAmountsOrThrow(): Long =
        fold(0L) { total, transaction -> Math.addExact(total, transaction.amount) }
}
