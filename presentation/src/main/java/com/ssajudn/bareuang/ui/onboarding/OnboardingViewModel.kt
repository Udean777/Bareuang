package com.ssajudn.bareuang.ui.onboarding

import androidx.lifecycle.ViewModel
import com.ssajudn.bareuang.data.local.ThemePreferences
import com.ssajudn.bareuang.data.local.UserSessionManager
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val darkMode: StateFlow<AppThemeDarkMode> = themePreferences.darkMode

    fun setDarkMode(mode: AppThemeDarkMode) {
        themePreferences.setDarkMode(mode)
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
