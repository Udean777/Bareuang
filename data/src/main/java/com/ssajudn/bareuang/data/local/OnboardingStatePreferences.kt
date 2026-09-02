package com.ssajudn.bareuang.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Stores only whether this single-device profile has completed onboarding. */
@Singleton
class OnboardingStatePreferences @Inject constructor(
    @ApplicationContext context: Context
) : com.ssajudn.bareuang.domain.port.OnboardingStatePort {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, value) }

    override fun completeOnboarding() { isOnboardingCompleted = true }

    override fun resetOnboarding() { isOnboardingCompleted = false }

    private companion object {
        const val PREF_NAME = "bareuang_onboarding"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
