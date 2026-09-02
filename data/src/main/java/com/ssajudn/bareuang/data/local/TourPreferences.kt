package com.ssajudn.bareuang.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persisted tour-guide state.
 *
 * Own SharedPreferences file (not the session one) for the same reason as
 * ThemePreferences: onboarding reset does not alter this independent prefs file,
 * and the tour-completed flag must survive sign-out.
 */
@Singleton
class TourPreferences @Inject constructor(
    @ApplicationContext context: Context
) : com.ssajudn.bareuang.domain.port.TourPreferencesPort {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override val isTourCompleted: Boolean
        get() = prefs.getBoolean(KEY_TOUR_COMPLETED, false)

    override fun markTourCompleted() {
        prefs.edit { putBoolean(KEY_TOUR_COMPLETED, true) }
    }

    override fun resetTour() {
        prefs.edit { putBoolean(KEY_TOUR_COMPLETED, false) }
    }

    companion object {
        private const val PREF_NAME = "bareuang_tour"
        private const val KEY_TOUR_COMPLETED = "tour_completed"

    }
}
