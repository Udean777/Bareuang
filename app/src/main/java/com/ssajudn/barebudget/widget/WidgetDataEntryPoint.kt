package com.ssajudn.barebudget.widget

import com.ssajudn.barebudget.data.local.WidgetPreferences
import com.ssajudn.barebudget.domain.usecase.GetDashboardSummaryUseCase
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
}
