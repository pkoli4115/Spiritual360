package com.hindu.pooja.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object PersonalDetails : Screen("personalDetails")
    object Home : Screen("home")
    object Poojas : Screen("poojas")
    object Vrathams : Screen("vrathams")
    object PoojaDetail : Screen("poojaDetail/{fileName}/{imageRes}") {
        fun createRoute(fileName: String, imageRes: String): String {
            return "poojaDetail/$fileName/$imageRes"
        }
    }
    object LanguageSelector : Screen("languageSelector")
    object TextZoom : Screen("textZoom")
    object Settings : Screen("settings")
    object Festivals : Screen("festivals")
    object Profile : Screen("profile")
}
