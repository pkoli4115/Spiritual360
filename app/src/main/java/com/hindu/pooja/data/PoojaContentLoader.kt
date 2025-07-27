package com.hindu.pooja.data

import android.content.Context
import com.google.gson.Gson
import com.hindu.pooja.model.PoojaDetail
import java.io.InputStreamReader

object PoojaContentLoader {
    fun loadPoojaContent(context: Context, fileName: String): PoojaDetail? {
        return try {
            val inputStream = context.assets.open("poojas/$fileName")
            val reader = InputStreamReader(inputStream)
            Gson().fromJson(reader, PoojaDetail::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
