package com.ssajudn.bareuang.data.service

import android.content.Context
import com.ssajudn.bareuang.domain.port.RecurringTransactionSchedulerPort
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RecurringTransactionWorkScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RecurringTransactionSchedulerPort {
    override fun ensureScheduled() = RecurringTransactionWorker.ensureScheduled(context)

    override fun runNow() = RecurringTransactionWorker.runNow(context)
}
