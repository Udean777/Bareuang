package com.ssajudn.bareuang.data.repository

import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint
import com.ssajudn.bareuang.data.datasource.local.AnalyticsLocalDataSource
import com.ssajudn.bareuang.domain.repository.AnalyticsRepository
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
