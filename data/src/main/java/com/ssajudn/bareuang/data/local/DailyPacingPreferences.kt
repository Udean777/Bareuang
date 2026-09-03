package com.ssajudn.bareuang.data.local

import android.content.Context
import androidx.core.content.edit
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyPacingPreferences @Inject constructor(
    @ApplicationContext context: Context
) : DailyPacingPreferencesPort {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val _customTarget = MutableStateFlow(readTarget())

    override val customTarget: StateFlow<Long?> = _customTarget.asStateFlow()

    override fun setCustomTarget(amount: Long?) {
        prefs.edit {
            if (amount == null) remove(KEY_TARGET) else putLong(KEY_TARGET, amount)
        }
        _customTarget.value = amount
    }

    override fun reset() {
        prefs.edit { remove(KEY_TARGET) }
        _customTarget.value = null
    }

    private fun readTarget(): Long? =
        if (prefs.contains(KEY_TARGET)) prefs.getLong(KEY_TARGET, 0L).takeIf { it > 0L } else null

    private companion object {
        const val PREF_NAME = "bareuang_daily_pacing"
        const val KEY_TARGET = "custom_target"
    }
}
