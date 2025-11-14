package com.hindu.pooja.util

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

object UpdateManager {

    private const val REQ_CODE = 7001
    private const val TAG = "InAppUpdate"

    private var appUpdateManager: AppUpdateManager? = null
    private var installListener: InstallStateUpdatedListener? = null

    /**
     * Call in onStart(). preferImmediate=true for blocking/critical updates.
     */
    fun checkAndPrompt(activity: Activity, preferImmediate: Boolean = false) {
        val manager = AppUpdateManagerFactory.create(activity).also { appUpdateManager = it }

        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                when {
                    preferImmediate && info.isImmediateUpdateAllowed -> {
                        startUpdate(activity, info, AppUpdateType.IMMEDIATE)
                    }
                    info.isFlexibleUpdateAllowed -> {
                        // Flexible: download in background, then prompt to restart.
                        startUpdate(activity, info, AppUpdateType.FLEXIBLE)
                        attachFlexibleListener()
                    }
                    info.isImmediateUpdateAllowed -> {
                        startUpdate(activity, info, AppUpdateType.IMMEDIATE)
                    }
                    else -> Log.i(TAG, "Update available but no allowed type.")
                }
            } else {
                Log.i(TAG, "No update available.")
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "Update check failed: ${e.message}")
        }
    }

    /**
     * For IMMEDIATE updates that were started and app got backgrounded,
     * Play asks you to resume the flow in onResume().
     */
    fun resumeIfNeeded(activity: Activity) {
        val manager = appUpdateManager ?: AppUpdateManagerFactory.create(activity).also { appUpdateManager = it }
        manager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Resume IMMEDIATE update
                startUpdate(activity, info, AppUpdateType.IMMEDIATE)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == REQ_CODE) {
            // For IMMEDIATE: resultCode == Activity.RESULT_OK on success
            // For FLEXIBLE: handled by install state listener
            Log.i(TAG, "Update flow finished with resultCode=$resultCode")
        }
    }

    private fun startUpdate(
        activity: Activity,
        info: com.google.android.play.core.appupdate.AppUpdateInfo,
        @AppUpdateType type: Int
    ) {
        try {
            // NOTE: this is the correct Play Core call (the older helper you saw doesn't exist)
            appUpdateManager?.startUpdateFlowForResult(
                info,
                type,
                activity,
                REQ_CODE
            )
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "startUpdateFlowForResult failed: ${e.message}")
        } catch (t: Throwable) {
            Log.e(TAG, "startUpdateFlowForResult error: ${t.message}")
        }
    }

    private fun attachFlexibleListener() {
        val manager = appUpdateManager ?: return
        if (installListener != null) return

        installListener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    Log.i(TAG, "Flexible update downloaded; completing.")
                    // Triggers app restart prompt (or immediate restart, depending on device)
                    manager.completeUpdate()
                    detachFlexibleListener()
                }
                InstallStatus.FAILED -> {
                    Log.w(TAG, "Flexible update failed: ${state.installErrorCode()}")
                    detachFlexibleListener()
                }
                else -> {} // ignore other states
            }
        }
        manager.registerListener(installListener!!)
    }

    private fun detachFlexibleListener() {
        appUpdateManager?.let { mgr ->
            installListener?.let { mgr.unregisterListener(it) }
        }
        installListener = null
    }
}
