package com.hindu.pooja.ui.ramayana

import android.content.Context
import com.hindu.pooja.ui.ramayana.reader.Lesson
import org.json.JSONObject

data class RamayanaModule(val title: String, val lessons: List<Lesson>)

object RamayanaJsonLoader {

    fun load(context: Context, fileName: String): RamayanaModule {
        val jsonString = context.assets.open(fileName).use { it.readBytes().toString(Charsets.UTF_8) }
        val root = JSONObject(jsonString)

        val title = root.optString("title", "Ramayana")

        val items = root.optJSONArray("items")
        val lessons = ArrayList<Lesson>()

        if (items != null) {
            for (i in 0 until items.length()) {
                val obj = items.getJSONObject(i)
                val id = obj.optString("id", "p$i")
                val t = obj.optString("title", "")
                val content = obj.optString("content", "")
                lessons.add(Lesson(id, t, content))
            }
        }

        return RamayanaModule(title, lessons)
    }
}
