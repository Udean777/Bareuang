package com.ssajudn.bareuang.widget

import com.ssajudn.bareuang.data.local.WidgetPreferences
import com.ssajudn.bareuang.domain.usecase.GetDashboardSummaryUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Glance receivers can't be @AndroidEntryPoint, so workers/receiver access
 * the graph through this entry point instead.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetDataEntryPoint {
    fun getDashboardSummary(): GetDashboardSummaryUseCase
    fun widgetPreferences(): WidgetPreferences
    fun currencyPreferences(): com.ssajudn.bareuang.data.local.CurrencyPreferences
}
