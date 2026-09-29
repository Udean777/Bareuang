package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.TransactionQueryRepository
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.BudgetPeriod
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Hanya enforce untuk transaksi dengan date == hari ini.
 */
class CheckDailyBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionQueryRepository,
    private val dailyPacingPreferences: DailyPacingPreferencesPort,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        amount: Long,
        date: String,
        category: TransactionCategory = TransactionCategory.OTHER
    ): Result<Unit> {
        if (amount <= 0) return Result.failure(
            AppException.TransactionValidationException(
                com.ssajudn.bareuang.domain.error.TransactionValidationReason.INVALID_AMOUNT
            )
        )
        return try {
            val today = LocalDate.now(clock)
            val monthYear = YearMonth.from(today).toString()
            val todayIso = today.toString()
            // only enforce for today
            if (date.length < 10 || date.take(10) != todayIso) return Result.success(Unit)

            val monthlyBudget = budgetRepository.getMonthlyBudget(monthYear).getOrElse { return Result.failure(it) }
            if (monthlyBudget <= 0) return Result.success(Unit) // monthly gate handles this

            val daysInMonth = today.lengthOfMonth()
            val daysPassed = today.dayOfMonth
            val remainingDays = (daysInMonth - daysPassed + 1).coerceAtLeast(1)
            val allTx = transactionRepository.getAllTransactions().getOrElse { return Result.failure(it) }
            val currentMonthTx = allTx.filter { !it.isRecurringParent && it.date.startsWith(monthYear) }
            val discretionaryExpenses = currentMonthTx.filter {
                it.type == TransactionType.EXPENSE &&
                    it.category != TransactionCategory.BILLS
            }
            val totalSpent = discretionaryExpenses.sumAmountsOrThrow()
            val remainingBudget = Math.subtractExact(monthlyBudget, totalSpent)
            val todaySpent = discretionaryExpenses.filter { it.date.take(10) == todayIso }.sumAmountsOrThrow()
            val pacing = CalculateDailyPacingUseCase(
                monthlyBudget = monthlyBudget,
                remainingBudget = remainingBudget,
                todaySpent = todaySpent,
                period = BudgetPeriod(
                    monthYear = monthYear,
                    todayIso = todayIso,
                    daysPassed = daysPassed,
                    daysInMonth = daysInMonth,
                ),
                customTarget = dailyPacingPreferences.customTarget.value,
            )
            val remainingToday = pacing.remaining

            if (category != TransactionCategory.BILLS && amount > remainingToday) {
                return Result.failure(
                    AppException.DailyBudgetExceededException(
                        remainingAmount = remainingToday.coerceAtLeast(0L),
                        requestedAmount = amount,
                    )
                )
            }
            Result.success(Unit)
        } catch (e: ArithmeticException) {
            Result.failure(AppException.DataException(cause = e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e is AppException) Result.failure(e) else Result.failure(AppException.UnknownError(cause = e))
        }
    }

    private fun List<com.ssajudn.bareuang.domain.model.Transaction>.sumAmountsOrThrow(): Long =
        fold(0L) { total, transaction -> Math.addExact(total, transaction.amount) }
}
