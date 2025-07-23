// File: app/src/main/java/com/hindu/pooja/auth/currentActivityOrNull.kt
package com.hindu.pooja.auth

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

private var currentActivity: Activity? = null

fun Application.currentActivityOrNull(): Activity? = currentActivity

fun Application.registerActivityListener() {
    registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            currentActivity = activity
        }

        override fun onActivityStarted(activity: Activity) { currentActivity = activity }
        override fun onActivityResumed(activity: Activity) { currentActivity = activity }
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    })
}
