package com.hindu.pooja.feature.elearning

import android.content.Context
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Type

/* =========================
   Data model for Flip Cards
   ========================= */
data class FlipCardsModule(
    val id: String = "ramayana_bala_kanda",
    val title: Map<String, String> = emptyMap(),
    val version: String? = null,
    val languages: List<String> = emptyList(),
    val lessons: List<Lesson> = emptyList()
)

data class Lesson(
    val id: String,
    val order: Int,
    val title: Map<String, String>,
    val flipCards: List<FlipCard>
)

data class FlipCard(
    val lang: String,
    val front: CardFace,
    val back: String
)

data class CardFace(
    val title: String,
    val hint: String = ""
)

/* =============================================================================
   Fallback “simple wiki” schema (title + content) — used if flipcard parse fails
   ============================================================================= */
private data class SimpleWikiModule(
    val id: String? = null,
    val title: Any? = null,                 // can be string or map; unused in convert
    val languages: List<String>? = null,
    val lessons: List<SimpleWikiLesson> = emptyList()
)

private data class SimpleWikiLesson(
    val id: String? = null,
    val order: Int? = null,
    val title: String = "",
    val content: String = ""
)

/* ===========================================================
   GSON helpers: accept "title" as STRING or as MAP<String,Str>
   =========================================================== */
private class MapStringAdapter : JsonDeserializer<Map<String, String>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Map<String, String> {
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                buildMap {
                    for ((k, v) in obj.entrySet()) {
                        put(k, if (v.isJsonPrimitive) v.asString else v.toString())
                    }
                }
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                // Default to Telugu if only a raw string is present
                mapOf("te" to json.asString)
            }
            else -> emptyMap()
        }
    }
}

private val mapStringType = object : TypeToken<Map<String, String>>() {}.type

private fun gsonForFlip(): Gson = GsonBuilder()
    .registerTypeAdapter(mapStringType, MapStringAdapter())
    .setLenient()
    .create()

private fun readAsset(context: Context, path: String): String {
    context.assets.open(path).use { input ->
        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { br ->
            return br.readText()
        }
    }
}

/* ========================================
   Repository with tolerant/fallback loading
   ======================================== */
object FlipCardRepository {

    /** Path your FlipCard screen has always loaded */
    private const val FLIPCARD_PATH = "eLearning/ramayana_bala_kanda_flipcards.json"

    fun loadModule(context: Context): FlipCardsModule {
        val raw = readAsset(context, FLIPCARD_PATH)

        // 1) Try strict flipcard load (but tolerant to title string/map)
        runCatching {
            val mod = gsonForFlip().fromJson(raw, FlipCardsModule::class.java)
            // Basic sanity: should have lessons with flipCards
            if (mod.lessons.isNotEmpty() && mod.lessons.first().flipCards.isNotEmpty()) {
                return mod
            }
        }.onFailure { /* swallow and try fallback */ }

        // 2) Fallback: maybe the file is a “simple wiki” module (title+content)
        runCatching {
            val simple = GsonBuilder().setLenient().create()
                .fromJson(raw, SimpleWikiModule::class.java)

            // If there is obvious simple content, convert it.
            if (simple.lessons.isNotEmpty() && simple.lessons.first().content.isNotBlank()) {
                return convertSimpleToFlip(simple)
            }
        }.onFailure { /* keep going to error */ }

        // 3) Give a clear error so it’s easy to diagnose
        throw IllegalStateException(
            "The file at $FLIPCARD_PATH is neither a valid Flip-Card module nor a Simple-Wiki module.\n" +
                    "If you intend to use the reader screen, keep your wiki JSON as a separate file (e.g. eLearning/balakanda_te_wiki_simple.json)\n" +
                    "and open it via the Wiki reader route instead of the FlipCard route."
        )
    }

    private fun convertSimpleToFlip(simple: SimpleWikiModule): FlipCardsModule {
        val lessons = simple.lessons
            .sortedBy { it.order ?: Int.MAX_VALUE }
            .mapIndexed { idx, s ->
                val lessonId = s.id ?: String.format("%02d", (s.order ?: (idx + 1)))
                val titleMap = mapOf("te" to s.title)
                val card = FlipCard(
                    lang = "te",
                    front = CardFace(title = s.title, hint = ""),
                    back = s.content
                )
                Lesson(
                    id = lessonId,
                    order = (s.order ?: (idx + 1)),
                    title = titleMap,
                    flipCards = listOf(card)
                )
            }

        val langs = listOf("te") // simple wiki we’re using is Telugu-only
        return FlipCardsModule(
            id = simple.id ?: "balakanda_te_wiki_simple_as_flip",
            title = mapOf("te" to "బాలకాండము — సంక్షిప్త కథ (ఫ్లిప్ రూపం)"),
            version = "auto-converted",
            languages = langs,
            lessons = lessons
        )
    }
}

/* =======================
   Small UI helper methods
   ======================= */
fun supportedLanguages(module: FlipCardsModule): List<String> {
    val langs = module.lessons.flatMap { it.flipCards.map { c -> c.lang } }.distinct()
    return if (langs.isEmpty()) listOf("te") else langs
}

fun titleFor(map: Map<String, String>, lang: String): String {
    return map[lang] ?: map["en"] ?: map.values.firstOrNull().orEmpty()
}

fun cardsFor(lesson: Lesson, lang: String): List<FlipCard> {
    val filtered = lesson.flipCards.filter { it.lang.equals(lang, ignoreCase = true) }
    return if (filtered.isNotEmpty()) filtered else lesson.flipCards
}
