package com.hindu.pooja.feature.elearning

import android.content.Context
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader

data class SimpleLesson(
    val id: String,
    val order: Int,
    val title: String,
    val content: String
)

data class SimpleLessonModule(
    val id: String,
    val title: String,
    val language: String,
    val lessons: List<SimpleLesson>
)

object SimpleLessonRepo {

    /** Loads the simple Telugu wiki lessons from assets. */
    fun loadTeWikiSimple(context: Context): SimpleLessonModule {
        val json = readAsset(context, "eLearning/balakanda_te_wiki_simple.json")
        val root = JsonParser.parseString(json).asJsonObject

        fun asStringFlexible(el: JsonElement?): String {
            if (el == null) return ""
            return when {
                el.isJsonPrimitive -> el.asString
                el.isJsonObject -> {
                    // Prefer Telugu if present
                    val obj = el.asJsonObject
                    when {
                        obj.has("te") -> obj["te"].asString
                        obj.has("en") -> obj["en"].asString
                        else -> obj.entrySet().firstOrNull()?.value?.asString ?: ""
                    }
                }
                else -> ""
            }
        }

        val moduleId = root.get("id")?.asString ?: "balakanda_te_wiki_simple"
        val moduleTitle = asStringFlexible(root.get("title"))
        val language = root.getAsJsonArray("languages")?.firstOrNull()?.asString ?: "te"

        val lessonsJson = root.getAsJsonArray("lessons")
        val lessons = lessonsJson.mapIndexed { _, el ->
            val o = el.asJsonObject
            SimpleLesson(
                id = o.get("id")?.asString ?: "",
                order = o.get("order")?.asInt ?: 0,
                title = asStringFlexible(o.get("title")),
                content = asStringFlexible(o.get("content"))
            )
        }.sortedBy { it.order }

        return SimpleLessonModule(moduleId, moduleTitle, language, lessons)
    }

    private fun readAsset(context: Context, path: String): String {
        context.assets.open(path).use { ins ->
            BufferedReader(InputStreamReader(ins, Charsets.UTF_8)).use { br ->
                return br.readText()
            }
        }
    }
}
