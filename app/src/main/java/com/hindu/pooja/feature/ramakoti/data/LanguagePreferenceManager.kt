package com.hindu.pooja.feature.ramakoti.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DS_NAME = "language_prefs"
val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = DS_NAME)

/**
 * Stores language per user id. Use getInstance(context) — ctor is private.
 */
class LanguagePreferenceManager private constructor(private val appContext: Context) {

    private val ds = appContext.languageDataStore
    private val KEY_PREFIX = "lang_for_uid_" // per-user keys

    /** Emits "" when not set for this uid. */
    fun languageFlowFor(uid: String?): Flow<String> {
        val key = stringPreferencesKey(KEY_PREFIX + (uid ?: "anon"))
        return ds.data.map { it[key] ?: "" }
    }

    /** Save language for the given uid (null → anonymous). */
    suspend fun setLanguageFor(uid: String?, lang: String) {
        val key = stringPreferencesKey(KEY_PREFIX + (uid ?: "anon"))
        ds.edit { it[key] = lang }
    }

    companion object {
        @Volatile private var INSTANCE: LanguagePreferenceManager? = null

        fun getInstance(context: Context): LanguagePreferenceManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: LanguagePreferenceManager(context.applicationContext).also { INSTANCE = it }
            }
    }
}
