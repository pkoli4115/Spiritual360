package com.hindu.pooja.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColors = lightColorScheme()
private val DarkColors = darkColorScheme()

@Composable
fun HinduPoojaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography.copy(
            // You can customize your font scaling here if you want!
            // Example:
            // headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp * fontScale),
            // bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp * fontScale),
        ),
        content = content
    )
}
