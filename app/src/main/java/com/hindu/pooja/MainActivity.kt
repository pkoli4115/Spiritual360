// File: app/src/main/java/com/hindu/pooja/MainActivity.kt
package com.hindu.pooja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.hindu.pooja.ui.navigation.HinduPoojaNavHost
import com.hindu.pooja.ui.theme.HinduPoojaTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            HinduPoojaAppContent() // ✅ Renamed to avoid name conflict with Application class
        }
    }
}

@Composable
fun HinduPoojaAppContent() {
    HinduPoojaTheme {
        val navController = rememberNavController()

        Surface {
            HinduPoojaNavHost(
                navController = navController,
                modifier = Modifier
            )
        }
    }
}
