package com.hindu.pooja.data
import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore("xp_prefs")

object XpManager {
    private val XP_KEY = intPreferencesKey("xp")

    suspend fun addXp(xpToAdd: Int, context: Context? = null) {
        context?.dataStore?.edit { prefs ->
            val current = prefs[XP_KEY] ?: 0
            prefs[XP_KEY] = current + xpToAdd
        }
    }

    suspend fun getXp(context: Context? = null): Int {
        return context?.dataStore?.data?.first()?.get(XP_KEY) ?: 0
    }
}
