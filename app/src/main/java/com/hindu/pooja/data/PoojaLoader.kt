package com.hindu.pooja.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hindu.pooja.model.PoojaIndexItem
import java.io.InputStreamReader

object PoojaLoader {
    fun loadPoojaIndex(context: Context, fileName: String): List<PoojaIndexItem> {
        val TAG = "PoojaLoader"
        return try {
            Log.d(TAG, "Attempting to open pooja index asset: poojas/$fileName")
            val inputStream = context.assets.open("poojas/$fileName")
            val reader = InputStreamReader(inputStream)
            Log.d(TAG, "Loaded pooja index asset: poojas/$fileName")
            val type = object : TypeToken<List<PoojaIndexItem>>() {}.type
            val result = Gson().fromJson<List<PoojaIndexItem>>(reader, type)
            Log.d(TAG, "Parsed ${result.size} items from pooja index asset: poojas/$fileName")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error loading/parsing pooja index asset: poojas/$fileName", e)
            emptyList()
        }
    }
}
