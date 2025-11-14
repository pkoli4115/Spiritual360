package com.hindu.pooja.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hindu.pooja.model.PoojaIndexItem
import java.io.InputStreamReader
import java.util.Locale

object PoojaLoader {

    private const val TAG = "PoojaLoader"

    private data class PoojaIndexItemDto(
        val id: String? = null,
        val name: String? = null,
        val image: String? = null,
        val file: String? = null,
        val category: String? = null,
        val isPremium: Boolean? = null,
        val scrollable: Boolean? = null
    )

    fun loadPoojaIndex(context: Context, fileName: String): List<PoojaIndexItem> {
        return try {
            Log.d(TAG, "open: poojas/$fileName")

            context.assets.open("poojas/$fileName").use { input ->
                InputStreamReader(input).use { reader ->
                    val type = object : TypeToken<List<PoojaIndexItemDto>>() {}.type
                    val raw: List<PoojaIndexItemDto> = Gson().fromJson(reader, type) ?: emptyList()

                    raw.mapNotNull { dto ->
                        val safeName = dto.name?.trim().takeUnless { it.isNullOrBlank() } ?: return@mapNotNull null
                        val safeId = dto.id?.trim().takeUnless { it.isNullOrBlank() } ?: slugify(safeName)
                        val safeFile = dto.file?.trim().takeUnless { it.isNullOrBlank() } ?: return@mapNotNull null
                        val safeImage = sanitizeDrawableName(dto.image)

                        PoojaIndexItem(
                            id = safeId,
                            name = safeName,
                            image = safeImage ?: "default_image", // must pass a string
                            file = safeFile,
                            category = dto.category,
                            isPremium = dto.isPremium ?: false,
                            scrollable = dto.scrollable ?: false
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading poojas/$fileName", e)
            emptyList()
        }
    }

    private fun sanitizeDrawableName(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val base = raw.substringAfterLast('/').substringBeforeLast('.')
        val lower = base.lowercase(Locale.ROOT)
        val clean = lower.replace(Regex("[^a-z0-9_]+"), "_").trim('_')
        return clean.ifBlank { null }
    }

    private fun slugify(input: String): String {
        val lower = input.lowercase(Locale.ROOT)
        return lower.replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "pooja" }
    }
}
