package com.hindu.pooja.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Billing : Screen("billing")
    object Featured : Screen("featured")
    object Login : Screen("login")
    object Settings : Screen("settings")
    object Kids : Screen("kids")

    object Poojas : Screen("poojas/{fileName}") {
        fun createRoute(fileName: String): String = "poojas/$fileName"
    }

    object PoojaDetail : Screen("pooja_detail/{fileName}") {
        fun createRoute(fileName: String): String = "pooja_detail/$fileName"
    }

    object FindItGame : Screen("find_it_game/{levelFile}") {
        fun createRoute(levelFile: String) = "find_it_game/$levelFile"
    }

    object GameResult : Screen("game_result/{levelName}") {
        fun createRoute(levelName: String) = "game_result/$levelName"
    }

    object Splash : Screen("splash")
}
