package com.hindu.pooja.feature.ramakoti.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Source of truth for Ramakoti "journey" fields stored in:
 * users/{uid}/ramakoti/ramakotiJourney
 *
 * NOTE:
 *  - Per-tap increments are handled by RamakotiSyncManager (transactional).
 *  - This repository only reads the journey doc and performs safe MERGE writes
 *    for actions like resetting the batch or starting the next crore.
 */
class RamakotiRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    data class Journey(
        val totalCount: Long = 0L,
        val currentBatchCount: Int = 0,
        val currentCrore: Int = 1,
        val language: String = "en"
    )

    private fun uid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")

    private fun journeyDoc() = db.collection("users")
        .document(uid())
        .collection("ramakoti")
        .document("ramakotiJourney")

    /** Read journey; create with defaults if it does not exist (MERGE). */
    suspend fun getJourney(): Journey {
        val ref = journeyDoc()
        val snap = ref.get().await()
        if (!snap.exists()) {
            val j = Journey()
            ref.set(
                mapOf(
                    "totalCount" to j.totalCount,
                    "currentBatchCount" to j.currentBatchCount,
                    "currentCrore" to j.currentCrore,
                    "language" to j.language,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            return j
        }
        return Journey(
            totalCount = snap.getLong("totalCount") ?: 0L,
            currentBatchCount = (snap.getLong("currentBatchCount") ?: 0L).toInt(),
            currentCrore = (snap.getLong("currentCrore") ?: 1L).toInt(),
            language = snap.getString("language") ?: "en"
        )
    }

    /**
     * Reset the batch counter to 0 (after 108 celebration).
     * Uses MERGE so there is no base-version conflict.
     */
    suspend fun resetBatchCounter(): Journey {
        val ref = journeyDoc()
        ref.set(
            mapOf(
                "currentBatchCount" to 0,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        return getJourney()
    }

    /**
     * Start the next crore: increments currentCrore and resets batch to 0.
     * Uses MERGE to avoid FAILED_PRECONDITION.
     */
    suspend fun startSecondCrore(): Journey {
        val ref = journeyDoc()
        // read existing to compute nextCrore safely
        val snap = ref.get().await()
        val current = (snap.getLong("currentCrore") ?: 1L).toInt()
        val next = current + 1

        ref.set(
            mapOf(
                "currentCrore" to next,
                "currentBatchCount" to 0,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        return getJourney()
    }

    /** Update preferred language on the journey doc. */
    suspend fun updateLanguage(language: String) {
        journeyDoc().set(
            mapOf(
                "language" to language,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }
}
