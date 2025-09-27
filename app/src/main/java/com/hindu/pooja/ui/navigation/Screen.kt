package com.hindu.pooja.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Featured : Screen("featured")
    object Login : Screen("login")
    object Settings : Screen("settings")
    object Kids : Screen("kids")
    object PersonalDetails : Screen("personal_details")
    object EditProfile : Screen("edit_profile")
    object FirstTimeProfile : Screen("first_time_profile")
    object Splash : Screen("splash")

    object PhoneLogin : Screen("phone_login")

    object Poojas : Screen("poojas/{fileName}") {
        fun createRoute(fileName: String): String = "poojas/$fileName"
    }

    object Vrathams : Screen("vrathams/{fileName}") {
        fun createRoute(fileName: String): String = "vrathams/$fileName"
    }

    object Ashtottaras : Screen("ashtottaras/{fileName}") {
        fun createRoute(fileName: String): String = "ashtottaras/$fileName"
    }

    object PoojaDetail : Screen("pooja_detail/{fileName}") {
        fun createRoute(fileName: String) = "pooja_detail/${Uri.encode(fileName)}"
    }

    object FindItGame : Screen("find_it_game/{levelFile}") {
        fun createRoute(levelFile: String) = "find_it_game/$levelFile"
    }

    object GameResult : Screen("game_result/{levelName}") {
        fun createRoute(levelName: String) = "game_result/$levelName"
    }
}
