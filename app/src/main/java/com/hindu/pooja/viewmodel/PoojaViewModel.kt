package com.hindu.pooja.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hindu.pooja.data.Pooja
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PoojaViewModel : ViewModel() {

    private val _poojas = MutableStateFlow<Map<String, Pooja>>(emptyMap())
    val poojas: StateFlow<Map<String, Pooja>> = _poojas

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
    }

    fun loadPooja(context: Context, fileName: String) {
        viewModelScope.launch {
            try {
                val jsonStr = context.assets.open("poojas/$fileName")
                    .bufferedReader().use { it.readText() }

                val pooja = Json.decodeFromString<Pooja>(jsonStr)

                _poojas.value = _poojas.value.toMutableMap().apply {
                    put(fileName, pooja)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
