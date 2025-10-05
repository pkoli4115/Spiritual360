package com.hindu.pooja.feature.elearning

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

// ----- Data Models (match your JSON exactly) -----
data class Module(
    val id: String,
    val title: Map<String, String>,
    val version: String,
    val languages: List<String>,
    val tts: Map<String, Any>,
    val lessons: List<Lesson>
)

data class Lesson(
    val id: String,
    val order: Int,
    val title: Map<String, String>,
    @SerializedName("flipCards") val flipCards: List<FlipCard>
)

data class FlipCard(
    val lang: String,
    val front: FrontBack,
    val back: String
)

data class FrontBack(
    val title: String,
    val hint: String
)

// ----- Repository / Loader -----
object FlipCardRepository {
    private const val ASSET_PATH = "eLearning/ramayana_bala_kanda_flipcards.json"
    @Volatile private var cached: Module? = null

    fun loadModule(context: Context): Module {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val json = context.assets.open(ASSET_PATH)
                .bufferedReader(Charsets.UTF_8).use { it.readText() }

            val moduleType = object : TypeToken<Module>() {}.type
            val module = Gson().fromJson<Module>(json, moduleType)
            cached = module
            return module
        }
    }
}

// ----- Helpers -----
fun supportedLanguages(module: Module): List<String> {
    // Ensure only languages present in flipCards are exposed
    val langs = module.lessons
        .flatMap { lesson -> lesson.flipCards.map { it.lang } }
        .toSet()
    val ordered = listOf("en", "te", "hi").filter { langs.contains(it) }
    return if (ordered.isNotEmpty()) ordered else langs.toList()
}

fun languageDisplayName(code: String): String = when (code) {
    "en" -> "English"
    "te" -> "తెలుగు"
    "hi" -> "हिन्दी"
    else -> code
}

// Returns cards for a given lesson & language
fun cardsFor(lesson: Lesson, lang: String): List<FlipCard> =
    lesson.flipCards.filter { it.lang == lang }.ifEmpty { lesson.flipCards }

// Safe title: prefer requested language, fallbacks to EN or any
fun titleFor(map: Map<String, String>, lang: String): String =
    map[lang] ?: map["en"] ?: map.values.firstOrNull().orEmpty()
