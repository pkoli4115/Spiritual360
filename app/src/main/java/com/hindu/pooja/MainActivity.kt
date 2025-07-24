package com.hindu.pooja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.hindu.pooja.ui.navigation.BottomNavItem
import com.hindu.pooja.ui.navigation.HinduPoojaNavHost
import com.hindu.pooja.ui.theme.HinduPoojaTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            HinduPoojaAppContent()
        }
    }
}

@Composable
fun HinduPoojaAppContent() {
    HinduPoojaTheme {
        val navController = rememberNavController()
        var selectedItem by remember { mutableStateOf(0) }

        val bottomNavItems = listOf(
            BottomNavItem.Home,
            BottomNavItem.Featured,
            BottomNavItem.Profile
        )

        val currentRoute = navController
            .currentBackStackEntryFlow
            .collectAsState(initial = navController.currentBackStackEntry)
            .value?.destination?.route

        LaunchedEffect(currentRoute) {
            println("✅ Current route: $currentRoute")
        }

        val showBottomBar = currentRoute != null && currentRoute in bottomNavItems.map { it.route }

        if (showBottomBar) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        bottomNavItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(id = item.icon),
                                        contentDescription = null
                                    )
                                },
                                label = { Text(text = stringResource(id = item.label)) },
                                selected = selectedItem == index,
                                onClick = {
                                    selectedItem = index
                                    navController.navigate(item.route) {
                                        popUpTo("home") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                HinduPoojaNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        } else {
            HinduPoojaNavHost(
                navController = navController,
                modifier = Modifier
            )
        }
    }
}
