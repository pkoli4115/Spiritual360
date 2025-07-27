package com.hindu.pooja.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.hindu.pooja.model.Katha
import com.hindu.pooja.model.PoojaDetail

object PoojaContentLoader {

    fun loadPoojaContent(context: Context, fileName: String): PoojaDetail? {
        return try {
            println("🔍 Attempting to load: assets/poojas/$fileName")

            val jsonString = context.assets.open("poojas/$fileName")
                .bufferedReader().use { it.readText() }

            val gson = Gson()
            val json = gson.fromJson(jsonString, JsonObject::class.java)

            val id = json["id"]?.asString ?: ""
            val name = json["name"]?.asString ?: json["title"]?.asString ?: "Untitled"
            val language = json["language"]?.asString ?: "unknown"
            val category = json["category"]?.asString
            val addedDate = json["addedDate"]?.asString

            val contentObj = json["content"]

            val slokas: List<String>? = if (contentObj?.asJsonObject?.has("slokas") == true) {
                gson.fromJson(contentObj.asJsonObject["slokas"], object : TypeToken<List<String>>() {}.type)
            } else null

            val verses: List<String>? = if (contentObj?.asJsonObject?.has("verses") == true) {
                gson.fromJson(contentObj.asJsonObject["verses"], object : TypeToken<List<String>>() {}.type)
            } else null

            val kathalu: List<Katha>? = if (contentObj?.asJsonObject?.has("kathalu") == true) {
                gson.fromJson(contentObj.asJsonObject["kathalu"], object : TypeToken<List<Katha>>() {}.type)
            } else null

            val mapContent: Map<String, String>? = if (slokas == null && verses == null && kathalu == null) {
                gson.fromJson<Map<String, String>>(
                    contentObj, object : TypeToken<Map<String, String>>() {}.type
                )
            } else null

            println("✅ Loaded: $fileName (slokas=${slokas?.size}, verses=${verses?.size}, kathalu=${kathalu?.size}, sections=${mapContent?.size})")

            PoojaDetail(
                id = id,
                name = name,
                language = language,
                category = category,
                addedDate = addedDate,
                content = mapContent,
                slokas = slokas,
                verses = verses,
                kathalu = kathalu
            )
        } catch (e: Exception) {
            println("❌ Error loading pooja: $fileName")
            e.printStackTrace()
            null
        }
    }
}
