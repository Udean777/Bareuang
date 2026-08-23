package com.ssajudn.barebudget.domain.usecase

import com.ssajudn.barebudget.domain.model.Transaction
import com.ssajudn.barebudget.domain.model.RecurringInterval
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ProcessRecurringTransactionsUseCase @Inject constructor() {

    data class RolloverResult(
        val newTransactions: List<Transaction>,
        val updatedTemplates: List<TemplateUpdate>
    )

    data class TemplateUpdate(
        val templateId: String,
        val nextOccurrenceDate: String
    )

    operator fun invoke(
        templates: List<Transaction>,
        todayIso: String
    ): RolloverResult {
        val newTransactions = mutableListOf<Transaction>()
        val updatedTemplates = mutableListOf<TemplateUpdate>()

        for (template in templates) {
            if (!template.isRecurringParent || template.recurringInterval == RecurringInterval.NONE) continue
            var nextDateStr = template.nextOccurrenceDate ?: continue

            var iterations = 0
            // Prevent infinite loop if dates are far behind
            while (nextDateStr <= todayIso && iterations < 30) {
                iterations++
                val newTx = Transaction(
                    id = UUID.randomUUID().toString(),
                    userId = template.userId,
                    amount = template.amount,
                    type = template.type,
                    category = template.category,
                    merchant = template.merchant,
                    date = nextDateStr,
                    notes = template.notes,
                    receiptUrl = template.receiptUrl,
                    walletId = template.walletId,
                    toWalletId = template.toWalletId,
                    recurringInterval = RecurringInterval.NONE,
                    isRecurringParent = false,
                    parentRecurringId = template.id,
                    nextOccurrenceDate = null
                )
                newTransactions.add(newTx)

                // Advance date
                nextDateStr = calculateNextOccurrence(nextDateStr, template.recurringInterval)
            }

            if (iterations > 0 && template.id != null) {
                updatedTemplates.add(
                    TemplateUpdate(
                        templateId = template.id,
                        nextOccurrenceDate = nextDateStr
                    )
                )
            }
        }

        return RolloverResult(newTransactions, updatedTemplates)
    }

    private fun calculateNextOccurrence(currentDateStr: String, interval: RecurringInterval): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(currentDateStr.substring(0, minOf(10, currentDateStr.length))) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            when (interval) {
                RecurringInterval.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                RecurringInterval.MONTHLY -> cal.add(Calendar.MONTH, 1)
                RecurringInterval.YEARLY -> cal.add(Calendar.YEAR, 1)
                RecurringInterval.NONE -> {}
            }
            sdf.format(cal.time)
        } catch (_: Exception) {
            currentDateStr
        }
    }
}
