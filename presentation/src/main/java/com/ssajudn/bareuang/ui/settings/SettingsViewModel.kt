package com.ssajudn.bareuang.ui.settings

import androidx.lifecycle.ViewModel
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import com.ssajudn.bareuang.domain.port.BillReminderPreferencesPort
import com.ssajudn.bareuang.domain.port.BillReminderSchedulerPort
import com.ssajudn.bareuang.domain.port.CurrencyPreferencesPort
import com.ssajudn.bareuang.domain.port.ThemePreferencesPort
import com.ssajudn.bareuang.domain.port.TourPreferencesPort
import com.ssajudn.bareuang.domain.port.WidgetPreferencesPort
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferencesPort,
    private val tourPreferences: TourPreferencesPort,
    private val widgetPreferences: WidgetPreferencesPort,
    private val currencyPreferences: CurrencyPreferencesPort,
    private val reminderPreferences: BillReminderPreferencesPort,
    private val reminderScheduler: BillReminderSchedulerPort,
) : ViewModel() {
    val darkMode get() = themePreferences.darkMode
    val widgetHideBalance get() = widgetPreferences.hideBalance
    val currency get() = currencyPreferences.currency
    val reminderHour get() = reminderPreferences.reminderHour()
    val reminderMinute get() = reminderPreferences.reminderMinute()

    fun setDarkMode(mode: AppThemeDarkMode) = themePreferences.setDarkMode(mode)

    fun setCurrency(currency: AppCurrency) = currencyPreferences.setCurrency(currency)

    fun setHideBalance(hidden: Boolean) = widgetPreferences.setHideBalance(hidden)

    fun setReminderTime(hour: Int, minute: Int) {
        reminderPreferences.setReminderTime(hour, minute)
        reminderScheduler.scheduleDailyAt(hour, minute)
    }

    fun resetTour() = tourPreferences.resetTour()
}
