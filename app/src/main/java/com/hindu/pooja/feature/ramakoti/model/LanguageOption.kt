package com.hindu.pooja.feature.ramakoti.model

/**
 * Represents a UI language choice.
 *
 * @param code ISO-ish app code: "en", "te", "hi"
 * @param displayName Shown on the selection UI
 * @param sample Short mantra sample in that script (used to preview)
 */
data class LanguageOption(
    val code: String,
    val displayName: String,
    val sample: String
)

/** Default languages for Ramakoti Phase 1. */
object RamakotiLanguages {
    val defaults: List<LanguageOption> = listOf(
        LanguageOption(code = "en", displayName = "English", sample = "Rama"),
        LanguageOption(code = "te", displayName = "తెలుగు", sample = "రామ"),
        LanguageOption(code = "hi", displayName = "हिंदी", sample = "राम"),
    )

    fun mantraFor(code: String): String = when (code.lowercase()) {
        "te" -> "రామ"
        "hi" -> "राम"
        else -> "Rama"
    }
}
