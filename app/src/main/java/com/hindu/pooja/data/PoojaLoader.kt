package com.hindu.pooja.app.data

import android.content.Context
import com.hindu.pooja.data.Pooja
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

object PoojaLoader {
    fun loadPoojaFromAssets(context: Context, fileName: String): Pooja {
        val jsonString = context.assets.open("poojas/$fileName").bufferedReader().use { it.readText() }
        return Json.decodeFromString(jsonString)
    }
}
