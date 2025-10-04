package com.hindu.pooja.util.ramakoti

import java.text.Normalizer

object RamakotiValidator {
    private val englishAccepted = setOf(
        "jai sri ram", "jai shri ram", "jai shree ram"
    )
    // Normalize with NFKC, collapse spaces, lowercase
    fun normalize(s: String): String {
        val nfkc = Normalizer.normalize(s, Normalizer.Form.NFKC)
        val trimmed = nfkc.trim { it <= ' ' }
        val collapsed = trimmed.split("\\s+".toRegex()).joinToString(" ")
        return collapsed.lowercase()
    }

    // Hindi: जय श्री राम (allow spacing variants)
    private fun isValidHindi(n: String): Boolean {
        // common variants with/without matra spacing
        val target = listOf(
            "जय श्री राम", "जय  श्री  राम", "जय  श्री राम", "जय श्री  राम"
        ).map(::normalize)
        return target.contains(n)
    }

    // Telugu: జై శ్రీ రామ్ / జై శ్రీ రాం
    private fun isValidTelugu(n: String): Boolean {
        val target = listOf(
            "జై శ్రీ రామ్",
            "జై శ్రీ రాం",
            "జై  శ్రీ  రామ్",
            "జై  శ్రీ  రాం"
        ).map(::normalize)
        return target.contains(n)
    }

    fun isValidPhrase(input: String, language: String): Boolean {
        val n = normalize(input)
        return when (language.lowercase()) {
            "hi" -> isValidHindi(n)
            "te" -> isValidTelugu(n)
            else -> englishAccepted.contains(n)
        }
    }
}
