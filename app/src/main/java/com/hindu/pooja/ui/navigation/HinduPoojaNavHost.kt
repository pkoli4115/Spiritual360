// app/src/main/java/com/hindu/pooja/ui/navigation/HinduPoojaNavHost.kt
package com.hindu.pooja.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
// NEW:
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.hindu.pooja.ui.kids.findit.FindItGameScreen
import com.hindu.pooja.ui.kids.findit.GameResultScreen
import com.hindu.pooja.ui.login.LoginScreen
import com.hindu.pooja.ui.personal.EditProfileScreen
// NEW:
import com.hindu.pooja.ui.personal.FirstTimeProfileScreen
import com.hindu.pooja.ui.screens.*
import com.hindu.pooja.viewmodel.ProfileViewModel
import java.net.URLDecoder

@Composable
fun HinduPoojaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // NEW: first-time profile capture (Name, Email-from-Google, Mobile)
        composable("first_profile") {
            FirstTimeProfileScreen(
                navController = navController,
                onCompletedRoute = Screen.Home.route
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(navController, profileViewModel)
        }

        composable(Screen.Featured.route) { TextScreen("Featured") }
        composable(Screen.Kids.route) { TextScreen("Kids Zone Coming Soon") }
        composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
        composable(Screen.EditProfile.route) { EditProfileScreen(onSaveSuccess = { navController.popBackStack() }) }

        // Billing kept as-is per your request
        composable(Screen.Billing.route) {
            BillingScreen(onPremiumUnlocked = { navController.popBackStack() })
        }

        composable(
            route = Screen.Poojas.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            PoojasScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.Vrathams.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            VrathamsScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.Ashtottaras.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            AshtottarasScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.PoojaDetail.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = URLDecoder.decode(encoded, "UTF-8")
            PoojaDetailScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.FindItGame.route,
            arguments = listOf(navArgument("levelFile") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelFile = backStackEntry.arguments?.getString("levelFile") ?: ""
            FindItGameScreen(levelFile = levelFile, navController = navController)
        }

        composable(
            route = Screen.GameResult.route,
            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelName = backStackEntry.arguments?.getString("levelName") ?: ""
            GameResultScreen(levelName = levelName, navController = navController)
        }

        composable(Screen.Settings.route) { TextScreen("Settings screen will be added here") }
    }
}

@Composable
fun TextScreen(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(32.dp)
    )
}
