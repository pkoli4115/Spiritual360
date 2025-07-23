package com.hindu.pooja.app.util

import android.content.Context
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.*

object DeviceIdHelper {

    private const val PREF_FILE_NAME = "secure_device_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    fun getDeviceId(context: Context): String {
        // Step 1: Try getting ANDROID_ID (not reliable on some devices)
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        // Filter out known bad default ID
        if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            return androidId
        }

        // Step 2: Fallback – use EncryptedSharedPreferences to store UUID
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            PREF_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var storedId = sharedPreferences.getString(KEY_DEVICE_ID, null)

        if (storedId == null) {
            storedId = UUID.randomUUID().toString()
            sharedPreferences.edit().putString(KEY_DEVICE_ID, storedId).apply()
        }

        return storedId
    }
}
