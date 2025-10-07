package com.hindu.pooja.ui.ramayana.reader.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.InputStreamReader

object KandaJsonLoader {
    private val gson: Gson = GsonBuilder().disableHtmlEscaping().create()

    enum class Kanda(val assetFile: String) {
        BALA("ramayana/balakanda_te_wiki_simple.json"),
        AYODHYA("ramayana/ayodhyakanda_te_wiki_simple.json")
    }

    fun load(context: Context, kanda: Kanda): KandaPayload {
        context.assets.open(kanda.assetFile).use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { br ->
                return gson.fromJson(br, KandaPayload::class.java)
            }
        }
    }
}
