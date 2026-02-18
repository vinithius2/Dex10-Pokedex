package com.vinithius.dex10.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import com.vinithius.dex10.datasource.data.AppPreferences

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface
)

val ColorScheme.text: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkText else LightText

@Composable
fun ThemeDex10(
    darkTheme: Boolean = isSystemInDarkTheme(),
    darkModeOverride: Int = AppPreferences.DARK_MODE_SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (darkModeOverride) {
        AppPreferences.DARK_MODE_ON -> true
        AppPreferences.DARK_MODE_OFF -> false
        else -> darkTheme
    }
    val colorScheme = if (isDark) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

