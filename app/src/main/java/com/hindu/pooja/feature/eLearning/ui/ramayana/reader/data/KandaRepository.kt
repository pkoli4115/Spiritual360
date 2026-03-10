package com.hindu.pooja.ui.ramayana.reader.data

import android.content.Context
import com.hindu.pooja.ui.ramayana.reader.Lesson as UiLesson

/**
 * Repository wrapper over KandaJsonLoader.
 *
 * - getLessons(...)           → raw JSON Lesson models (KandaModels.Lesson)
 * - getTitle(...)             → top-level kanda title (language aware)
 * - getReaderLessons(...)     → mapped to UI Lesson used by WikiReaderScreen
 *
 * Existing Telugu-only functions still work and internally
 * call the language-aware versions with Language.TE.
 */
class KandaRepository(private val context: Context) {

    /** Raw KandaPayload lessons from JSON (KandaModels.kt) — language-aware. */
    fun getLessons(
        kanda: KandaJsonLoader.Kanda,
        language: KandaJsonLoader.Language
    ): List<Lesson> {
        val payload = KandaJsonLoader.load(context, kanda, language)
        return payload.lessons.sortedBy { it.order }
    }

    /** Telugu-only helper (backwards compatible). */
    fun getLessons(kanda: KandaJsonLoader.Kanda): List<Lesson> =
        getLessons(kanda, KandaJsonLoader.Language.TE)

    /** Title for the given kanda in the requested language, with fallback. */
    fun getTitle(
        kanda: KandaJsonLoader.Kanda,
        language: KandaJsonLoader.Language
    ): String {
        val p = KandaJsonLoader.load(context, kanda, language)
        // top-level title is a map { "te": "...", "en": "...", ... }
        val langKey = when (language) {
            KandaJsonLoader.Language.TE -> "te"
            KandaJsonLoader.Language.EN -> "en"
            KandaJsonLoader.Language.HI -> "hi"
        }
        return p.title[langKey] ?: p.title.values.firstOrNull().orEmpty()
    }

    /** Telugu title helper kept for compatibility. */
    fun getTitle(kanda: KandaJsonLoader.Kanda): String =
        getTitle(kanda, KandaJsonLoader.Language.TE)

    /**
     * Lessons mapped to the UI model expected by WikiReaderScreen.
     * Language-aware version.
     */
    fun getReaderLessons(
        kanda: KandaJsonLoader.Kanda,
        language: KandaJsonLoader.Language
    ): List<UiLesson> {
        return getLessons(kanda, language).map { l ->
            UiLesson(
                id = l.id,
                title = l.title,
                content = l.content
            )
        }
    }

    /** Telugu-only helper kept for existing screens. */
    fun getReaderLessons(kanda: KandaJsonLoader.Kanda): List<UiLesson> =
        getReaderLessons(kanda, KandaJsonLoader.Language.TE)
}
