package com.hindu.pooja.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hindu.pooja.model.PoojaIndexItem
import java.io.InputStreamReader

object PoojaLoader {

    fun loadPoojaListFromAsset(context: Context, fileName: String): List<PoojaIndexItem> {
        return try {
            val inputStream = context.assets.open("poojas/$fileName")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<PoojaIndexItem>>() {}.type
            Gson().fromJson(reader, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    fun loadPoojaIndex(context: Context, fileName: String): List<PoojaIndexItem> {
        return try {
            val inputStream = context.assets.open("poojas/$fileName")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<PoojaIndexItem>>() {}.type
            Gson().fromJson(reader, type)
        } catch (e: Exception) {
            emptyList()
        }
    }


    private fun loadPoojaList(context: Context, fileName: String): List<PoojaIndexItem> {
        return try {
            val inputStream = context.assets.open("poojas/$fileName")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<PoojaIndexItem>>() {}.type
            Gson().fromJson(reader, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
