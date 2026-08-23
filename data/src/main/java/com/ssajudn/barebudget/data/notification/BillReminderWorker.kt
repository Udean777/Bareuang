package com.ssajudn.barebudget.data.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ssajudn.barebudget.domain.model.DueBillStatus
import com.ssajudn.barebudget.domain.repository.DueBillRepository
import com.ssajudn.barebudget.domain.usecase.BuildBillRemindersUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Memeriksa tagihan UNPAID yang mendekati/lewat jatuh tempo dan menampilkan
 * notifikasi ringkasan. Aman dijalankan berkali-kali: dedup via [BillReminderPrefs].
 */
@HiltWorker
class BillReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dueBillRepository: DueBillRepository,
    private val buildReminders: BuildBillRemindersUseCase,
    private val prefs: BillReminderPrefs,
    private val notifier: BillNotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!prefs.notificationsEnabled()) return Result.success()

        val bills = dueBillRepository.getDueBills(DueBillStatus.UNPAID.name)
            .getOrDefault(emptyList())

        val reminders = buildReminders(bills) { bill ->
            runCatching { com.ssajudn.barebudget.utils.DateUtils.getDaysUntilDue(bill.dueDate) }
                .getOrDefault(Long.MAX_VALUE)
        }.filter { reminder ->
            val key = dedupeKey(reminder.billId, reminder.dueDateIso, reminder.urgency.name)
            val fresh = !prefs.alreadyShown(key)
            if (fresh) prefs.markShown(key)
            fresh
        }

        if (reminders.isNotEmpty()) {
            notifier.showSummary(reminders)
        }
        return Result.success()
    }

    private fun dedupeKey(billId: String, dueDateIso: String, urgency: String) =
        "$billId:$dueDateIso:$urgency"

    companion object {
        const val UNIQUE_PERIODIC = "bill_reminder_periodic"
        const val UNIQUE_ONE_TIME = "bill_reminder_once"
    }
}
