package com.ssajudn.barebudget.data.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Menjadwalkan pemeriksaan pengingat: periodic 2x sehari sebagai jaring pengaman,
 * plus one-time setiap app dibuka agar notifikasi muncul tanpa menunggu periode.
 */
@Singleton
class BillReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun ensureScheduled() {
        val request = PeriodicWorkRequestBuilder<BillReminderWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BillReminderWorker.UNIQUE_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun runNow() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            BillReminderWorker.UNIQUE_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<BillReminderWorker>().build()
        )
    }
}
