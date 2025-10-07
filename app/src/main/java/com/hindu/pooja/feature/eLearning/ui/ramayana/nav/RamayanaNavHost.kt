package com.hindu.pooja.ui.ramayana.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hindu.pooja.feature.quiz.AyodhyaKandaQuizRepo
import com.hindu.pooja.feature.quiz.BalaKandaQuizRepo
import com.hindu.pooja.feature.quiz.BalaKandaQuizScreen
import com.hindu.pooja.ui.ramayana.ramakoti.RamakotiIntroScreen
import com.hindu.pooja.ui.ramayana.ramakoti.RamakotiScreen
import com.hindu.pooja.ui.ramayana.reader.WikiReaderScreen.WikiReaderScreen
import com.hindu.pooja.ui.ramayana.reader.repo.AyodhyaLessonRepo
import com.hindu.pooja.ui.ramayana.reader.repo.BalaLessonRepo
import com.hindu.pooja.util.TtsHelper

@Composable
fun RamayanaNavHost(
    navController: NavController,
    startDestination: String = RamayanaRoutes.HUB
) {
    val graphController = navController as? NavHostController ?: navController

    NavHost(
        navController = graphController as NavHostController,
        startDestination = startDestination
    ) {
        // ---------- Hub ----------
        composable(RamayanaRoutes.HUB) {
            com.hindu.pooja.ui.ramayana.hub.RamayanaHubScreen(
                onOpenBala = { graphController.navigate(RamayanaRoutes.BALA_WIKI) },
                onOpenAyodhya = { graphController.navigate(RamayanaRoutes.AYODHYA_WIKI) },
                onOpenRamakoti = { graphController.navigate(RamayanaRoutes.RAMAKOTI_INTRO) }
            )
        }

        // ---------- Bala Kanda Reader ----------
        composable(RamayanaRoutes.BALA_WIKI) {
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
                onBack = { graphController.popBackStack() },
                onLastPage = { graphController.navigate(RamayanaRoutes.BALA_QUIZ) }
            )
        }

        // ---------- Ayodhya Kanda Reader ----------
        composable(RamayanaRoutes.AYODHYA_WIKI) {
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
                onBack = { graphController.popBackStack() },
                onLastPage = { graphController.navigate(RamayanaRoutes.AYODHYA_QUIZ) }
            )
        }

        // ---------- Ramakoti Flow ----------
        composable(RamayanaRoutes.RAMAKOTI_INTRO) {
            RamakotiIntroScreen(
                navController = graphController,
                onNextRoute = RamayanaRoutes.RAMAKOTI_WRITER
            )
        }
        composable(RamayanaRoutes.RAMAKOTI_WRITER) {
            RamakotiScreen(navController = graphController)
        }

        // ---------- Bala Kanda Quiz ----------
        composable(RamayanaRoutes.BALA_QUIZ) {
            val repo = remember { BalaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { graphController.popBackStack() },
                onFinish = { graphController.popBackStack() }
            )
        }

        // ---------- Ayodhya Kanda Quiz ----------
        composable(RamayanaRoutes.AYODHYA_QUIZ) {
            val repo = remember { AyodhyaKandaQuizRepo.default() }
            BalaKandaQuizScreen(
                repo = repo,
                onBack = { graphController.popBackStack() },
                onFinish = { graphController.popBackStack() }
            )
        }
    }
}
