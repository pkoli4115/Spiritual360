package com.hindu.pooja.feature.ramakoti.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private const val TAG = "RamaSync"

class RamakotiSyncManager(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    suspend fun ensureMetaInitialized() {
        val uid = auth.currentUser?.uid ?: run {
            Log.w(TAG, "ensureMetaInitialized: no user"); return
        }
        val metaRef = db.collection("users").document(uid)
            .collection("ramakoti_meta").document("meta")

        val snap = metaRef.get().await()
        if (!snap.exists()) {
            Log.d(TAG, "Creating lifetime meta for uid=$uid")
            val init = mapOf(
                "lifetimeCount" to 0,
                "batches" to 0,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            metaRef.set(init, SetOptions.merge()).await()
        } else {
            Log.d(TAG, "Meta already exists for uid=$uid")
        }
    }

    suspend fun addCount(delta: Int) {
        if (delta == 0) return
        val uid = auth.currentUser?.uid ?: run {
            Log.w(TAG, "addCount($delta): no user"); return
        }
        val metaRef = db.collection("users").document(uid)
            .collection("ramakoti_meta").document("meta")

        ensureMetaInitialized()

        db.runTransaction { tx ->
            val cur = tx.get(metaRef)
            val oldLife = cur.getLong("lifetimeCount") ?: 0L
            val newLife = oldLife + delta

            val oldBlocks = (oldLife / 108L).toInt()
            val newBlocks = (newLife / 108L).toInt()
            val addBatches = (newBlocks - oldBlocks).coerceAtLeast(0)

            val updates = hashMapOf<String, Any>(
                "lifetimeCount" to newLife,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (addBatches > 0) {
                updates["batches"] = FieldValue.increment(addBatches.toLong())
                updates["lastFinishedAt"] = FieldValue.serverTimestamp()
            }
            tx.set(metaRef, updates, SetOptions.merge())
            null
        }.await()
        Log.d(TAG, "addCount($delta) OK")
    }
}
