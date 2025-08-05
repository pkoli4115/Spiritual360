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
import com.hindu.pooja.ui.kids.findit.GameResultScreen
import com.hindu.pooja.ui.login.LoginScreen
import com.hindu.pooja.ui.login.PhoneLoginScreen
import com.hindu.pooja.ui.personal.EditProfileScreen
import com.hindu.pooja.ui.personal.PersonalDetailsScreen
import com.hindu.pooja.ui.screens.*
import com.hindu.pooja.viewmodel.ProfileViewModel
import java.net.URLDecoder

@Composable
fun HinduPoojaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Inject ProfileViewModel ONCE here for all screens that need it!
    val profileViewModel: ProfileViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // 🔐 Login
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onPhoneLoginClick = {
                    navController.navigate(Screen.PhoneLogin.route)   // <-- 2. NAVIGATE TO PHONELOGIN
                }
            )
        }

        // 📱 Phone Login screen
        composable(Screen.PhoneLogin.route) {    // <-- 3. ADD THIS
            PhoneLoginScreen(
                onOtpVerified = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 🏠 Home (passes profileViewModel)
        composable(Screen.Home.route) {
            HomeScreen(navController, profileViewModel)
        }

        // ⭐ Featured
        composable(Screen.Featured.route) {
            TextScreen("Featured")
        }

        // 👶 Kids
        composable(Screen.Kids.route) {
            TextScreen("Kids Zone Coming Soon")
        }

        // 👤 Profile (passes profileViewModel, optional—remove if not needed)
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

        // 👤 First-time user details
        composable(Screen.PersonalDetails.route) {
            PersonalDetailsScreen(
                onSubmitSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PersonalDetails.route) { inclusive = true }
                    }
                }
            )
        }

        // 💳 Billing screen placeholder
        composable(Screen.Billing.route) {
            BillingScreen(
                onPurchaseClick = { /* will implement later */ },
                onRestoreClick = { /* will implement later */ }
            )
        }

        // 📚 Poojas
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

        // 📖 Pooja Detail
        composable(
            route = Screen.PoojaDetail.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = URLDecoder.decode(encoded, "UTF-8")
            PoojaDetailScreen(navController = navController, fileName = fileName)
        }

        // 🎮 Find-It Game
        composable(
            route = Screen.FindItGame.route,
            arguments = listOf(navArgument("levelFile") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelFile = backStackEntry.arguments?.getString("levelFile") ?: ""
            FindItGameScreen(levelFile = levelFile, navController = navController)
        }

        // 🏆 Game Result
        composable(
            route = Screen.GameResult.route,
            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelName = backStackEntry.arguments?.getString("levelName") ?: ""
            GameResultScreen(levelName = levelName, navController = navController)
        }

        // ⚙️ Settings
        composable(Screen.Settings.route) {
            TextScreen("Settings screen will be added here")
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
