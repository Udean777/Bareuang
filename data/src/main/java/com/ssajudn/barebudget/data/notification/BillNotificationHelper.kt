package com.ssajudn.barebudget.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ssajudn.barebudget.data.R
import com.ssajudn.barebudget.domain.usecase.BillReminder
import com.ssajudn.barebudget.domain.usecase.BillReminderUrgency
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Membangun dan menampilkan satu notifikasi ringkasan pengingat tagihan.
 * Semua teks diambil dari resource agar mengikuti bahasa aplikasi (id/en).
 */
@Singleton
class BillNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.bill_reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.bill_reminder_channel_desc)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun showSummary(reminders: List<BillReminder>) {
        if (reminders.isEmpty()) return

        ensureChannel()

        val overdueCount = reminders.count { it.urgency == BillReminderUrgency.OVERDUE }
        val title = when {
            overdueCount > 0 -> context.getString(R.string.bill_reminder_title_overdue, overdueCount)
            else -> context.getString(R.string.bill_reminder_title_upcoming, reminders.size)
        }

        val preview = reminders.take(MAX_LINES).joinToString("\n") { reminder ->
            "• ${reminder.providerName}: ${formatAmount(reminder.amount)}"
        }
        val extra = reminders.size - MAX_LINES
        val body = if (extra > 0) {
            "$preview\n" + context.getString(R.string.bill_reminder_more, extra)
        } else {
            preview
        }

        // Membuka aplikasi; tidak ada deep link karena app full-offline
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun formatAmount(amount: Long): String = "Rp" + amount.toString()
        .reversed().chunked(3).joinToString(".").reversed()

    companion object {
        const val CHANNEL_ID = "bill_reminders"
        const val NOTIFICATION_ID = 4201
        private const val MAX_LINES = 3
    }
}
