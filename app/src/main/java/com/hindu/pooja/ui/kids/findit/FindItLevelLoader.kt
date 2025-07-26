package com.hindu.pooja.ui.kids.findit

import android.content.Context
import com.hindu.pooja.ui.kids.findit.model.HiddenObjectsLevel
import kotlinx.serialization.json.Json

object FindItLevelLoader {

    // ✅ This setting avoids crashing when extra keys like "sceneImage" are in JSON
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun loadLevel(context: Context, fileName: String): HiddenObjectsLevel {
        val jsonString = context.assets.open("hidden_objects/$fileName")
            .bufferedReader().use { it.readText() }

        return json.decodeFromString(HiddenObjectsLevel.serializer(), jsonString)
    }
}
