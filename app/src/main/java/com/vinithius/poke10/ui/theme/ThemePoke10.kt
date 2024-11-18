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
    primary = Color(0xFFFF5722), // Laranja avermelhado
    onPrimary = Color.White,    // Texto sobre a cor primária
    background = Color(0xFFFFF9C4), // Amarelo bem claro
    onBackground = Color(0xFF3E2723), // Marrom escuro para contraste
    surface = Color(0xFFFFECB3), // Amarelo suave
    onSurface = Color.Black     // Texto em superfícies claras
)

// Cores para o tema escuro
private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF8A65), // Laranja mais claro para modo escuro
    onPrimary = Color.Black,    // Texto escuro para contraste no modo escuro
    background = Color(0xFF303030), // Fundo cinza escuro
    onBackground = Color(0xFFFFCCBC), // Texto com tom de laranja suave
    surface = Color(0xFF424242), // Superfícies cinza médio
    onSurface = Color.White     // Texto em superfícies escuras
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
