package com.hindu.pooja.ui.ramayana.reader.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Loads Ramayana Kanda JSON modules from assets.
 *
 * We now support 3 languages with a consistent naming pattern:
 *
 *  Telugu:
 *   - ramayana/balakanda_te_wiki_simple.json
 *   - ramayana/ayodhyakanda_te_wiki_simple.json
 *   - ramayana/aranya_kanda_te_wiki_simple.json
 *   - ramayana/kishkindha_kanda_te_wiki_simple.json
 *   - ramayana/sundara_kanda_te_wiki_simple.json
 *   - ramayana/yuddha_kanda_te_wiki_simple.json
 *   - ramayana/uttara_kanda_te_wiki_simple.json
 *
 *  English:
 *   - ramayana/balakanda_en_wiki_simple.json
 *   - ramayana/ayodhyakanda_en_wiki_simple.json
 *   - ramayana/aranya_kanda_en_wiki_simple.json
 *   - ramayana/kishkindha_kanda_en_wiki_simple.json
 *   - ramayana/sundara_kanda_en_wiki_simple.json
 *   - ramayana/yuddha_kanda_en_wiki_simple.json
 *   - ramayana/uttara_kanda_en_wiki_simple.json
 *
 *  Hindi:
 *   - ramayana/balakanda_hi_wiki_simple.json
 *   - ramayana/ayodhyakanda_hi_wiki_simple.json
 *   - ramayana/aranya_kanda_hi_wiki_simple.json
 *   - ramayana/kishkindha_kanda_hi_wiki_simple.json
 *   - ramayana/sundara_kanda_hi_wiki_simple.json
 *   - ramayana/yuddha_kanda_hi_wiki_simple.json
 *   - ramayana/uttara_kanda_hi_wiki_simple.json
 */
object KandaJsonLoader {

    enum class Language {
        TE, EN, HI
    }

    /**
     * Base names (without lang suffix) for each Kanda.
     * The actual asset path is derived from [language].
     */
    enum class Kanda(val baseName: String) {
        BALA       ("balakanda"),
        AYODHYA    ("ayodhyakanda"),
        ARANYA     ("aranya_kanda"),
        KISHKINDHA ("kishkindha_kanda"),
        SUNDARA    ("sundara_kanda"),
        YUDDHA     ("yuddha_kanda"),
        UTTARA     ("uttara_kanda")
    }

    private val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    private fun assetPath(kanda: Kanda, language: Language): String {
        val suffix = when (language) {
            Language.TE -> "_te_wiki_simple.json"
            Language.EN -> "_en_wiki_simple.json"
            Language.HI -> "_hi_wiki_simple.json"
        }
        return "ramayana/${kanda.baseName}$suffix"
    }

    /**
     * Load a given Kanda in a given language.
     * Default language = Telugu to keep old code working.
     */
    fun load(
        context: Context,
        kanda: Kanda,
        language: Language = Language.TE
    ): KandaPayload {
        val file = assetPath(kanda, language)
        context.assets.open(file).use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { br ->
                return gson.fromJson(br, KandaPayload::class.java)
            }
        }
    }
}
