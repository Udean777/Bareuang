package com.ssajudn.bareuang.ui.onboarding

import androidx.lifecycle.ViewModel
import com.ssajudn.bareuang.domain.port.CurrencyPreferencesPort
import com.ssajudn.bareuang.domain.port.ThemePreferencesPort
import com.ssajudn.bareuang.domain.port.OnboardingStatePort
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingState: OnboardingStatePort,
    private val themePreferences: ThemePreferencesPort,
    private val currencyPreferences: CurrencyPreferencesPort
) : ViewModel() {

    val darkMode: StateFlow<AppThemeDarkMode> = themePreferences.darkMode
    val currency: StateFlow<AppCurrency> = currencyPreferences.currency

    fun setDarkMode(mode: AppThemeDarkMode) {
        themePreferences.setDarkMode(mode)
    }

    fun setCurrency(curr: AppCurrency) {
        currencyPreferences.setCurrency(curr)
    }

    /** Marks local onboarding complete; the app has no account or cloud profile. */
    fun startLocalSession(onStarted: () -> Unit) {
        onboardingState.completeOnboarding()
        onStarted()
    }
}
