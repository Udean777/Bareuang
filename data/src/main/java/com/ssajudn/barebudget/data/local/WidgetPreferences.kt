package com.ssajudn.barebudget.data.local

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Widget home screen settings. Same SharedPreferences approach as
 * [ThemePreferences] — survives sign-out and is readable from the widget
 * worker without a session.
 */
@Singleton
class WidgetPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _hideBalance = MutableStateFlow(prefs.getBoolean(KEY_HIDE_BALANCE, false))
    val hideBalance: StateFlow<Boolean> = _hideBalance.asStateFlow()

    fun setHideBalance(hidden: Boolean) {
        prefs.edit { putBoolean(KEY_HIDE_BALANCE, hidden) }
        _hideBalance.value = hidden
    }

    companion object {
        private const val PREF_NAME = "bare_budget_widget"
        private const val KEY_HIDE_BALANCE = "hide_balance"

    }
}
