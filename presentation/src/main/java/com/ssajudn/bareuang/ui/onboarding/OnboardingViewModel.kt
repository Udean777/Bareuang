package com.ssajudn.bareuang.ui.onboarding

import androidx.lifecycle.ViewModel
import com.ssajudn.bareuang.data.local.CurrencyPreferences
import com.ssajudn.bareuang.data.local.ThemePreferences
import com.ssajudn.bareuang.data.local.UserSessionManager
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val themePreferences: ThemePreferences,
    private val currencyPreferences: CurrencyPreferences
) : ViewModel() {

    val darkMode: StateFlow<AppThemeDarkMode> = themePreferences.darkMode
    val currency: StateFlow<AppCurrency> = currencyPreferences.currency

    fun setDarkMode(mode: AppThemeDarkMode) {
        themePreferences.setDarkMode(mode)
    }

    fun setCurrency(curr: AppCurrency) {
        currencyPreferences.setCurrency(curr)
    }

    /**
     * Full-offline app: a locally generated session id is the only identity
     * needed. All data stays in Room on this device.
     */
    fun startLocalSession(onStarted: () -> Unit) {
        sessionManager.startGuestSession()
        onStarted()
    }
}
