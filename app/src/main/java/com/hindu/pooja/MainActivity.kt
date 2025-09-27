package com.hindu.pooja

import android.content.Intent
import android.os.Bundle
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        // ✅ Shared CallbackManager instance for FB login
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
        // ✅ Forward to shared callback manager
        fbCallbackManager.onActivityResult(requestCode, resultCode, data)
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
