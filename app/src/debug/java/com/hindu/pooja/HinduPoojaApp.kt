package com.hindu.pooja

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HinduPoojaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        val appCheck = FirebaseAppCheck.getInstance()

        // Debug builds → Debug App Check provider
        appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        Log.i(TAG, "AppCheck: Debug provider INSTALLED")

        // Probe token length so we can verify real vs placeholder
        fetchAppCheckToken(force = true)
        Handler(Looper.getMainLooper()).postDelayed(
            { fetchAppCheckToken(force = false) },
            1200L
        )
    }

    private fun fetchAppCheckToken(force: Boolean) {
        FirebaseAppCheck.getInstance().getToken(force)
            .addOnSuccessListener { res ->
                val token = res.token.orEmpty()
                val isReal = token.length > 150
                Log.i(
                    TAG,
                    if (isReal) "✅ AppCheck REAL token (len=${token.length}) force=$force"
                    else        "⚠️ AppCheck PLACEHOLDER token (len=${token.length}) force=$force"
                )
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ AppCheck token fetch failed (force=$force): ${e.message}")
            }
    }

    companion object { private const val TAG = "AppCheck" }
}
