package com.hindu.pooja.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hindu.pooja.ui.kids.findit.FindItGameScreen
import com.hindu.pooja.ui.screens.*
import java.net.URLDecoder
import com.hindu.pooja.ui.screens.PoojaDetailScreen


@Composable
fun HinduPoojaNavHost(
    navController: androidx.navigation.NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(baseViewModel = hiltViewModel())
        }
        composable(Screen.Featured.route) {
            TextScreen("Featured")
        }
        composable(Screen.Kids.route) {
            TextScreen("Kids Zone")
        }

        // Full list of poojas in a section
        composable(
            route = Screen.Poojas.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            PoojasScreen(navController = navController, fileName = fileName)
        }

        // Pooja detail screen with URL decoding
        composable(
            route = Screen.PoojaDetail.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedFile = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = URLDecoder.decode(encodedFile, "UTF-8")
            PoojaDetailScreen(navController = navController, fileName = fileName)
        }

        // Find-It Game screen (unchanged)
        composable(
            route = Screen.FindItGame.route,
            arguments = listOf(navArgument("levelFile") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelFile = backStackEntry.arguments?.getString("levelFile") ?: ""
            FindItGameScreen(levelFile = levelFile, navController = navController)
        }
    }
}

@Composable
fun TextScreen(label: String) {
    androidx.compose.material3.Text(
        text = label,
        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(32.dp)
    )
}