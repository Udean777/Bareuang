package com.ssajudn.bareuang.ui.splash

import androidx.lifecycle.ViewModel
import com.ssajudn.bareuang.domain.port.OnboardingStatePort
import com.ssajudn.bareuang.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Determines the app's start destination from the persisted session.
 *
 * Reads the local onboarding state from the Hilt-provided singleton.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingState: OnboardingStatePort
) : ViewModel() {

    /** The route the app should navigate to after the splash animation. */
    fun computeStartDestination(): String = when {
        !onboardingState.isOnboardingCompleted -> Screen.Onboarding.route
        else -> Screen.Dashboard.route
    }
}
