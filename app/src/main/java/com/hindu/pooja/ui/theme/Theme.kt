package com.hindu.pooja.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Saffron = Color(0xFFFF9933)
private val SaffronDark = Color(0xFFB35C00)
private val SaffronLight = Color(0xFFFFD180)
private val TextPrimary = Color(0xFF2D1406)

private val SaffronColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = Color.White,
    primaryContainer = SaffronLight,
    onPrimaryContainer = TextPrimary,
    secondary = SaffronDark,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    error = Color(0xFFB00020),
    onError = Color.White,
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
