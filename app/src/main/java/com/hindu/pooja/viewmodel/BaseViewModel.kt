package com.hindu.pooja.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BaseViewModel : ViewModel() {
    private val _language = MutableStateFlow("te") // Telugu default
    val language: StateFlow<String> = _language

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale.coerceIn(1.0f, 2.0f)
    }
}
