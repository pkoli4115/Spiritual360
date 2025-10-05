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

    fun applyLanguage(lang: String) {
        currentLang = lang
        val locale = when (lang) {
            "te" -> Locale("te", "IN")
            "hi" -> Locale("hi", "IN")
            else -> Locale("en", "US")
        }
        tts?.language = locale
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "balakanda-${System.currentTimeMillis()}")
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
