package com.hindu.pooja.ui.ramayana.reader.data

/**
 * Raw JSON model for a Ramayana Kanda module.
 *
 * Matches files like:
 *  - ramayana/aranya_kanda_en_wiki_simple.json
 *  - ramayana/balakanda_te_wiki_simple.json
 * etc.
 */
data class KandaPayload(
    val id: String = "",
    // e.g. { "te": "బాలకాండము కథ", "en": "Ramayana — Balakanda ..." }
    val title: Map<String, String> = emptyMap(),
    val version: String? = null,
    val created_at: String? = null,
    val languages: List<String>? = null,
    val tts: TtsConfig? = null,
    val lessons: List<Lesson> = emptyList()
)

/**
 * Optional TTS config block from JSON.
 */
data class TtsConfig(
    val default_language: String? = null,
    val available_languages: List<String>? = null
)

/**
 * Single lesson/page in the Kanda JSON.
 *
 * Fields match entries under the "lessons" array:
 * {
 *   "id": "01",
 *   "order": 1,
 *   "title": "Lesson 01 — ...",
 *   "content": "Long text..."
 * }
 */
data class Lesson(
    val id: String = "",
    val order: Int = 0,
    val title: String = "",
    val content: String = ""
)
