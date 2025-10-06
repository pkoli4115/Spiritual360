package com.hindu.pooja.ui.screens

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.hindu.pooja.data.PoojaContentLoader
import com.hindu.pooja.model.PoojaDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.hindu.pooja.feature.elearning.ui.WikiReaderScreen
import com.hindu.pooja.feature.elearning.ui.Lesson
import com.hindu.pooja.util.TtsHelper
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun PoojaDetailScreen(fileName: String, navController: NavController) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("Pooja") }
    var lang by remember { mutableStateOf("te") }
    var lessons by remember { mutableStateOf<List<Lesson>>(emptyList()) }

    // Try normal loader first; on error, fall back to tolerant parser
    LaunchedEffect(fileName) {
        val result: LoadedOrFallback = withContext(Dispatchers.IO) {
            try {
                val detail: PoojaDetail? = PoojaContentLoader.loadPoojaContent(context, fileName)
                LoadedOrFallback.SuccessFromModel(detail)
            } catch (_: Throwable) {
                parseRawAssetToLessons(context, fileName)
            }
        }

        when (result) {
            is LoadedOrFallback.SuccessFromModel -> {
                val d: PoojaDetail? = result.detail
                title = d?.name ?: d?.content?.get("name") ?: "Pooja"
                lang = d?.language ?: "te"
                lessons = buildPaginatedLessonsFromDetail(d)
            }
            is LoadedOrFallback.FallbackRaw -> {
                title = result.meta.title
                lang = result.meta.lang
                lessons = result.lessons
            }
        }

        if (lessons.isEmpty()) {
            lessons = listOf(Lesson(id = "empty", title = title, content = ""))
        }
    }

    val tts = remember { TtsHelper(context) }

    WikiReaderScreen(
        lessons = lessons,
        initialIndex = 0,
        ttsHelper = tts,
        title = title,
        languageCode = lang,
        onBack = { navController.popBackStack() },
        onLastPage = { /* optional */ }
    )
}

/* ----------------------------- Result wrapper ------------------------------ */

private sealed class LoadedOrFallback {
    data class SuccessFromModel(val detail: PoojaDetail?) : LoadedOrFallback()
    data class FallbackRaw(val meta: Meta, val lessons: List<Lesson>) : LoadedOrFallback()
}

private data class Meta(val title: String, val lang: String)

/* ---------------------- Fallback JSON tolerant parsing --------------------- */

private fun parseRawAssetToLessons(
    context: Context,
    assetPath: String
): LoadedOrFallback.FallbackRaw {
    val json = context.assets.open(assetPath).use { it.readBytes().toString(Charsets.UTF_8) }
    val root = JSONObject(json)

    val title = root.optString("name_te")
        .ifBlank { root.optString("name") }
        .ifBlank { root.optString("name_en") }
        .ifBlank { "Pooja" }

    val lang = root.optString("language", "te")
    val content = root.optJSONObject("content")

    val lessons = mutableListOf<Lesson>()

    // 1) Verses: string | [string] | [{title, lines:[...]}]
    val versesLines = mutableListOf<String>()
    content?.opt("verses")?.let { v ->
        when (v) {
            is JSONArray -> {
                if (v.length() > 0 && v.opt(0) is JSONObject) {
                    for (i in 0 until v.length()) {
                        val sec = v.optJSONObject(i) ?: continue
                        val lines = sec.optJSONArray("lines") ?: continue
                        for (j in 0 until lines.length()) {
                            val line = lines.optString(j).trim()
                            if (line.isNotEmpty()) versesLines += line
                        }
                    }
                } else {
                    for (i in 0 until v.length()) {
                        val line = v.optString(i).trim()
                        if (line.isNotEmpty()) versesLines += line
                    }
                }
            }
            is String -> versesLines += v.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
    if (versesLines.isNotEmpty()) {
        lessons += paginateLines(
            lines = versesLines,
            baseTitle = title,
            numberItems = true
        )
    }

    // 2) Slokas (top-level array, if present)
    val slokasLines = mutableListOf<String>()
    root.optJSONArray("slokas")?.let { arr ->
        for (i in 0 until arr.length()) {
            val line = arr.optString(i).trim()
            if (line.isNotEmpty()) slokasLines += line
        }
    }
    if (slokasLines.isNotEmpty()) {
        lessons += paginateLines(
            lines = slokasLines,
            baseTitle = title,
            numberItems = true
        )
    }

    // 3) Other content sections (exclude "verses"); paginate by paragraphs, no numbering
    content?.let { cobj ->
        val keys = cobj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key == "verses") continue
            val value = cobj.optString(key, "").trim()
            if (value.isNotEmpty()) {
                val paragraphs = value.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
                lessons += paginateLines(
                    lines = paragraphs,
                    baseTitle = key.ifBlank { title },
                    numberItems = false
                )
            }
        }
    }

    return LoadedOrFallback.FallbackRaw(
        meta = Meta(title = title, lang = lang),
        lessons = lessons.ifEmpty { listOf(Lesson("empty", title, "")) }
    )
}

