package com.hindu.pooja.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hindu.pooja.viewmodel.SplashNavigation
import com.hindu.pooja.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel.navigationState) {
        viewModel.navigationState.collect { nav ->
            when (nav) {
                is SplashNavigation.ToLogin -> navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
                is SplashNavigation.ToPersonalDetails -> navController.navigate("personalDetails") {
                    popUpTo("splash") { inclusive = true }
                }
                is SplashNavigation.ToHome -> navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
                else -> Unit
            }
        }
    }
}
