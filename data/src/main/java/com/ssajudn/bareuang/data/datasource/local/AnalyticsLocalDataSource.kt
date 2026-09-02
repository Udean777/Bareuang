package com.ssajudn.bareuang.data.datasource.local

import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint
import com.ssajudn.bareuang.domain.repository.AnalyticsRepository
import com.ssajudn.bareuang.data.error.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsLocalDataSource @Inject constructor(
    private val db: AppDatabase
) {

    suspend fun getCashflowAnalytics(): Result<List<CashflowDataPoint>> = withContext(Dispatchers.IO) {
        try {
            val points = mutableListOf<CashflowDataPoint>()
            val monthFormat = java.text.SimpleDateFormat("yyyy-MM", Locale.US)
            val labelFormat = java.text.SimpleDateFormat("MMM", Locale("id", "ID"))
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.MONTH, -5)
            val fromDate = monthFormat.format(calendar.time) + "-01"
            calendar.add(Calendar.MONTH, 6)
            val toDate = monthFormat.format(calendar.time) + "-01"
            val rows = db.transactionDao().getCashflowByMonth(fromDate, toDate).associateBy { it.month }

            for (i in 5 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -i)
                val monthKey = monthFormat.format(cal.time)
                val label = labelFormat.format(cal.time)
                val row = rows[monthKey]

                points.add(
                    CashflowDataPoint(
                        month = monthKey,
                        label = label,
                        income = row?.income ?: 0L,
                        expense = row?.expense ?: 0L
                    )
                )
            }

            Result.success(points)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }

    suspend fun getNetWorthAnalytics(): Result<List<NetWorthDataPoint>> = withContext(Dispatchers.IO) {
        try {
            val wallets = db.walletDao().getAllWallets()
            val currentNetWorth = wallets.sumOf { it.balance }
            val cashflowResult = getCashflowAnalytics()
            val cashflow = cashflowResult.getOrElse { return@withContext Result.failure(it) }

            val points = ArrayList<NetWorthDataPoint>(cashflow.size)
            for (i in cashflow.indices) {
                points.add(NetWorthDataPoint("", "", 0L))
            }
            var runningNetWorth = currentNetWorth

            for (i in cashflow.indices.reversed()) {
                points[i] = NetWorthDataPoint(
                    month = cashflow[i].month,
                    label = cashflow[i].label,
                    netWorth = runningNetWorth
                )
                val netChange = cashflow[i].income - cashflow[i].expense
                runningNetWorth -= netChange
            }

            Result.success(points)
        } catch (e: Exception) {
            Result.failure(ApiErrorParser.fromThrowable(e))
        }
    }
}
