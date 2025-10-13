package com.hindu.pooja.feature.profile.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Reads Ramakoti journey + history from Firestore.
 *
 * Journey doc:
 *   users/{uid}/ramakoti/ramakotiJourney
 *     - totalCount: Long
 *     - currentBatchCount: Int
 *     - currentCrore: Int
 *     - language: String
 *
 * Crore history:
 *   users/{uid}/ramakotiHistory/{autoId}
 *     - croreNumber: Int
 *     - completedAt: TS
 *     - totalAtCompletion: Long
 *     - certificateId: String?
 *     - certificateUrl: String?
 */
class JourneyRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    data class JourneySummary(
        val totalCount: Long = 0L,
        val currentBatchCount: Int = 0,
        val currentCrore: Int = 1,
        val language: String = "en"
    )

    data class CroreHistoryItem(
        val id: String,
        val croreNumber: Int,
        val completedAtMs: Long?,          // may be null while waiting for server TS
        val totalAtCompletion: Long,
        val certificateId: String?,
        val certificateUrl: String?
    )

    private fun uid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("Not signed in")

    suspend fun getJourney(): JourneySummary {
        val snap = db.collection("users").document(uid())
            .collection("ramakoti").document("ramakotiJourney")
            .get().await()

        if (!snap.exists()) return JourneySummary()

        return JourneySummary(
            totalCount = snap.getLong("totalCount") ?: 0L,
            currentBatchCount = (snap.getLong("currentBatchCount") ?: 0L).toInt(),
            currentCrore = (snap.getLong("currentCrore") ?: 1L).toInt(),
            language = snap.getString("language") ?: "en"
        )
    }

    suspend fun getCroreHistory(limit: Long = 50): List<CroreHistoryItem> {
        val q = db.collection("users").document(uid())
            .collection("ramakotiHistory")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(limit)

        val res = q.get().await()
        return res.documents.map { d ->
            CroreHistoryItem(
                id = d.id,
                croreNumber = (d.getLong("croreNumber") ?: 0L).toInt(),
                completedAtMs = d.getTimestamp("completedAt")?.toDate()?.time,
                totalAtCompletion = d.getLong("totalAtCompletion") ?: 0L,
                certificateId = d.getString("certificateId"),
                certificateUrl = d.getString("certificateUrl")
            )
        }
    }
}
