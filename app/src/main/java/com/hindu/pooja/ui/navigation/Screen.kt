package com.hindu.pooja.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    // Core tabs
    object Home : Screen("home")
    object Featured : Screen("featured")        // Tab + page
    object Kids : Screen("kids")
    object Profile : Screen("profile")
    object Settings : Screen("settings")

    // Auth
    object Login : Screen("login")
    object PhoneLogin : Screen("phone_login")
    object FirstTimeProfile : Screen("first_time_profile")
    object EditProfile : Screen("edit_profile")
    object PersonalDetails : Screen("personal_details")
    object Splash : Screen("splash")

    // 🔹 Featured → Ramakoti
    object Ramakoti : Screen("featured/ramakoti")

    // 🔹 Donations (from Donate CTA)
    object Donations : Screen("donations")

    // Pooja categories
    object Poojas : Screen("poojas/{fileName}") {
        fun createRoute(fileName: String): String = "poojas/$fileName"
    }

    object Vrathams : Screen("vrathams/{fileName}") {
        fun createRoute(fileName: String): String = "vrathams/$fileName"
    }

    object Ashtottaras : Screen("ashtottaras/{fileName}") {
        fun createRoute(fileName: String): String = "ashtottaras/$fileName"
    }

    // Pooja detail
    object PoojaDetail : Screen("pooja_detail/{fileName}") {
        fun createRoute(fileName: String) = "pooja_detail/${Uri.encode(fileName)}"
    }

    // Games
    object FindItGame : Screen("find_it_game/{levelFile}") {
        fun createRoute(levelFile: String) = "find_it_game/${Uri.encode(levelFile)}"
    }

    object GameResult : Screen("game_result/{levelName}") {
        fun createRoute(levelName: String) = "game_result/${Uri.encode(levelName)}"
    }
}
