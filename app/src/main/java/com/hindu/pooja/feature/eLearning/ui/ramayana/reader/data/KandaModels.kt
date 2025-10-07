package com.hindu.pooja.ui.ramayana.reader.data

data class KandaPayload(
    val id: String,
    val title: Map<String, String> = emptyMap(),
    val version: String,
    val created_at: String? = null,
    val languages: List<String> = listOf("te"),
    val tts: TtsConfig = TtsConfig(),
    val source: String? = null,
    val url: String? = null,
    val license: String? = null,
    val lessons: List<Lesson>
)

data class TtsConfig(
    val default_language: String = "te",
    val available_languages: List<String> = listOf("te")
)

data class Lesson(
    val id: String,
    val order: Int,
    val title: String,
    val content: String
)
