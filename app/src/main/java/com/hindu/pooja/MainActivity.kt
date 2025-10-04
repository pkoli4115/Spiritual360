package com.hindu.pooja

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.hindu.pooja.ui.navigation.BottomNavItem
import com.hindu.pooja.ui.navigation.BottomNavigationBar
import com.hindu.pooja.ui.navigation.HinduPoojaNavHost
import com.hindu.pooja.ui.theme.HinduPoojaTheme
import dagger.hilt.android.AndroidEntryPoint
import com.hindu.pooja.R

// Facebook
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.CallbackManager

// ---- Ramakoti audio key bus (single-file helper) ----
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

enum class VolumeEvent { UP, DOWN }

object RamakotiAudioKeyBus {
    // only capture keys when a listener on Ramakoti screen is active & audio mode is ON
    @Volatile private var active: Boolean = false
    fun setActive(enabled: Boolean) { active = enabled }

    val events = MutableSharedFlow<VolumeEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun emit(e: VolumeEvent): Boolean {
        if (!active) return false
        events.tryEmit(e)
        return true
    }
}
// -----------------------------------------------------

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        lateinit var fbCallbackManager: CallbackManager
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Facebook SDK init
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)

        fbCallbackManager = CallbackManager.Factory.create()

        setContent {
            HinduPoojaAppContent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fbCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    // Intercept hardware volume keys while Audio Mode is active on Ramakoti screen
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    val consumed = RamakotiAudioKeyBus.emit(VolumeEvent.UP)
                    if (consumed) return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val consumed = RamakotiAudioKeyBus.emit(VolumeEvent.DOWN)
                    if (consumed) return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}

@Composable
fun HinduPoojaAppContent() {
    HinduPoojaTheme {
        val navController = rememberNavController()

        val bottomNavItems = listOf(
            BottomNavItem("home", R.drawable.ic_home, R.string.nav_home),
            BottomNavItem("featured", R.drawable.ic_star, R.string.nav_featured),
            BottomNavItem("kids", R.drawable.ic_kids, R.string.nav_kids),
            BottomNavItem("profile", R.drawable.ic_profile, R.string.nav_profile)
        )

        val currentBackStackEntry by navController.currentBackStackEntryFlow
            .collectAsState(initial = navController.currentBackStackEntry)
        val currentRoute = currentBackStackEntry?.destination?.route

        val showBottomBar = currentRoute in bottomNavItems.map { it.route }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(
                        navController = navController,
                        items = bottomNavItems
                    )
                }
            }
        ) { innerPadding ->
            HinduPoojaNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
