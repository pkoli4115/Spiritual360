package com.hindu.pooja.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Profile : Screen("profile")
    object Billing : Screen("billing")
    // Add others as needed
    companion object {
    }
}
