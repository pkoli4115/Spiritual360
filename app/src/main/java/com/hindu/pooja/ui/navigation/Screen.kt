package com.hindu.pooja.ui.navigation

import android.net.Uri
import com.hindu.pooja.feature.quiz.AyodhyaKandaQuizRepo
import com.hindu.pooja.feature.quiz.BalaKandaQuizScreen

sealed class Screen(val route: String) {
    // Tabs
    object Home : Screen("home")
    object Featured : Screen("featured")
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

    // Featured → Ramakoti (unchanged)
    object Ramakoti : Screen("featured/ramakoti")

    // eLearning
    object BalaKandaFlip : Screen("featured/balakanda")                 // (keep if you still want flips)
    object BalaKandaWikiSimple : Screen("featured/balakanda/wiki")      // reader
    object BalaKandaQuiz : Screen("featured/balakanda/quiz")            // quiz

    // NEW: Ayodhya Kanda reader route (matches NavHost "ramayana/ayodhya/wiki")
    object AyodhyaKandaWiki : Screen("ramayana/ayodhya/wiki")
    object AyodhyaKandaQuiz : Screen("ramayana/ayodhya/quiz")

    // Donations
    object Donations : Screen("donations")

    // Content routes
    object Poojas : Screen("poojas/{fileName}") {
        fun createRoute(fileName: String) = "poojas/$fileName"
    }
    object Vrathams : Screen("vrathams/{fileName}") {
        fun createRoute(fileName: String) = "vrathams/$fileName"
    }
    object Ashtottaras : Screen("ashtottaras/{fileName}") {
        fun createRoute(fileName: String) = "ashtottaras/$fileName"
    }
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

    // ---------- Generic Wiki Reader (add-only, no behavior change) ----------
    // Use to open long-form "read mode" from any section (Ashtottara, Daily Pooja, etc.)
    object WikiReader : Screen("wikiReader?file={file}&title={title}&lang={lang}&index={index}") {
        fun createRoute(
            file: String,
            title: String,
            lang: String = "te",
            index: Int = 0
        ): String {
            val encFile = Uri.encode(file)
            val encTitle = Uri.encode(title)
            val encLang = Uri.encode(lang)
            return "wikiReader?file=$encFile&title=$encTitle&lang=$encLang&index=$index"
        }
    }
}
