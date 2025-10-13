package com.hindu.pooja.feature.ramakoti.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Phase-2 orchestration:
 * - When a crore completes, write history + return flag for UI to prompt certificate/reflection.
 * - Records a history row: users/{uid}/ramakotiHistory/{autoId}
 */
class RamakotiPhase2Repository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private fun uid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("Not signed in")

    private fun historyCol() = db.collection("users")
        .document(uid())
        .collection("ramakotiHistory")

    /**
     * Call when totalCount crosses a crore boundary (1e7, 2e7, ...)
     */
    suspend fun onCroreCompleted(
        croreNumber: Int,
        totalAtCompletion: Long,
        certificateId: String?,
        certificateUrl: String?
    ) = withContext(Dispatchers.IO) {
        val data = hashMapOf(
            "croreNumber" to croreNumber,
            "completedAt" to FieldValue.serverTimestamp(),
            "totalAtCompletion" to totalAtCompletion,
            "certificateId" to certificateId,
            "certificateUrl" to certificateUrl
        )
        historyCol().add(data).await()
    }

    /**
     * Helper: returns true if totalCount just reached a new crore threshold.
     * e.g., 10_000_000, 20_000_000, ...
     */
    fun isCroreMilestone(totalCount: Long): Pair<Boolean, Int> {
        if (totalCount <= 0) return false to 0
        val croreNumber = (totalCount / 10_000_000L).toInt()
        val isExact = totalCount % 10_000_000L == 0L
        return (isExact && croreNumber > 0) to croreNumber
    }
}