/* ----------------------- Model-based pagination path ----------------------- */

private fun buildPaginatedLessonsFromDetail(detail: PoojaDetail?): List<Lesson> {
    if (detail == null) return emptyList()

    val out = mutableListOf<Lesson>()
    val baseTitle = detail.name?.ifBlank { null } ?: "Pooja"

    // Verses: prefer detail.content["verses"] as String (newline-separated), else detail.verses
    val verses: List<String> = run {
        val raw = detail.content?.get("verses")
        when (raw) {
            is String -> raw.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
            else -> detail.verses?.filter { it.isNotBlank() } ?: emptyList()
        }
    }
    if (verses.isNotEmpty()) {
        out += paginateLines(
            lines = verses,
            baseTitle = detail.name ?: baseTitle,
            numberItems = true
        )
    }

    // Slokas
    val slokas = detail.slokas?.filter { it.isNotBlank() } ?: emptyList()
    if (slokas.isNotEmpty()) {
        out += paginateLines(
            lines = slokas,
            baseTitle = detail.name ?: baseTitle,
            numberItems = true
        )
    }

    // Other sections from content (excluding "verses")
    detail.content
        ?.filterKeys { it != "verses" }
        ?.forEach { (key, value) ->
            val text = value?.trim().orEmpty()
            if (text.isNotEmpty()) {
                val paragraphs = text.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
                out += paginateLines(
                    lines = paragraphs,
                    baseTitle = key.ifBlank { detail.name ?: baseTitle },
                    numberItems = false
                )
            }
        }

    return out
}

/* -------------------------- Shared paginator ------------------------------- */

private fun paginateLines(
    lines: List<String>,
    baseTitle: String,
    numberItems: Boolean,
    softCharBudget: Int = 620,
    hardLineCap: Int = 18
): List<Lesson> {
    if (lines.isEmpty()) return emptyList()

    val pages = mutableListOf<Lesson>()
    var i = 0
    var pageNo = 0

    while (i < lines.size) {
        var count = 0
        var chars = 0
        val start = i
        while (i < lines.size && count < hardLineCap) {
            val len = lines[i].length
            val nextChars = if (count == 0) len else chars + 1 + len
            if (count >= 2 && nextChars > softCharBudget) break
            chars = nextChars
            count++
            i++
        }

        val pageLines = lines.subList(start, (start + count).coerceAtMost(lines.size))
        val content = if (numberItems) {
            pageLines.mapIndexed { idx, line -> "${start + idx + 1}. $line" }.joinToString("\n\n")
        } else {
            pageLines.joinToString("\n\n")
        }

        pages += Lesson(
            id = "${baseTitle.hashCode()}-${pageNo++}",
            title = baseTitle,
            content = content
        )
    }

    return pages
}

