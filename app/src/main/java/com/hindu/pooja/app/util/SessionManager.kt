package com.hindu.pooja.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hindu.pooja.app.util.DeviceIdHelper

object SessionManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Save the current device session to Firestore under /activeSessions/{uid}
     */
    fun saveSession(
        context: Context,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError(Exception("User not logged in"))
            return
        }

        val deviceId = DeviceIdHelper.getDeviceId(context)
        val sessionData = mapOf(
            "deviceId" to deviceId,
            "lastLogin" to FieldValue.serverTimestamp()
        )

        firestore.collection("activeSessions")
            .document(user.uid)
            .set(sessionData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    /**
     * Check if the current device matches the stored session
     * If not, sign the user out and call onInvalid()
     */
    fun checkSession(
        context: Context,
        onValid: () -> Unit,
        onInvalid: () -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onInvalid()
            return
        }

        val currentDeviceId = DeviceIdHelper.getDeviceId(context)

        firestore.collection("activeSessions")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val storedDeviceId = document.getString("deviceId")

                if (storedDeviceId == currentDeviceId) {
                    onValid()
                } else {
                    auth.signOut()
                    onInvalid()
                }
            }
            .addOnFailureListener {
                auth.signOut()
                onInvalid()
            }
    }
}
