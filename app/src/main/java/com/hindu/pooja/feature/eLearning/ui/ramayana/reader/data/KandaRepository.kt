package com.hindu.pooja.ui.ramayana.reader.data

import android.content.Context

class KandaRepository(private val context: Context) {

    fun getLessons(kanda: KandaJsonLoader.Kanda): List<Lesson> {
        val payload = KandaJsonLoader.load(context, kanda)
        return payload.lessons.sortedBy { it.order }
    }

    fun getTitle(kanda: KandaJsonLoader.Kanda): String {
        val p = KandaJsonLoader.load(context, kanda)
        // top-level title is a map { "te": "..." }
        return p.title["te"] ?: p.title.values.firstOrNull().orEmpty()
    }
}
