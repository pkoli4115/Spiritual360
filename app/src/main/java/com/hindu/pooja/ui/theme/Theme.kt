package com.hindu.pooja.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Unified light color scheme using shared constants from Color.kt
private val SaffronColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    primaryContainer = Saffron.copy(alpha = 0.85f),
    onPrimaryContainer = Color.White,
    secondary = IndiaGreen,
    onSecondary = Color.White,
    background = Cream,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun HinduPoojaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SaffronColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
