package com.hindu.pooja.feature.ramakoti.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

private const val DS_NAME = "ramakoti_prefs"
private val Context.dataStore by preferencesDataStore(name = DS_NAME)

enum class RamaLang(val code: String, val display: String, val phrase: String) {
    EN("en", "English", "Jai Sri Ram"),
    HI("hi", "हिंदी", "जय श्री राम"),
    TE("te", "తెలుగు", "జై శ్రీరాం")
}

@Singleton
class RamakotiPreferences @Inject constructor(
    @ApplicationContext private val appCtx: Context
) {
    private object Keys {
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val LANG = stringPreferencesKey("lang_code")
    }

    val reminderEnabled: Flow<Boolean> =
        appCtx.dataStore.data.map { it[Keys.REMINDER_ENABLED] ?: false }

    val reminderHour: Flow<Int> =
        appCtx.dataStore.data.map { it[Keys.REMINDER_HOUR] ?: 7 }

    val reminderMinute: Flow<Int> =
        appCtx.dataStore.data.map { it[Keys.REMINDER_MINUTE] ?: 0 }

    val language: Flow<RamaLang> =
        appCtx.dataStore.data.map { prefs ->
            when (prefs[Keys.LANG] ?: RamaLang.TE.code) {
                RamaLang.EN.code -> RamaLang.EN
                RamaLang.HI.code -> RamaLang.HI
                else -> RamaLang.TE
            }
        }

    suspend fun setReminderEnabled(value: Boolean) {
        appCtx.dataStore.edit { it[Keys.REMINDER_ENABLED] = value }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        appCtx.dataStore.edit {
            it[Keys.REMINDER_HOUR] = hour
            it[Keys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun setLanguage(lang: RamaLang) {
        appCtx.dataStore.edit { it[Keys.LANG] = lang.code }
    }
}
