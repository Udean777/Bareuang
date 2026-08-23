package com.ssajudn.barebudget

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ssajudn.barebudget.data.notification.BillReminderPrefs
import com.ssajudn.barebudget.data.notification.BillReminderScheduler
import com.ssajudn.barebudget.data.local.WidgetPreferences
import com.ssajudn.barebudget.widget.BudgetWidgetWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BareBudgetApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var billReminderScheduler: BillReminderScheduler
    @Inject lateinit var billReminderPrefs: BillReminderPrefs
    @Inject lateinit var widgetPreferences: WidgetPreferences

    override val workManagerConfiguration: Configuration get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // Pengingat harian pada jam pilihan user; pemeriksaan cepat juga dijalankan tiap app dibuka.
        billReminderScheduler.scheduleDailyAt(billReminderPrefs.reminderHour(), billReminderPrefs.reminderMinute())
        billReminderScheduler.runNow()
        com.ssajudn.barebudget.data.service.RecurringTransactionWorker.ensureScheduled(this)
        com.ssajudn.barebudget.data.service.RecurringTransactionWorker.runNow(this)
        BudgetWidgetWorker.ensureScheduled(this)

        // Re-render the widget immediately whenever the privacy toggle changes.
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            widgetPreferences.hideBalance.drop(1).collect {
                BudgetWidgetWorker.runNow(this@BareBudgetApplication)
            }
        }
    }
}
