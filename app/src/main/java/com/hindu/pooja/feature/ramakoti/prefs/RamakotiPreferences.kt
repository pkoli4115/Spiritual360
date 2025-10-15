package com.hindu.pooja.feature.ramakoti.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DS_NAME = "ramakoti_prefs"

/**
 * IMPORTANT: DataStore delegate MUST be top-level.
 * Do NOT place this inside a class/object.
 */
private val Context.ramakotiDataStore by preferencesDataStore(name = DS_NAME)

class RamakotiPreferences private constructor(private val appContext: Context) {

    // ---------- Keys ----------
    private val KEY_TARGET_COUNT           = intPreferencesKey("target_count")              // 1_00_000 / 10_00_000 / 1_00_00_000
    private val KEY_LAST_CERT_FOR_TARGET   = intPreferencesKey("last_cert_for_target")      // guard to avoid duplicate auto-issue
    private val KEY_SELECTED_LANGUAGE      = stringPreferencesKey("selected_language")      // "en" | "te" | "hi"

    // Persisted “I already showed the next-target prompt for this target”
    private val KEY_LAST_PROMPTED_FOR_TGT  = intPreferencesKey("last_prompted_for_target")

    // Reminders
    private val KEY_REMINDER_ENABLED       = booleanPreferencesKey("reminder_enabled")
    private val KEY_REMINDER_HOUR          = intPreferencesKey("reminder_hour")             // 24h
    private val KEY_REMINDER_MINUTE        = intPreferencesKey("reminder_minute")

    // ---------- Flows ----------
    val targetCount: Flow<Int> = data.map { it[KEY_TARGET_COUNT] ?: 10_000_000 }
    val lastCertForTarget: Flow<Int> = data.map { it[KEY_LAST_CERT_FOR_TARGET] ?: 0 }
    val selectedLanguage: Flow<String> = data.map { it[KEY_SELECTED_LANGUAGE] ?: "en" }

    val lastPromptedForTarget: Flow<Int> = data.map { it[KEY_LAST_PROMPTED_FOR_TGT] ?: 0 }

    val reminderEnabled: Flow<Boolean> = data.map { it[KEY_REMINDER_ENABLED] ?: false }
    val reminderHour: Flow<Int> = data.map { it[KEY_REMINDER_HOUR] ?: 7 }
    val reminderMinute: Flow<Int> = data.map { it[KEY_REMINDER_MINUTE] ?: 0 }

    // ---------- Mutators ----------
    suspend fun setTargetCount(target: Int) {
        appContext.ramakotiDataStore.edit { it[KEY_TARGET_COUNT] = target }
    }

    suspend fun markCertIssuedFor(target: Int) {
        appContext.ramakotiDataStore.edit { it[KEY_LAST_CERT_FOR_TARGET] = target }
    }

    suspend fun setSelectedLanguage(lang: String) {
        appContext.ramakotiDataStore.edit { it[KEY_SELECTED_LANGUAGE] = lang }
    }

    suspend fun setLastPromptedForTarget(target: Int) {
        appContext.ramakotiDataStore.edit { it[KEY_LAST_PROMPTED_FOR_TGT] = target }
    }
    suspend fun clearLastPromptedForTarget() = setLastPromptedForTarget(0)

    suspend fun setReminderEnabled(enabled: Boolean) {
        appContext.ramakotiDataStore.edit { it[KEY_REMINDER_ENABLED] = enabled }
    }
    suspend fun setReminderHour(hour24: Int) {
        appContext.ramakotiDataStore.edit { it[KEY_REMINDER_HOUR] = hour24.coerceIn(0, 23) }
    }
    suspend fun setReminderMinute(minute: Int) {
        appContext.ramakotiDataStore.edit { it[KEY_REMINDER_MINUTE] = minute.coerceIn(0, 59) }
    }

    // ---------- Internal ----------
    private val data: Flow<Preferences> get() = appContext.ramakotiDataStore.data

    companion object {
        @Volatile private var INSTANCE: RamakotiPreferences? = null

        fun getInstance(context: Context): RamakotiPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RamakotiPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
