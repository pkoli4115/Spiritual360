package com.hindu.pooja.feature.ramakoti.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the per-day count and a stable "meta" doc (lifetime & batches).
 *
 * Day doc :  /users/{uid}/ramakoti/{yyyy-MM-dd}
 * Meta doc:  /users/{uid}/ramakoti_meta/meta
 */
@Singleton
class RamakotiSyncManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    data class AddCountResult(
        val todayCountAfter: Int,
        val completedBatches: Int,
        val justCompleted108: Boolean
    )

    private fun uidOrNull() = auth.currentUser?.uid

    private fun dayKey(ts: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(Date(ts))
    }

    /**
     * (Optional) Call once when the Ramakoti screen opens.
     * Ensures the meta doc exists so external code that does "update" won't crash.
     */
    suspend fun ensureMetaInitialized() {
        val uid = uidOrNull() ?: return
        val metaRef = db.collection("users").document(uid)
            .collection("ramakoti_meta").document("meta")

        metaRef.set(
            mapOf(
                "lifetimeCount" to FieldValue.increment(0),
                "batches" to FieldValue.increment(0),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    /**
     * Increment today's count by [n]; also upsert lifetime meta and compute celebration.
     */
    suspend fun addCount(n: Int): AddCountResult {
        val uid = uidOrNull() ?: return AddCountResult(0, 0, false)
        if (n <= 0) return AddCountResult(0, 0, false)

        val key = dayKey()
        val dayRef  = db.collection("users").document(uid)
            .collection("ramakoti").document(key)
        val metaRef = db.collection("users").document(uid)
            .collection("ramakoti_meta").document("meta")

        var result = AddCountResult(0, 0, false)

        db.runTransaction { tx ->
            // ---- Day doc (create/merge) ----
            val daySnap = tx.get(dayRef)
            val before = (daySnap.getLong("count") ?: 0L).toInt()
            val after  = before + n

            if (!daySnap.exists()) {
                tx.set(dayRef, mapOf(
                    "count" to after,
                    "lastUpdated" to FieldValue.serverTimestamp()
                ))
            } else {
                tx.update(dayRef, mapOf(
                    "count" to after,
                    "lastUpdated" to FieldValue.serverTimestamp()
                ))
            }

            // detect 108 completions in this call
            val completedBatchesNow =
                (after / 108 - before / 108).coerceAtLeast(0)

            // ---- Meta doc (create/merge) ----
            tx.set(
                metaRef,
                mapOf(
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "lifetimeCount" to FieldValue.increment(n.toLong())
                ),
                SetOptions.merge()
            )

            if (completedBatchesNow > 0) {
                tx.set(
                    metaRef,
                    mapOf(
                        "batches" to FieldValue.increment(completedBatchesNow.toLong()),
                        "lastFinishedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }

            result = AddCountResult(
                todayCountAfter = after,
                completedBatches = completedBatchesNow,
                justCompleted108 = completedBatchesNow > 0
            )
            null
        }.await()

        return result
    }

    /**
     * Reset ONLY today's grid; lifetime remains a historical total.
     */
    suspend fun clearAllForToday() {
        val uid = uidOrNull() ?: return
        val key = dayKey()
        val dayRef = db.collection("users").document(uid)
            .collection("ramakoti").document(key)

        dayRef.set(
            mapOf(
                "count" to 0,
                "lastUpdated" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }
}
