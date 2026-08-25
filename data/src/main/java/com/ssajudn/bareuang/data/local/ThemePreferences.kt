package com.ssajudn.bareuang.data.local

import android.content.Context
import androidx.core.content.edit
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _darkMode = MutableStateFlow(readDarkMode())
    val darkMode: StateFlow<AppThemeDarkMode> = _darkMode.asStateFlow()

    fun setDarkMode(mode: AppThemeDarkMode) {
        prefs.edit { putString(KEY_DARK_MODE, mode.name) }
        _darkMode.value = mode
    }

    private fun readDarkMode(): AppThemeDarkMode =
        prefs.getString(KEY_DARK_MODE, null)
            ?.let { name -> AppThemeDarkMode.entries.firstOrNull { it.name == name } }
            ?: AppThemeDarkMode.FollowSystem

    companion object {
        private const val PREF_NAME = "bareuang_appearance"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
