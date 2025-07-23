// File: app/src/main/java/com/hindu/pooja/HinduPoojaApp.kt
package com.hindu.pooja

import android.app.Application
import com.google.firebase.FirebaseApp
import com.hindu.pooja.auth.registerActivityListener
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HinduPoojaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        registerActivityListener() // ✅ Required for PhoneAuth callbacks
    }
}
