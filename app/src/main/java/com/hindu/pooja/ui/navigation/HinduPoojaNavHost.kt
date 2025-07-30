package com.hindu.pooja.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hindu.pooja.ui.kids.findit.FindItGameScreen
import com.hindu.pooja.ui.login.LoginScreen
import com.hindu.pooja.ui.personal.EditProfileScreen
import com.hindu.pooja.ui.personal.PersonalDetailsScreen
import com.hindu.pooja.ui.screens.*
import java.net.URLDecoder

@Composable
fun HinduPoojaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // 🔐 Login route
        composable(route = Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onPhoneLoginClick = {
                    // TODO: Handle phone login if needed
                }
            )
        }


        // 🏠 Home
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        // ⭐ Featured
        composable(Screen.Featured.route) {
            TextScreen("Featured")
        }

        // 👶 Kids Zone
        composable(Screen.Kids.route) {
            TextScreen("Kids Zone")
        }

        // 👤 Profile
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        // ✏️ Edit Profile
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // 📋 Personal Details (first-time users)
        composable(Screen.PersonalDetails.route) {
            PersonalDetailsScreen(
                onSubmitSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PersonalDetails.route) { inclusive = true }
                    }
                }
            )
        }

        // 📚 Pooja list
        composable(
            route = Screen.Poojas.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            PoojasScreen(navController = navController, fileName = fileName)
        }

        // 📚 Vrathams
        composable(
            route = Screen.Vrathams.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            VrathamsScreen(navController = navController, fileName = fileName)
        }

        // 📚 Ashtottaras
        composable(
            route = Screen.Ashtottaras.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            AshtottarasScreen(navController = navController, fileName = fileName)
        }

        // 📖 Pooja detail
        composable(
            route = Screen.PoojaDetail.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = URLDecoder.decode(encoded, "UTF-8")
            PoojaDetailScreen(navController = navController, fileName = fileName)
        }

        // 🧩 Find-It Game
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
    Text(
        text = label,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(32.dp)
    )
}
