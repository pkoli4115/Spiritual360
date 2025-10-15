package com.hindu.pooja.ui.navigation

import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.ui.ramayana.ramakoti.RamakotiIntroScreen
import com.hindu.pooja.feature.ramakoti.ui.RamakotiWriterScreen
import com.hindu.pooja.ui.ramayana.reader.WikiReaderScreen.WikiReaderScreen
import com.hindu.pooja.ui.ramayana.reader.Lesson
import com.hindu.pooja.ui.ramayana.reader.repo.AyodhyaLessonRepo
import com.hindu.pooja.ui.ramayana.reader.repo.BalaLessonRepo
import com.hindu.pooja.feature.quiz.AyodhyaKandaQuizRepo
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

// Phase 2 additions
import com.hindu.pooja.feature.profile.ui.JourneyScreen
import com.hindu.pooja.feature.profile.ui.CertificatesScreen
import com.hindu.pooja.feature.profile.ui.ReflectionsScreen
import com.hindu.pooja.feature.ramakoti.ui.LanguageSelectionScreen
import com.hindu.pooja.feature.ramakoti.ui.CertificateScreen

// Language guard (one-shot read to avoid race)
import kotlinx.coroutines.flow.first
import com.hindu.pooja.feature.ramakoti.data.LanguagePreferenceManager

@Composable
fun HinduPoojaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val start = Screen.Splash.route

    NavHost(
        navController = navController,
        startDestination = start,
        modifier = modifier
    ) {
        /* ---------------- PHASE 1 ROUTES (unchanged) ---------------- */
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
        composable(Screen.EditProfile.route) { EditProfileScreen(navController = navController) }
        composable(Screen.Login.route) { LoginScreen(navController = navController) }
        composable(Screen.Splash.route) { SplashScreen(navController = navController) }

        /* ---------------- RAMAKOTI & RAMAYANA ---------------- */

        // Back-compat: if any old caller still uses "featured/ramakoti"
        composable("featured/ramakoti") {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.Ramakoti.route) {
                    popUpTo("featured/ramakoti") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        // ✅ Canonical Ramakoti entry with per-user language guard
        composable(Screen.Ramakoti.route) {
            val ctx = LocalContext.current
            val langMgr = remember { LanguagePreferenceManager.getInstance(ctx) }
            var lang: String? by remember { mutableStateOf(null) }

            LaunchedEffect(Unit) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                lang = langMgr.languageFlowFor(uid).first()  // "" if unset for this user
            }

            when (lang) {
                null -> { /* optional placeholder while reading */ }
                "" -> {
                    // No language for THIS user → go to picker
                    LaunchedEffect("to-picker") {
                        navController.navigate("ramakoti/language") {
                            launchSingleTop = true
                        }
                    }
                }
                else -> {
                    // Language exists for this user → proceed to intro
                    RamakotiIntroScreen(
                        navController = navController,
                        onNextRoute = "ramakoti/writer"
                    )
                }
            }
        }

        // Writer screen
        composable("ramakoti/writer") {
            val vm: com.hindu.pooja.feature.ramakoti.RamakotiViewModel = hiltViewModel()
            RamakotiWriterScreen(
                vm = vm,
                onPickNextTarget = {
                    navController.navigate("ramakoti/language") { launchSingleTop = true }
                }
            )
        }

        // Certificate preview
        composable("ramakoti/certificate") {
            CertificateScreen(milestoneCountText = "1 Crore Sri Rama Namas Completed")
        }

        /* ---------------- WIKI + QUIZZES (unchanged) ---------------- */
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
        composable(Screen.BalaKandaQuiz.route) {
            val repo = remember { BalaKandaQuizRepo.default() }
            BalaKandaQuizScreen(repo, { navController.popBackStack() }, { navController.popBackStack() })
        }
        composable(Screen.AyodhyaKandaQuiz.route) {
            val repo = remember { AyodhyaKandaQuizRepo.default() }
            BalaKandaQuizScreen(repo, { navController.popBackStack() }, { navController.popBackStack() })
        }

        composable(Screen.Donations.route) { Text("Donations screen (wire your UPI flow here)") }

        /* ---------------- POOJAS / VRATHAMS / DETAILS / GAMES (unchanged) ---------------- */
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

        composable(
            route = Screen.FindItGame.route,
            arguments = listOf(navArgument("levelFile") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("levelFile").orEmpty()
            val levelFile = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            FindItGameScreen(levelFile = levelFile, navController = navController)
        }

        composable(
            route = Screen.GameResult.route,
            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("levelName").orEmpty()
            val levelName = URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
            GameResultScreen(levelName = levelName, navController = navController)
        }

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
            val lessons = remember(file) { loadAshtottaraLessonsFromAsset(ctx, file) }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = initialIndex,
                ttsHelper = tts,
                title = screenTitle,
                languageCode = lang,
                onBack = { navController.popBackStack() },
                onLastPage = { }
            )
        }

        composable("ramayana/ayodhya/wiki") {
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
                onLastPage = { navController.navigate(Screen.AyodhyaKandaQuiz.route) }
            )
        }

        /* ---------------- PHASE 2 ADDITIONS ---------------- */
        composable("profile/journey") {
            JourneyScreen(onOpenCertificates = {
                navController.navigate("profile/certificates")
            })
        }
        composable("profile/certificates") { CertificatesScreen() }
        composable("profile/reflections") { ReflectionsScreen() }

        // Language selection (picker)
        composable("ramakoti/language") { LanguageSelectionScreen(navController) }

        // Certificate preview
        composable("ramakoti/certificate") {
            CertificateScreen(milestoneCountText = "1 Crore Sri Rama Namas Completed")
        }
    }
}

/* ---------------- Helper kept intact ---------------- */
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
        out.add(Lesson("ashtottara-$id-$i", pageTitle, verse))
    }
    return out
}
