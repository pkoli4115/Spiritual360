package com.hindu.pooja.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.feature.ramakoti.ui.CertificateScreen
import com.hindu.pooja.feature.ramakoti.ui.LanguageSelectionScreen
import com.hindu.pooja.feature.ramakoti.ui.RamakotiHomeScreen
import com.hindu.pooja.feature.ramakoti.ui.RamakotiWriterScreen
import com.hindu.pooja.ui.login.LoginScreen
import com.hindu.pooja.ui.personal.EditProfileScreen
import com.hindu.pooja.ui.personal.FirstTimeProfileScreen
import com.hindu.pooja.ui.ramayana.ramakoti.RamakotiIntroScreen
import com.hindu.pooja.ui.screens.ProfileScreen
import com.hindu.pooja.ui.screens.SplashScreen
import com.hindu.pooja.viewmodel.ProfileViewModel

@Composable
fun HinduPoojaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {

        /* ---------------- Base routes ---------------- */

        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.FirstTimeProfile.route) {
            FirstTimeProfileScreen(
                navController = navController,
                onCompletedRoute = Screen.Home.route
            )
        }

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

        /* ---------------- RAMAKOTI ---------------- */

        // Home dashboard
        composable(Screen.Home.route) {
            RamakotiHomeScreen(navController = navController)
        }

        // Maa Asayam / Intro screen
        composable(Screen.RamakotiIntro.route) {
            RamakotiIntroScreen(
                navController = navController,
                onNextRoute = Screen.RamakotiWriter.route
            )
        }

        // Language selection
        composable(Screen.RamakotiLanguage.route) {
            LanguageSelectionScreen(navController)
        }

        // Writer screen
        composable(Screen.RamakotiWriter.route) {
            RamakotiWriterScreen(
                onPickNextTarget = {
                    navController.navigate(Screen.RamakotiLanguage.route)
                }
            )
        }

        // Certificate screen
        composable(Screen.RamakotiCertificate.route) {
            CertificateScreen(
                milestoneCountText = "1 Crore Sri Rama Namas Completed"
            )
        }
    }
}