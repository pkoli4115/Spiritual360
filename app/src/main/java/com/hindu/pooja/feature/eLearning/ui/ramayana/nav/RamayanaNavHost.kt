package com.hindu.pooja.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hindu.pooja.feature.quiz.AyodhyaKandaQuizRepo
import com.hindu.pooja.feature.quiz.AranyaKandaQuizRepo
import com.hindu.pooja.feature.quiz.BalaKandaQuizRepo
import com.hindu.pooja.feature.quiz.BalaKandaQuizScreen
import com.hindu.pooja.feature.quiz.KishkindhaKandaQuizRepo
import com.hindu.pooja.feature.quiz.SundaraKandaQuizRepo
import com.hindu.pooja.feature.quiz.UttaraKandaQuizRepo
import com.hindu.pooja.feature.quiz.YuddhaKandaQuizRepo
import com.hindu.pooja.ui.ramayana.RamayanaRoutes
import com.hindu.pooja.ui.ramayana.hub.RamayanaHubScreen
import com.hindu.pooja.ui.ramayana.reader.WikiReaderScreen.WikiReaderScreen
import com.hindu.pooja.ui.ramayana.reader.data.KandaJsonLoader
import com.hindu.pooja.ui.ramayana.reader.data.KandaRepository
import com.hindu.pooja.util.TtsHelper
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.json.JSONObject

/**
 * Dedicated NavHost for the Ramayana learning module.
 */
