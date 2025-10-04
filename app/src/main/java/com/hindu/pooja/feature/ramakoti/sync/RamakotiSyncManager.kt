package com.hindu.pooja.feature.ramakoti.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RamakotiSyncManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private fun uidOrNull() = auth.currentUser?.uid

    private fun dayKey(ts: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(Date(ts))
    }

    suspend fun addCount(n: Int) {
        val uid = uidOrNull() ?: return
        if (n <= 0) return
        val key = dayKey()
        val docRef = db.collection("users").document(uid)
            .collection("ramakoti").document(key)

        db.runTransaction { tx ->
            val snap = tx.get(docRef)
            if (!snap.exists()) {
                tx.set(docRef, mapOf(
                    "count" to n,
                    "lastUpdated" to FieldValue.serverTimestamp()
                ))
            } else {
                tx.update(docRef, mapOf(
                    "count" to FieldValue.increment(n.toLong()),
                    "lastUpdated" to FieldValue.serverTimestamp()
                ))
            }
        }.await()
    }

    suspend fun clearAllForToday() {
        val uid = uidOrNull() ?: return
        val key = dayKey()
        val docRef = db.collection("users").document(uid)
            .collection("ramakoti").document(key)
        docRef.set(mapOf("count" to 0, "lastUpdated" to FieldValue.serverTimestamp())).await()
    }
}
