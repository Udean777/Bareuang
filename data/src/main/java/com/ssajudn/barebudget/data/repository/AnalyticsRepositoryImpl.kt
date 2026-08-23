package com.ssajudn.barebudget.data.repository

import com.ssajudn.barebudget.domain.model.CashflowDataPoint
import com.ssajudn.barebudget.domain.model.NetWorthDataPoint
import com.ssajudn.barebudget.data.datasource.local.AnalyticsLocalDataSource
import com.ssajudn.barebudget.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val local: AnalyticsLocalDataSource
) : AnalyticsRepository {

    override suspend fun getCashflowAnalytics(): Result<List<CashflowDataPoint>> =
        local.getCashflowAnalytics()

    override suspend fun getNetWorthAnalytics(): Result<List<NetWorthDataPoint>> =
        local.getNetWorthAnalytics()
}
