package com.hindu.pooja.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hindu.pooja.feature.quiz.AyodhyaKandaQuizRepo
import com.hindu.pooja.feature.quiz.BalaKandaQuizRepo
import com.hindu.pooja.feature.quiz.BalaKandaQuizScreen
import com.hindu.pooja.ui.ramayana.reader.WikiReaderScreen.WikiReaderScreen
import com.hindu.pooja.ui.ramayana.reader.repo.AyodhyaLessonRepo
import com.hindu.pooja.ui.ramayana.reader.repo.BalaLessonRepo
import com.hindu.pooja.util.TtsHelper

/**
 * Dedicated NavHost for the Ramayana learning module.
 * You can call this from your main nav graph or a tab:
 *     RamayanaNavHost(navController = rememberNavController())
 */
@Composable
fun RamayanaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = "ramayana/home"
) {
    val ctx = LocalContext.current
    val tts = remember { TtsHelper(ctx) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // --- Ramayana landing or placeholder ---
        composable("ramayana/home") {
            Text("📖 Select Kanda to begin your Ramayana journey")
        }

        // ---------- Bala Kanda Wiki ----------
        composable("ramayana/bala/wiki") {
            val module = remember { BalaLessonRepo.loadTeWikiSimple(ctx) }
            val lessons = remember(module) { module.lessons }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = "బాలకాండము కథ",
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate("ramayana/bala/quiz") }
            )
        }

        // ---------- Bala Kanda Quiz ----------
        composable("ramayana/bala/quiz") {
            val repo = remember { BalaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate("ramayana/home") }
            )
        }

        // ---------- Ayodhya Kanda Wiki ----------
        composable("ramayana/ayodhya/wiki") {
            val module = remember { AyodhyaLessonRepo.loadTeWikiSimple(ctx) }
            val lessons = remember(module) { module.lessons }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = "అయోధ్యకాండము కథ",
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate("ramayana/ayodhya/quiz") }
            )
        }

        // ---------- Ayodhya Kanda Quiz ----------
        composable("ramayana/ayodhya/quiz") {
            val repo = remember { AyodhyaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate("ramayana/home") }
            )
        }

        // ---------- Generic Wiki Reader ----------
        composable(
            route = "ramayana/wikiReader?file={file}&title={title}&lang={lang}&index={index}",
            arguments = listOf(
                navArgument("file") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("lang") { type = NavType.StringType; defaultValue = "te" },
                navArgument("index") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val encFile = backStackEntry.arguments?.getString("file").orEmpty()
            val encTitle = backStackEntry.arguments?.getString("title").orEmpty()
            val lang = backStackEntry.arguments?.getString("lang") ?: "te"
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            val file = java.net.URLDecoder.decode(encFile, java.nio.charset.StandardCharsets.UTF_8.name())
            val title = java.net.URLDecoder.decode(encTitle, java.nio.charset.StandardCharsets.UTF_8.name())

            val lessons = remember(file) {
                loadLessonsFromAsset(ctx, file)
            }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = index,
                ttsHelper = tts,
                title = title,
                languageCode = lang,
                onBack = { navController.popBackStack() },
                onLastPage = { }
            )
        }
    }
}

/* --------------------- helpers --------------------- */

private fun loadLessonsFromAsset(
    context: android.content.Context,
    assetPath: String
): List<com.hindu.pooja.ui.ramayana.reader.Lesson> {
    val json = context.assets.open(assetPath).use { it.readBytes().toString(Charsets.UTF_8) }
    val root = org.json.JSONObject(json)
    val titleBase = root.optString("name", root.optString("name_en", "Lesson"))
    val id = root.optString("id", "unknown")
    val content = root.optJSONObject("content") ?: org.json.JSONObject()
    val versesArr = content.optJSONArray("verses") ?: return emptyList()

    val out = ArrayList<com.hindu.pooja.ui.ramayana.reader.Lesson>(versesArr.length())
    for (i in 0 until versesArr.length()) {
        val verse = versesArr.optString(i)
        val pageTitle = "$titleBase — ${i + 1}/${versesArr.length()}"
        out.add(
            com.hindu.pooja.ui.ramayana.reader.Lesson(
                id = "$id-$i",
                title = pageTitle,
                content = verse
            )
        )
    }
    return out
}
