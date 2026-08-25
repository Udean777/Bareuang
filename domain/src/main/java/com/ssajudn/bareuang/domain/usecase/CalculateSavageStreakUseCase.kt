package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Savage Streak: number of consecutive days (counting back from today)
 * without any FOOD or ENTERTAINMENT expense, capped at a 30-day window.
 * Returns 0 honestly when there is nothing to show.
 */
class CalculateSavageStreakUseCase @Inject constructor(
    private val clock: Clock
) {
    operator fun invoke(transactions: List<Transaction>): Int {
        if (transactions.isEmpty()) return 0

        val spendDates = transactions
            .filter { it.category == TransactionCategory.FOOD || it.category == TransactionCategory.ENTERTAINMENT }
            .mapNotNull { it.date.takeIf { d -> d.length >= 10 }?.substring(0, 10) }
            .toSet()

        var streak = 0
        val today = LocalDate.now(clock)
        for (i in 0 until 30) {
            val dateStr = today.minusDays(i.toLong()).toString()
            if (!spendDates.contains(dateStr)) {
                streak++
            } else {
                if (i > 0) break // Streak broken
            }
        }
        return streak
    }
}
