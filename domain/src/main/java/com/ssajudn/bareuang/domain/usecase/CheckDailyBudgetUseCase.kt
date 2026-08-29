package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.utils.DomainCurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * ponytail: derived daily budget, no DB field. Blokir EXPENSE jika melebihi jatah harian.
 * Hanya enforce untuk transaksi dengan date == hari ini.
 */
class CheckDailyBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(amount: Long, date: String, currency: AppCurrency): Result<Unit> {
        return try {
            val now = Calendar.getInstance()
            val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now.time)
            val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
            // only enforce for today
            if (date.take(10) != todayIso) return Result.success(Unit)

            val monthlyBudget = budgetRepository.getMonthlyBudget(monthYear).getOrDefault(0L)
            if (monthlyBudget <= 0) return Result.success(Unit) // monthly gate handles this

            val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
            // ponytail: flat tanpa rollover — monthly/daysInMonth
            val dailyAllowance = if (monthlyBudget > 0) monthlyBudget / daysInMonth else 0L
            val allTx = transactionRepository.getTransactions(limit = 500).getOrDefault(emptyList())
            val currentMonthTx = allTx.filter { !it.isRecurringParent && it.date.startsWith(monthYear) }
            val todaySpent = currentMonthTx.filter { it.type == TransactionType.EXPENSE && it.date.take(10) == todayIso }.sumOf { it.amount }
            val remainingToday = dailyAllowance - todaySpent

            if (amount > remainingToday) {
                val formatted = DomainCurrencyFormatter.format(remainingToday.coerceAtLeast(0L), currency)
                val msg = if (remainingToday <= 0) "Jatah harian habis. Coba lagi besok."
                else "Melebihi jatah harian. Sisa hari ini $formatted."
                return Result.failure(AppException.DataException(msg))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is AppException) Result.failure(e) else Result.failure(AppException.UnknownError(cause = e))
        }
    }
}
