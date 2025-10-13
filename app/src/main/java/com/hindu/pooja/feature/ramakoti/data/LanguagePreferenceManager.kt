package com.hindu.pooja.feature.ramakoti.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Stores Ramakoti language per Firebase user (UID).
 *
 * For a given uid:
 *  - languageFlowFor(uid) emits "" (blank) if not chosen yet for that user.
 *  - setLanguageFor(uid, lang) persists the choice for that user.
 */
class LanguagePreferenceManager(private val context: Context) {

    companion object {
        private val Context.ramakotiDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "ramakoti_prefs"
        )

        // Optional: track last user who saved a language (not required for guard)
        private val KEY_LAST_USER = stringPreferencesKey("ramakoti_last_user")

        private fun langKeyFor(uid: String) = stringPreferencesKey("ramakoti_language_$uid")
    }

    /**
     * Observe language for the given user ID.
     * If uid is null (not logged in), we expose blank so the app can decide.
     */
    fun languageFlowFor(uid: String?): Flow<String> {
        if (uid.isNullOrBlank()) return flowOf("")
        val key = langKeyFor(uid)
        return context.ramakotiDataStore.data.map { prefs ->
            prefs[key] ?: ""
        }
    }

    /**
     * Persist language for a particular user ID.
     * No-op if uid is null/blank.
     */
    suspend fun setLanguageFor(uid: String?, lang: String) {
        val id = uid ?: return
        val key = langKeyFor(id)
        context.ramakotiDataStore.edit { prefs ->
            prefs[key] = lang.lowercase()
            prefs[KEY_LAST_USER] = id
        }
    }
}
