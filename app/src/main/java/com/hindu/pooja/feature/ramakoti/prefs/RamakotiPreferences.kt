package com.hindu.pooja.feature.ramakoti.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull // <-- added

private val Context.dataStore by preferencesDataStore(name = "ramakoti_prefs")

class RamakotiPreferences private constructor(private val context: Context) {

    companion object {
        private var INSTANCE: RamakotiPreferences? = null
        fun getInstance(context: Context): RamakotiPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RamakotiPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }

        private val KEY_TARGET_COUNT = intPreferencesKey("target_count")
        private val KEY_LAST_CERT_FOR_TARGET = intPreferencesKey("last_cert_for_target")

        // 🔔 Reminder fields
        private val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        private val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    /* ----------------- Ramakoti prefs ----------------- */
    val targetCount: Flow<Int> = context.dataStore.data.map { it[KEY_TARGET_COUNT] ?: 100_000 }
    val lastCertForTarget: Flow<Int> = context.dataStore.data.map { it[KEY_LAST_CERT_FOR_TARGET] ?: 0 }

    suspend fun setTargetCount(count: Int) {
        context.dataStore.edit { it[KEY_TARGET_COUNT] = count }
    }

    suspend fun markCertIssuedFor(target: Int) {
        context.dataStore.edit { it[KEY_LAST_CERT_FOR_TARGET] = target }
    }

    /* ----------------- Reminder prefs ----------------- */

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_REMINDER_ENABLED] ?: false }
    val reminderHour: Flow<Int> = context.dataStore.data.map { it[KEY_REMINDER_HOUR] ?: 7 }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[KEY_REMINDER_MINUTE] ?: 0 }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderHour(hour: Int) {
        context.dataStore.edit { it[KEY_REMINDER_HOUR] = hour }
    }

    suspend fun setReminderMinute(minute: Int) {
        context.dataStore.edit { it[KEY_REMINDER_MINUTE] = minute }
    }

    /* ----------------- Safe getters (for BootReceiver, Scheduler) ----------------- */

    suspend fun isReminderEnabled(): Boolean {
        return reminderEnabled.map { it }.firstOrNull() ?: false
    }

    suspend fun getReminderHour(): Int {
        return reminderHour.map { it }.firstOrNull() ?: 7
    }

    suspend fun getReminderMinute(): Int {
        return reminderMinute.map { it }.firstOrNull() ?: 0
    }
}
