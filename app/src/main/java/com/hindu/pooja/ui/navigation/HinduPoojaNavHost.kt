package com.hindu.pooja.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hindu.pooja.ui.login.LoginScreen
import com.hindu.pooja.ui.login.PhoneLoginScreen
import com.hindu.pooja.ui.personal.PersonalDetailsScreen
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
    }
}
