package com.hindu.pooja.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hindu.pooja.ui.billing.BillingScreen
import com.hindu.pooja.ui.kids.findit.FindItGameScreen
import com.hindu.pooja.ui.kids.findit.GameResultScreen
import com.hindu.pooja.ui.login.LoginScreen
import com.hindu.pooja.ui.login.PhoneLoginScreen
import com.hindu.pooja.ui.personal.PersonalDetailsScreen
import com.hindu.pooja.ui.profile.ProfileScreen
import com.hindu.pooja.ui.screens.FeaturedScreen
import com.hindu.pooja.ui.screens.HomeScreen
import com.hindu.pooja.ui.splash.SplashScreen
import com.hindu.pooja.viewmodel.LoginViewModel

@Composable
fun HinduPoojaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }

        composable("login") {
            LoginScreen(
                viewModel = hiltViewModel<LoginViewModel>(),
                onLoginSuccess = {
                    navController.navigate("splash") { // ✅ Re-check session and profile
                        popUpTo("login") { inclusive = true }
                    }
                },
                onPhoneLoginClick = {
                    navController.navigate("phoneLogin")
                }
            )
        }

        composable("phoneLogin") {
            PhoneLoginScreen(
                onOtpVerified = {
                    navController.navigate("splash") { // ✅ Use splash logic after OTP too
                        popUpTo("phoneLogin") { inclusive = true }
                    }
                }
            )
        }

        composable("personalDetails") {
            PersonalDetailsScreen(
                onSubmitSuccess = {
                    navController.navigate("home") {
                        popUpTo("personalDetails") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(navController = navController)
        }

        // ✅ NEW: Profile screen route
        composable("profile") {
            ProfileScreen(navController = navController)
        }

        // ✅ NEW: Billing screen route
        composable("billing") {
            BillingScreen()
        }
        // ✅ NEW: Featured screen route

        composable("featured") {
            FeaturedScreen()
        }

        composable("find_it_game/{levelFile}") {
            val levelFile = it.arguments?.getString("levelFile") ?: "hidden_objects_shiva_scene.json"
            FindItGameScreen(navController = navController, levelFile = levelFile)
        }

        composable("game_result/{levelName}") {
            val levelName = it.arguments?.getString("levelName") ?: "Unknown"
            GameResultScreen(levelName = levelName, navController = navController)
        }

    }

    }
