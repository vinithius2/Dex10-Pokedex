package com.vinithius.poke10.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cores para o tema claro
private val LightColors = lightColorScheme(
    primary = Color(0xFFFF0000),
    onPrimary = Color.White,
    secondary = Color(0xFFEAEAEA),
    onSecondary = Color(0xFF363636),
    tertiary = Color(0xFF770000),
    onTertiary = Color.White,
    background = Color(0xFFDEDEDE),
    onBackground = Color(0xFF838282),
    surface = Color(0xFFE1E1E1),
    onSurface = Color.Black
)

// Cores para o tema escuro
private val DarkColors = darkColorScheme(
    primary = Color(0xFF110000),
    onPrimary = Color.Black,
    secondary = Color(0xFF5D0000),
    onSecondary = Color.Black,
    tertiary = Color.White,
    onTertiary = Color.Black,
    background = Color(0xFF171717),
    onBackground = Color(0xFF211B1A),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

// Tipografia básica
private val Typography = Typography()

@Composable
fun ThemePoke10(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