@Composable
fun RamayanaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = RamayanaRoutes.HUB
) {
    val ctx = LocalContext.current
    val tts = remember { TtsHelper(ctx) }
    val kandaRepo = remember(ctx) { KandaRepository(ctx) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(RamayanaRoutes.HUB) {
            RamayanaHubScreen(
                onOpenBala = { navController.navigate(RamayanaRoutes.BALA_WIKI) },
                onOpenAyodhya = { navController.navigate(RamayanaRoutes.AYODHYA_WIKI) },
                onOpenRamakoti = { navController.navigate(RamayanaRoutes.RAMAKOTI_WRITER) },
                onOpenAranya = { navController.navigate(RamayanaRoutes.ARANYA_WIKI) },
                onOpenKishkindha = { navController.navigate(RamayanaRoutes.KISHKINDHA_WIKI) },
                onOpenSundara = { navController.navigate(RamayanaRoutes.SUNDARA_WIKI) },
                onOpenYuddha = { navController.navigate(RamayanaRoutes.YUDDHA_WIKI) },
                onOpenUttara = { navController.navigate(RamayanaRoutes.UTTARA_WIKI) }
            )
        }

        composable(RamayanaRoutes.BALA_WIKI) {
            val lessons = remember { kandaRepo.getReaderLessons(KandaJsonLoader.Kanda.BALA) }
            val title = remember { kandaRepo.getTitle(KandaJsonLoader.Kanda.BALA) }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = title.ifBlank { "బాలకాండము కథ" },
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate(RamayanaRoutes.BALA_QUIZ) }
            )
        }

        composable(RamayanaRoutes.BALA_QUIZ) {
            val repo = remember { BalaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate(RamayanaRoutes.HUB) }
            )
        }

        composable(RamayanaRoutes.AYODHYA_WIKI) {
            val lessons = remember {
                kandaRepo.getReaderLessons(KandaJsonLoader.Kanda.AYODHYA)
            }
            val title = remember { kandaRepo.getTitle(KandaJsonLoader.Kanda.AYODHYA) }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = title.ifBlank { "అయోధ్యకాండము కథ" },
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate(RamayanaRoutes.AYODHYA_QUIZ) }
            )
        }

        composable(RamayanaRoutes.AYODHYA_QUIZ) {
            val repo = remember { AyodhyaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate(RamayanaRoutes.HUB) }
            )
        }

        composable(RamayanaRoutes.ARANYA_WIKI) {
            val lessons = remember {
                kandaRepo.getReaderLessons(KandaJsonLoader.Kanda.ARANYA)
            }
            val title = remember { kandaRepo.getTitle(KandaJsonLoader.Kanda.ARANYA) }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = title.ifBlank { "అరణ్యకాండము కథ" },
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate(RamayanaRoutes.ARANYA_QUIZ) }
            )
        }

        composable(RamayanaRoutes.ARANYA_QUIZ) {
            val repo = remember { AranyaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate(RamayanaRoutes.HUB) }
            )
        }

        composable(RamayanaRoutes.KISHKINDHA_WIKI) {
            val lessons = remember {
                kandaRepo.getReaderLessons(KandaJsonLoader.Kanda.KISHKINDHA)
            }
            val title = remember { kandaRepo.getTitle(KandaJsonLoader.Kanda.KISHKINDHA) }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = title.ifBlank { "కిష్కిందకాండము కథ" },
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate(RamayanaRoutes.KISHKINDHA_QUIZ) }
            )
        }

        composable(RamayanaRoutes.KISHKINDHA_QUIZ) {
            val repo = remember { KishkindhaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate(RamayanaRoutes.HUB) }
            )
        }

        composable(RamayanaRoutes.SUNDARA_WIKI) {
            val lessons = remember {
                kandaRepo.getReaderLessons(KandaJsonLoader.Kanda.SUNDARA)
            }
            val title = remember { kandaRepo.getTitle(KandaJsonLoader.Kanda.SUNDARA) }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = title.ifBlank { "సుందరకాండము కథ" },
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate(RamayanaRoutes.SUNDARA_QUIZ) }
            )
        }

        composable(RamayanaRoutes.SUNDARA_QUIZ) {
            val repo = remember { SundaraKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate(RamayanaRoutes.HUB) }
            )
        }

        composable(RamayanaRoutes.YUDDHA_WIKI) {
            val lessons = remember {
                kandaRepo.getReaderLessons(KandaJsonLoader.Kanda.YUDDHA)
            }
            val title = remember { kandaRepo.getTitle(KandaJsonLoader.Kanda.YUDDHA) }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = title.ifBlank { "యుద్ధకాండము కథ" },
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate(RamayanaRoutes.YUDDHA_QUIZ) }
            )
        }

        composable(RamayanaRoutes.YUDDHA_QUIZ) {
            val repo = remember { YuddhaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate(RamayanaRoutes.HUB) }
            )
        }

        composable(RamayanaRoutes.UTTARA_WIKI) {
            val lessons = remember {
                kandaRepo.getReaderLessons(KandaJsonLoader.Kanda.UTTARA)
            }
            val title = remember { kandaRepo.getTitle(KandaJsonLoader.Kanda.UTTARA) }

            WikiReaderScreen(
                lessons = lessons,
                initialIndex = 0,
                ttsHelper = tts,
                title = title.ifBlank { "ఉత్తరకాండము కథ" },
                languageCode = "te",
                onBack = { navController.popBackStack() },
                onLastPage = { navController.navigate(RamayanaRoutes.UTTARA_QUIZ) }
            )
        }

        composable(RamayanaRoutes.UTTARA_QUIZ) {
            val repo = remember { UttaraKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { navController.popBackStack() },
                onFinish = { navController.navigate(RamayanaRoutes.HUB) }
            )
        }

        composable(
            route = "ramayana/wikiReader?file={file}&title={title}&lang={lang}&index={index}",
            arguments = listOf(
                navArgument("file") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("lang") {
                    type = NavType.StringType
                    defaultValue = "te"
                },
                navArgument("index") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val encFile = backStackEntry.arguments?.getString("file").orEmpty()
            val encTitle = backStackEntry.arguments?.getString("title").orEmpty()
            val lang = backStackEntry.arguments?.getString("lang") ?: "te"
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            val file = URLDecoder.decode(encFile, StandardCharsets.UTF_8.name())
            val title = URLDecoder.decode(encTitle, StandardCharsets.UTF_8.name())

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

private fun loadLessonsFromAsset(
    context: Context,
    assetPath: String
): List<com.hindu.pooja.ui.ramayana.reader.Lesson> {
    val json = context.assets.open(assetPath)
        .use { it.readBytes().toString(Charsets.UTF_8) }

    val root = JSONObject(json)
    val titleBase = root.optString("name", root.optString("name_en", "Lesson"))
    val id = root.optString("id", "unknown")
    val content = root.optJSONObject("content") ?: JSONObject()
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