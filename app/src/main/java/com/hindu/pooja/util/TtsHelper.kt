package com.hindu.pooja.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    @Volatile private var ready = false
    private var currentLang: String = "en"

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        ready = (status == TextToSpeech.SUCCESS)
        if (ready) applyLanguage(currentLang)
    }

    /**
     * Apply a given language code ("te", "hi", "en")
     * and update internal TextToSpeech locale.
     */
    fun applyLanguage(lang: String) {
        currentLang = lang
        val locale = when (lang.lowercase()) {
            "te" -> Locale("te", "IN")
            "hi" -> Locale("hi", "IN")
            "en" -> Locale("en", "US")
            else -> Locale.getDefault()
        }
        tts?.language = locale
    }

    /**
     * Speak text using current language (previously applied).
     */
    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "tts-${System.currentTimeMillis()}"
        )
    }

    /**
     * Speak text in a specific language without changing global language state.
     * Example: tts.speak("రామాయణం", lang = "te")
     */
    fun speak(text: String, lang: String) {
        if (!ready || text.isBlank()) return
        val locale = when (lang.lowercase()) {
            "te" -> Locale("te", "IN")
            "hi" -> Locale("hi", "IN")
            "en" -> Locale("en", "US")
            else -> Locale.getDefault()
        }
        tts?.language = locale
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "tts-${System.currentTimeMillis()}"
        )
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        ready = false
    }

    fun isReady(): Boolean = ready
}
