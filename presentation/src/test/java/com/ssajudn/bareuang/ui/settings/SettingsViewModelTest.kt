package com.ssajudn.bareuang.ui.settings

import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import com.ssajudn.bareuang.domain.port.BillReminderPreferencesPort
import com.ssajudn.bareuang.domain.port.BillReminderSchedulerPort
import com.ssajudn.bareuang.domain.port.CurrencyPreferencesPort
import com.ssajudn.bareuang.domain.port.ThemePreferencesPort
import com.ssajudn.bareuang.domain.port.TourPreferencesPort
import com.ssajudn.bareuang.domain.port.WidgetPreferencesPort
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class SettingsViewModelTest {
    private val themePreferences = mockk<ThemePreferencesPort>(relaxed = true)
    private val tourPreferences = mockk<TourPreferencesPort>(relaxed = true)
    private val widgetPreferences = mockk<WidgetPreferencesPort>(relaxed = true)
    private val currencyPreferences = mockk<CurrencyPreferencesPort>(relaxed = true)
    private val reminderPreferences = mockk<BillReminderPreferencesPort>(relaxed = true)
    private val reminderScheduler = mockk<BillReminderSchedulerPort>(relaxed = true)
    private val viewModel = SettingsViewModel(
        themePreferences = themePreferences,
        tourPreferences = tourPreferences,
        widgetPreferences = widgetPreferences,
        currencyPreferences = currencyPreferences,
        reminderPreferences = reminderPreferences,
        reminderScheduler = reminderScheduler,
    )

    @Test
    fun `preference changes go to their owning ports`() {
        viewModel.setDarkMode(AppThemeDarkMode.Dark)
        viewModel.setCurrency(AppCurrency.USD)
        viewModel.setHideBalance(true)
        viewModel.resetTour()

        verify { themePreferences.setDarkMode(AppThemeDarkMode.Dark) }
        verify { currencyPreferences.setCurrency(AppCurrency.USD) }
        verify { widgetPreferences.setHideBalance(true) }
        verify { tourPreferences.resetTour() }
    }

    @Test
    fun `reminder time persists and reschedules background work`() {
        viewModel.setReminderTime(hour = 7, minute = 45)

        verify { reminderPreferences.setReminderTime(7, 45) }
        verify { reminderScheduler.scheduleDailyAt(7, 45) }
    }
}
