package com.ssajudn.bareuang.ui.onboarding

import androidx.lifecycle.ViewModel
import com.ssajudn.bareuang.data.local.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionManager: UserSessionManager
) : ViewModel() {

    /**
     * Full-offline app: a locally generated session id is the only identity
     * needed. All data stays in Room on this device.
     */
    fun startLocalSession(onStarted: () -> Unit) {
        sessionManager.startGuestSession()
        onStarted()
    }
}
