package com.hindu.pooja.feature.ramakoti.data

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ReferralRepository {
    private val db = Firebase.firestore

    suspend fun recordReferral(referrerUid: String, referredUid: String) {
        val col = db.collection("users").document(referrerUid).collection("referrals")
        val data = mapOf("referredUid" to referredUid, "createdAt" to com.google.firebase.Timestamp.now())
        col.add(data).await()
    }
}
