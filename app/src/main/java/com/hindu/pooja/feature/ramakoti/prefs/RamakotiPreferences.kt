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
private val Context.ramakotiDataStore by preferencesDataStore(name = DS_NAME)

class RamakotiPreferences private constructor(private val appContext: Context) {

    private val KEY_TARGET_COUNT           = intPreferencesKey("target_count")
    private val KEY_LAST_CERT_FOR_TARGET   = intPreferencesKey("last_cert_for_target")
    private val KEY_SELECTED_LANGUAGE      = stringPreferencesKey("selected_language")
    private val KEY_LAST_PROMPTED_FOR_TGT  = intPreferencesKey("last_prompted_for_target")
    private val KEY_REMINDER_ENABLED       = booleanPreferencesKey("reminder_enabled")
    private val KEY_REMINDER_HOUR          = intPreferencesKey("reminder_hour")
    private val KEY_REMINDER_MINUTE        = intPreferencesKey("reminder_minute")
    private val KEY_CURRENT_RUN_ID         = stringPreferencesKey("current_run_id") // 🆕 Added

    private val data: Flow<Preferences> get() = appContext.ramakotiDataStore.data

    // 🔹 Existing preference flows
    val targetCount: Flow<Int> = data.map { it[KEY_TARGET_COUNT] ?: 10_000_000 }
    val lastCertForTarget: Flow<Int> = data.map { it[KEY_LAST_CERT_FOR_TARGET] ?: 0 }
    val selectedLanguage: Flow<String> = data.map { it[KEY_SELECTED_LANGUAGE] ?: "en" }
    val lastPromptedForTarget: Flow<Int> = data.map { it[KEY_LAST_PROMPTED_FOR_TGT] ?: 0 }

    val reminderEnabled: Flow<Boolean> = data.map { it[KEY_REMINDER_ENABLED] ?: false }
    val reminderHour: Flow<Int> = data.map { it[KEY_REMINDER_HOUR] ?: 7 }
    val reminderMinute: Flow<Int> = data.map { it[KEY_REMINDER_MINUTE] ?: 0 }

    // 🆕 Added: current run ID tracking
    val currentRunId: Flow<String> = data.map { it[KEY_CURRENT_RUN_ID] ?: "" }

    /** Save a new run ID when user sets a new target */
    suspend fun setCurrentRunId(runId: String) {
        appContext.ramakotiDataStore.edit { it[KEY_CURRENT_RUN_ID] = runId }
    }

    /** Clears the active run ID (if user resets or logs out) */
    suspend fun clearCurrentRunId() {
        appContext.ramakotiDataStore.edit { it.remove(KEY_CURRENT_RUN_ID) }
    }

    // 🔹 Existing setters
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

    companion object {
        @Volatile private var INSTANCE: RamakotiPreferences? = null

        fun getInstance(context: Context): RamakotiPreferences =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RamakotiPreferences(context.applicationContext).also { INSTANCE = it }
            }
    }
}
