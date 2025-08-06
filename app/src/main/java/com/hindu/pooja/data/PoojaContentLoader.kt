package com.hindu.pooja.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.hindu.pooja.model.Katha
import com.hindu.pooja.model.PoojaDetail
import com.hindu.pooja.model.PoojaIndexItem

object PoojaContentLoader {

    fun loadPoojaContent(context: Context, fileName: String): PoojaDetail? {
        val TAG = "PoojaContentLoader"
        return try {
            Log.d(TAG, "Attempting to open pooja asset: poojas/$fileName")
            val jsonString = context.assets.open("poojas/$fileName")
                .bufferedReader().use { it.readText() }
            Log.d(TAG, "Loaded asset: poojas/$fileName, size=${jsonString.length}")

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

            Log.d(TAG, "Parsed PoojaDetail: id=$id, name=$name, language=$language, category=$category, addedDate=$addedDate")
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
            Log.e(TAG, "Error loading/parsing pooja asset: poojas/$fileName", e)
            null
        }
    }

    fun loadPoojaIndex(context: Context, fileName: String): List<PoojaIndexItem> {
        val TAG = "PoojaContentLoader"
        return try {
            Log.d(TAG, "Attempting to open pooja index asset: poojas/$fileName")
            val json = context.assets.open("poojas/$fileName")
                .bufferedReader().use { it.readText() }
            Log.d(TAG, "Loaded pooja index asset: poojas/$fileName, size=${json.length}")
            Gson().fromJson(json, object : TypeToken<List<PoojaIndexItem>>() {}.type)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading/parsing pooja index asset: poojas/$fileName", e)
            emptyList()
        }
    }
}
