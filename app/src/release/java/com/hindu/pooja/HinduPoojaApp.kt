package com.hindu.pooja

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HinduPoojaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        // Release builds → Play Integrity provider
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        Log.i(TAG, "AppCheck: Play Integrity provider INSTALLED")
        // No debug token probes here; tokens are valid only when installed from Google Play.
    }

    companion object { private const val TAG = "AppCheck" }
}
