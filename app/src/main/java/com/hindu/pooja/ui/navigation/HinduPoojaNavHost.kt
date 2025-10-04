package com.hindu.pooja.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.feature.ramakoti.ui.RamakotiScreen
import com.hindu.pooja.feature.ramakoti.ui.RamakotiIntroScreen
import com.hindu.pooja.ui.kids.findit.FindItGameScreen
import com.hindu.pooja.ui.kids.findit.GameResultScreen
import com.hindu.pooja.ui.login.LoginScreen
import com.hindu.pooja.ui.personal.EditProfileScreen
import com.hindu.pooja.ui.personal.FirstTimeProfileScreen
import com.hindu.pooja.ui.screens.*
import com.hindu.pooja.viewmodel.ProfileViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun HinduPoojaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val start = if (FirebaseAuth.getInstance().currentUser == null)
        Screen.Login.route else Screen.Home.route

    NavHost(
        navController = navController,
        startDestination = start,
        modifier = modifier
    ) {
        // --- Core screens ---
        composable(Screen.FirstTimeProfile.route) {
            FirstTimeProfileScreen(navController, onCompletedRoute = Screen.Home.route)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, profileViewModel = profileViewModel)
        }
        composable(Screen.Featured.route) {
            FeaturedScreen(navController = navController)
        }
        composable(Screen.Kids.route) { Text("Kids Zone Coming Soon") }
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                viewModel = profileViewModel,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        composable(Screen.Login.route) { LoginScreen(navController = navController) }
        composable(Screen.Splash.route) { Text("Splash Screen (placeholder)") }

        // --- Ramakoti flow ---
        // Use the existing Screen.Ramakoti to open the INTRO first
        composable(Screen.Ramakoti.route) {
            RamakotiIntroScreen(
                navController = navController,
                onNextRoute = "ramakoti/writer"
            )
        }
        // Writer page route (navigated from Intro NEXT)
        composable("ramakoti/writer") {
            RamakotiScreen(navController = navController)
        }

        // Donations placeholder
        composable(Screen.Donations.route) {
            Text("Donations screen (wire your UPI/flow here)")
        }

        // --- Content routes (Home) ---
        composable(
            route = Screen.Poojas.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName").orEmpty()
            val fileName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            PoojasScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.Vrathams.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName").orEmpty()
            val fileName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            VrathamsScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.Ashtottaras.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName").orEmpty()
            val fileName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            AshtottarasScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.PoojaDetail.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName").orEmpty()
            val fileName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            PoojaDetailScreen(navController = navController, fileName = fileName)
        }

        // ---------- Find-It ----------
        composable(
            route = Screen.FindItGame.route,
            arguments = listOf(navArgument("levelFile") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("levelFile").orEmpty()
            val levelFile = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            FindItGameScreen(levelFile = levelFile, navController = navController)
        }

        // ---------- Game Result ----------
        composable(
            route = Screen.GameResult.route,
            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("levelName").orEmpty()
            val levelName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            GameResultScreen(levelName = levelName, navController = navController)
        }
    }
}
