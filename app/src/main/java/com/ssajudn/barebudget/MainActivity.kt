package com.ssajudn.barebudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.barebudget.data.local.ThemePreferences
import com.ssajudn.barebudget.ui.navigation.AppNavigation
import com.ssajudn.barebudget.domain.model.AppThemeDarkMode
import com.ssajudn.barebudget.ui.theme.BareBudgetTheme
import com.ssajudn.barebudget.ui.theme.ThemeDarkMode
import com.ssajudn.barebudget.widget.BudgetWidgetWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePrefs: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by themePrefs.darkMode.collectAsStateWithLifecycle()

            val darkTheme = when (darkMode) {
                AppThemeDarkMode.FollowSystem -> isSystemInDarkTheme()
                AppThemeDarkMode.Light -> false
                AppThemeDarkMode.Dark -> true
            }

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }
            }

            BareBudgetTheme(
                darkMode = ThemeDarkMode.valueOf(darkMode.name),
            ) {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        BudgetWidgetWorker.runNow(this)
    }
}
