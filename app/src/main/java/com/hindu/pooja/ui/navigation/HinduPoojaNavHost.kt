package com.hindu.pooja.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.ui.ramayana.ramakoti.RamakotiIntroScreen
import com.hindu.pooja.ui.ramayana.ramakoti.RamakotiScreen
import com.hindu.pooja.ui.ramayana.reader.WikiReaderScreen.WikiReaderScreen
import com.hindu.pooja.ui.ramayana.reader.Lesson
import com.hindu.pooja.ui.ramayana.reader.repo.AyodhyaLessonRepo
import com.hindu.pooja.ui.ramayana.reader.repo.BalaLessonRepo
import com.hindu.pooja.feature.quiz.AyodhyaKandaQuizRepo
// Quiz
import com.hindu.pooja.feature.quiz.BalaKandaQuizRepo
import com.hindu.pooja.feature.quiz.BalaKandaQuizScreen
import com.hindu.pooja.ui.kids.findit.FindItGameScreen
import com.hindu.pooja.ui.kids.findit.GameResultScreen
import com.hindu.pooja.ui.login.LoginScreen
import com.hindu.pooja.ui.personal.EditProfileScreen
import com.hindu.pooja.ui.personal.FirstTimeProfileScreen
import com.hindu.pooja.ui.screens.*
import com.hindu.pooja.viewmodel.ProfileViewModel
import com.hindu.pooja.util.TtsHelper
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.json.JSONObject

@Composable
fun HinduPoojaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val start = Screen.Splash.route   // always start at Splash


    NavHost(
        navController = navController,
        startDestination = start,
        modifier = modifier
    ) {
        // --- Core screens ---
        composable(Screen.FirstTimeProfile.route) {
            FirstTimeProfileScreen(navController, onCompletedRoute = Screen.Home.route)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, profileViewModel = profileViewModel)
        }
        composable(Screen.Featured.route) {
            FeaturedScreen(navController = navController)
        }
        composable(Screen.Kids.route) { Text("Kids Zone Coming Soon") }
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
        composable(Screen.Login.route) { LoginScreen(navController = navController) }
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // --- Featured flows ---
        composable(Screen.Ramakoti.route) {
            RamakotiIntroScreen(
                navController = navController,
                onNextRoute = "ramakoti/writer"
            )
        }
        composable("ramakoti/writer") {
            RamakotiScreen(navController = navController)
        }

        // ---------- Bala Kanda WIKI (reader) ----------
        composable(Screen.BalaKandaWikiSimple.route) {
            val ctx = LocalContext.current
            val tts = remember { TtsHelper(ctx) }

            val module = remember { BalaLessonRepo.loadTeWikiSimple(ctx) }
            val lessons = remember(module) { module.lessons }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = "బాలకాండము కథ",
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate(Screen.BalaKandaQuiz.route) }
            )
        }

        // --- Bala Kanda Quiz ---
        composable(Screen.BalaKandaQuiz.route) {
            val repo = remember { BalaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.popBackStack() }
            )
        }
        // --- Ayodhya Kanda Quiz ---
        composable(Screen.AyodhyaKandaQuiz.route) {
            val repo = remember { AyodhyaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.popBackStack() }
            )
        }

        // --- Donations placeholder ---
        composable(Screen.Donations.route) {
            Text("Donations screen (wire your UPI/flow here)")
        }

        // ---------- Content routes (Home) ----------
        composable(
            route = Screen.Poojas.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName").orEmpty()
            val fileName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            PoojasScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.Vrathams.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName").orEmpty()
            val fileName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            VrathamsScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.Ashtottaras.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName").orEmpty()
            val fileName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            AshtottarasScreen(navController = navController, fileName = fileName)
        }

        composable(
            route = Screen.PoojaDetail.route,
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName").orEmpty()
            val fileName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            PoojaDetailScreen(navController = navController, fileName = fileName)
        }

        // ---------- Find-It ----------
        composable(
            route = Screen.FindItGame.route,
            arguments = listOf(navArgument("levelFile") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("levelFile").orEmpty()
            val levelFile = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            FindItGameScreen(levelFile = levelFile, navController = navController)
        }

        // ---------- Game Result ----------
        composable(
            route = Screen.GameResult.route,
            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("levelName").orEmpty()
            val levelName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            GameResultScreen(levelName = levelName, navController = navController)
        }

        // ---------- Generic Wiki Reader (asset-driven) ----------
        composable(
            route = Screen.WikiReader.route,
            arguments = listOf(
                navArgument("file") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("lang") { type = NavType.StringType; defaultValue = "te" },
                navArgument("index") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val ctx = LocalContext.current

            val encFile = backStackEntry.arguments?.getString("file").orEmpty()
            val encTitle = backStackEntry.arguments?.getString("title").orEmpty()
            val lang = backStackEntry.arguments?.getString("lang") ?: "te"
            val initialIndex = backStackEntry.arguments?.getInt("index") ?: 0

            val file = URLDecoder.decode(encFile, StandardCharsets.UTF_8.name())
            val screenTitle = URLDecoder.decode(encTitle, StandardCharsets.UTF_8.name())

            val tts = remember { TtsHelper(ctx) }

            val lessons = remember(file) {
                loadAshtottaraLessonsFromAsset(ctx, file)
            }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = initialIndex,
                ttsHelper = tts,
                title = screenTitle,
                languageCode = lang,
                onBack = { navController.popBackStack() },
                onLastPage = { /* optional */ }
            )
        }

        // ---------- Ayodhya Kanda WIKI (same reader as Bala) ----------
        composable(route = "ramayana/ayodhya/wiki") {
            val ctx = LocalContext.current
            val tts = remember { TtsHelper(ctx) }

            val module = remember { AyodhyaLessonRepo.loadTeWikiSimple(ctx) }
            val lessons = remember(module) { module.lessons }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = "అయోధ్యకాండము కథ",
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate(Screen.AyodhyaKandaQuiz.route) } // ✅ FIX
            )
        }

    }
}

/* -------------------- Private helpers (self-contained) -------------------- */

private fun loadAshtottaraLessonsFromAsset(
    context: android.content.Context,
    assetPath: String
): List<Lesson> {
    val json = context.assets.open(assetPath).use { it.readBytes().toString(Charsets.UTF_8) }
    val root = JSONObject(json)

    val titleBase = root.optString("name", root.optString("name_en", "Ashtottara"))
    val id = root.optString("id", "unknown")

    val content = root.optJSONObject("content") ?: JSONObject()
    val versesArr = content.optJSONArray("verses") ?: return emptyList()

    val out = ArrayList<Lesson>(versesArr.length())
    for (i in 0 until versesArr.length()) {
        val verse = versesArr.optString(i)
        val pageTitle = "$titleBase — ${i + 1}/${versesArr.length()}"
        out.add(
            Lesson(
                id = "ashtottara-$id-$i",
                title = pageTitle,
                content = verse
            )
        )
    }
    return out
}
