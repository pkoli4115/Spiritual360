package com.hindu.pooja.feature.ramakoti.i18n

/**
 * A single UI option for language selection.
 */
data class LanguageOption(
    val code: String,      // "en", "hi", "te", ...
    val label: String,     // English / हिन्दी / తెలుగు ...
    val sample: String     // a short display sample (shown in picker if needed)
)

/**
 * Centralized language registry for Ramakoti. Extend this list to add new languages.
 * The mantra text returned by [mantraFor] is what goes INSIDE the cells.
 */
object RamakotiLanguages {

    // Default options shown in the picker.
    val defaults: List<LanguageOption> = listOf(
        LanguageOption("en", "English", "Jai Sri Ram"),
        LanguageOption("hi", "हिन्दी",  "जय श्री राम"),
        LanguageOption("te", "తెలుగు",  "జై శ్రీ రామ్")
    )

    /** Normalize to one of our known codes (fallback "en"). */
    fun normalize(code: String?): String =
        defaults.firstOrNull { it.code == code }?.code ?: "en"

    /** Full mantra to render inside each cell for the given code. */
    fun mantraFor(code: String?): String = when (normalize(code)) {
        "hi" -> "जय श्री राम"
        "te" -> "జై శ్రీ రామ్"
        else -> "Jai Sri Ram"
    }
}
