package com.ssajudn.barebudget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeDarkMode {
    FollowSystem,
    Light,
    Dark,
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF845400),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF4A216),
    onPrimaryContainer = Color(0xFF623E00),
    secondary = Color(0xFF396842),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFBAF0BF),
    onSecondaryContainer = Color(0xFF3F6F48),
    tertiary = Color(0xFF7A5648),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6A998),
    onTertiaryContainer = Color(0xFF5E3D31),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFDF9F3),
    onBackground = Color(0xFF1C1C18),
    surface = Color(0xFFFDF9F3),
    onSurface = Color(0xFF1C1C18),
    surfaceVariant = Color(0xFFE6E2DC),
    onSurfaceVariant = Color(0xFF524434),
    outline = Color(0xFF857461),
    outlineVariant = Color(0xFFD8C3AD),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF31302D),
    inverseOnSurface = Color(0xFFF4F0EA),
    inversePrimary = Color(0xFFFFB958),
    surfaceDim = Color(0xFFDDDAD4),
    surfaceBright = Color(0xFFFDF9F3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F3ED),
    surfaceContainer = Color(0xFFF1EDE7),
    surfaceContainerHigh = Color(0xFFEBE8E2),
    surfaceContainerHighest = Color(0xFFE6E2DC),
    primaryFixed = Color(0xFFFFDDB5),
    primaryFixedDim = Color(0xFFFFB958),
    onPrimaryFixed = Color(0xFF2A1800),
    onPrimaryFixedVariant = Color(0xFF643F00),
    secondaryFixed = Color(0xFFBAF0BF),
    secondaryFixedDim = Color(0xFF9FD3A4),
    onSecondaryFixed = Color(0xFF00210A),
    onSecondaryFixedVariant = Color(0xFF21502C),
    tertiaryFixed = Color(0xFFFFDBCE),
    tertiaryFixedDim = Color(0xFFEBBCAB),
    onTertiaryFixed = Color(0xFF2E150A),
    onTertiaryFixedVariant = Color(0xFF5F3F32),
    surfaceTint = Color(0xFF845400),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB958),
    onPrimary = Color(0xFF2A1800),
    primaryContainer = Color(0xFF623E00),
    onPrimaryContainer = Color(0xFFFFDDB5),
    secondary = Color(0xFF9FD3A4),
    onSecondary = Color(0xFF00210A),
    secondaryContainer = Color(0xFF21502C),
    onSecondaryContainer = Color(0xFFBAF0BF),
    tertiary = Color(0xFFD6A998),
    onTertiary = Color(0xFF2E150A),
    tertiaryContainer = Color(0xFF5E3D31),
    onTertiaryContainer = Color(0xFFFFDBCE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1C18),
    onBackground = Color(0xFFF4F0EA),
    surface = Color(0xFF1C1C18),
    onSurface = Color(0xFFF4F0EA),
    surfaceVariant = Color(0xFF524434),
    onSurfaceVariant = Color(0xFFD8C3AD),
    outline = Color(0xFF857461),
    outlineVariant = Color(0xFF524434),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFFDF9F3),
    inverseOnSurface = Color(0xFF31302D),
    inversePrimary = Color(0xFF845400),
    surfaceDim = Color(0xFF1C1C18),
    surfaceBright = Color(0xFF3A3935),
    surfaceContainerLowest = Color(0xFF1B1B18),
    surfaceContainerLow = Color(0xFF252420),
    surfaceContainer = Color(0xFF292826),
    surfaceContainerHigh = Color(0xFF33322E),
    surfaceContainerHighest = Color(0xFF3E3D39),
    primaryFixed = Color(0xFFFFDDB5),
    primaryFixedDim = Color(0xFFFFB958),
    onPrimaryFixed = Color(0xFF2A1800),
    onPrimaryFixedVariant = Color(0xFF643F00),
    secondaryFixed = Color(0xFFBAF0BF),
    secondaryFixedDim = Color(0xFF9FD3A4),
    onSecondaryFixed = Color(0xFF00210A),
    onSecondaryFixedVariant = Color(0xFF21502C),
    tertiaryFixed = Color(0xFFFFDBCE),
    tertiaryFixedDim = Color(0xFFEBBCAB),
    onTertiaryFixed = Color(0xFF2E150A),
    onTertiaryFixedVariant = Color(0xFF5F3F32),
    surfaceTint = Color(0xFFFFB958),
)

private val LocalCategoryColors = staticCompositionLocalOf { LightCategoryColors }

val categoryColors: CategoryColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCategoryColors.current

@Composable
fun BareBudgetTheme(
    darkMode: ThemeDarkMode = ThemeDarkMode.FollowSystem,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (darkMode) {
        ThemeDarkMode.FollowSystem -> isSystemInDarkTheme()
        ThemeDarkMode.Light -> false
        ThemeDarkMode.Dark -> true
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalCategoryColors provides if (darkTheme) DarkCategoryColors else LightCategoryColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
