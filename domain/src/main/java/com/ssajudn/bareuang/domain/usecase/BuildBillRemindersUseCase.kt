package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.DueBill
import javax.inject.Inject

enum class BillReminderUrgency { OVERDUE, TODAY, TOMORROW, SOON }

data class BillReminder(
    val billId: String,
    val providerName: String,
    val amount: Long,
    val dueDateIso: String,
    val daysLeft: Long,
    val urgency: BillReminderUrgency
)

/**
 * Pure policy: maps unpaid bills into reminders worth notifying.
 * OVERDUE = lewat jatuh tempo, TODAY = hari ini, TOMORROW = besok,
 * SOON = dalam 3 hari ke depan. Sisanya tidak perlu diingatkan.
 */
class BuildBillRemindersUseCase @Inject constructor() {

    operator fun invoke(unpaidBills: List<DueBill>, daysLeftOf: (DueBill) -> Long): List<BillReminder> =
        unpaidBills.mapNotNull { bill ->
            val id = bill.id ?: return@mapNotNull null
            val daysLeft = daysLeftOf(bill)
            val urgency = urgencyFor(daysLeft) ?: return@mapNotNull null
            BillReminder(
                billId = id,
                providerName = bill.providerName,
                amount = bill.totalAmount,
                dueDateIso = bill.dueDate,
                daysLeft = daysLeft,
                urgency = urgency
            )
        }

    companion object {
        fun urgencyFor(daysLeft: Long): BillReminderUrgency? = when {
            daysLeft < 0 -> BillReminderUrgency.OVERDUE
            daysLeft == 0L -> BillReminderUrgency.TODAY
            daysLeft == 1L -> BillReminderUrgency.TOMORROW
            daysLeft <= 3L -> BillReminderUrgency.SOON
            else -> null
        }
    }
}
