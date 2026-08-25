package com.ssajudn.bareuang.domain.repository

import com.ssajudn.bareuang.domain.model.CashflowDataPoint
import com.ssajudn.bareuang.domain.model.NetWorthDataPoint

interface AnalyticsRepository {
    suspend fun getCashflowAnalytics(): Result<List<CashflowDataPoint>>
    suspend fun getNetWorthAnalytics(): Result<List<NetWorthDataPoint>>
}
