package com.hindu.pooja.ui.kids.flashcards

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object FlashCardRepository {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun assetFileNameForCategory(categoryId: String): String =
        when (categoryId) {
            "know_gods" -> "flashcards_know_gods.json"
            "sloka_meanings" -> "flashcards_slokas.json"
            "ramayana_stories" -> "flashcards_ramayana.json"
            else -> "flashcards_know_gods.json"
        }

    fun loadSetFromAssets(context: Context, categoryId: String): FlashCardSet {
        val assetFile = assetFileNameForCategory(categoryId)
        val assetPath = "flashcards/$assetFile"
        val text = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }
}
