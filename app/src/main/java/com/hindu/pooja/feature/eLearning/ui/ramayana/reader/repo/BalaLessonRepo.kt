package com.hindu.pooja.ui.ramayana.reader.repo

import android.content.Context
import org.json.JSONObject

object BalaLessonRepo {
    fun loadTeWikiSimple(context: Context): SimpleLessonModule {
        val jsonText = context.assets.open("ramayana/balakanda_te_wiki_simple.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(jsonText)
        val lessonsArr = root.optJSONArray("lessons") ?: return SimpleLessonModule(emptyList())
        val lessons = ArrayList<com.hindu.pooja.ui.ramayana.reader.Lesson>()
        for (i in 0 until lessonsArr.length()) {
            val item = lessonsArr.getJSONObject(i)
            lessons += com.hindu.pooja.ui.ramayana.reader.Lesson(
                id = "bala-$i",
                title = item.optString("title"),
                content = item.optString("content")
            )
        }
        return SimpleLessonModule(lessons)
    }
}

data class SimpleLessonModule(
    val lessons: List<com.hindu.pooja.ui.ramayana.reader.Lesson>
)
