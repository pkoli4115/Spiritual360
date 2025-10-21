package com.hindu.pooja.feature.ramakoti.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

// Top-level (recommended) DataStore delegate
private const val DS_NAME = "ramakoti_language_prefs"
private val Context.ramakotiLangDataStore by preferencesDataStore(name = DS_NAME)

class LanguagePreferenceManager private constructor(
    private val appContext: Context
) {

    companion object {
        @Volatile private var INSTANCE: LanguagePreferenceManager? = null

        fun getInstance(context: Context): LanguagePreferenceManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: LanguagePreferenceManager(context.applicationContext).also { INSTANCE = it }
            }

        private fun keyFor(uid: String): Preferences.Key<String> =
            stringPreferencesKey("lang_$uid")
    }

    /**
     * Flow of language code ("en"/"hi"/"te"/...) for a user.
     * ✅ IMPORTANT: For brand-new users (no value saved), we emit "" (empty)
     * so the NavHost can route to the LanguageSelection screen.
     */
    fun languageFlowFor(uid: String?): Flow<String> =
        appContext.ramakotiLangDataStore.data.map { prefs ->
            when {
                uid.isNullOrBlank() -> ""                      // no user yet → force picker
                else -> prefs[keyFor(uid)] ?: ""               // unset → empty (not "en")
            }
        }

    /** Persist language for a user. */
    suspend fun setLanguageFor(uid: String, code: String) {
        if (uid.isBlank()) return
        appContext.ramakotiLangDataStore.edit { prefs ->
            prefs[keyFor(uid)] = code
        }
    }

    /**
     * Get current language for a user, or null if none set / uid blank.
     * (Use when you need a one-shot value instead of observing the Flow.)
     */
    suspend fun getLanguageFor(uid: String): String? {
        if (uid.isBlank()) return null
        return appContext.ramakotiLangDataStore.data
            .map { it[keyFor(uid)] }
            .firstOrNull()
    }

    /** Optional helper if you ever want to reset language and re-show the picker. */
    suspend fun clearLanguageFor(uid: String) {
        if (uid.isBlank()) return
        appContext.ramakotiLangDataStore.edit { prefs ->
            prefs.remove(keyFor(uid))
        }
    }
}
