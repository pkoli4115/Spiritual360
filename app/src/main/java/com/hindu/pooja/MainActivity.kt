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
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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

// Ramakoti audio key bus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

// In-app updates
import com.hindu.pooja.util.UpdateManager

enum class VolumeEvent { UP, DOWN }

object RamakotiAudioKeyBus {
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        lateinit var fbCallbackManager: CallbackManager
            private set
    }

    @Volatile
    private var splashReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ system splash
        val splash: SplashScreen = installSplashScreen()
        splash.setKeepOnScreenCondition { !splashReady }

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Facebook SDK init
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)
        fbCallbackManager = CallbackManager.Factory.create()

        setContent {
            HinduPoojaAppContent()
        }

        splashReady = true
    }

    override fun onStart() {
        super.onStart()
        // Prompt updates when user opens the app.
        // Set preferImmediate=true only for critical releases you want to block on.
        UpdateManager.checkAndPrompt(this, preferImmediate = false)
    }

    override fun onResume() {
        super.onResume()
        // Resume any in-progress IMMEDIATE update flow (required by Play)
        UpdateManager.resumeIfNeeded(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fbCallbackManager.onActivityResult(requestCode, resultCode, data)
        UpdateManager.onActivityResult(requestCode, resultCode)
    }

    // Handle volume keys via onKeyDown (avoid restricted dispatchKeyEvent)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> if (RamakotiAudioKeyBus.emit(VolumeEvent.UP)) return true
            KeyEvent.KEYCODE_VOLUME_DOWN -> if (RamakotiAudioKeyBus.emit(VolumeEvent.DOWN)) return true
        }
        return super.onKeyDown(keyCode, event)
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
