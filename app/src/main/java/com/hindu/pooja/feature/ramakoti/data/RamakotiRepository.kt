// FILE: app/src/main/java/com/hindu/pooja/feature/ramakoti/data/RamakotiRepository.kt
package com.hindu.pooja.feature.ramakoti.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RamakotiRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    /* ------------------------- Public API used by ViewModel ------------------------- */

    /** Return the latest in-progress batch, or (null, emptyList) if none exists. */
    suspend fun getActiveBatchOrNull(language: String): Pair<String?, List<Any?>> {
        val uid = userId()
        val col = batchesCol(uid)

        // Preferred: server-side filter + order (requires composite index)
        try {
            val qs = col
                .whereEqualTo("status", "in_progress")
                .orderBy("batchNumber", Query.Direction.DESCENDING)
                .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val doc = qs.documents.firstOrNull()
            if (doc != null) {
                val cells = ensure108(readCells(doc))
                return doc.id to cells
            }
        } catch (e: FirebaseFirestoreException) {
            // If index is missing, fall back to client-side max
            if (e.code != FirebaseFirestoreException.Code.FAILED_PRECONDITION) throw e
        }

        // Fallback
        val all = col.get().await().documents
        val latest = all
            .asSequence()
            .filter { it.getString("status") == "in_progress" }
            .maxByOrNull { it.getLong("batchNumber") ?: 0L }

        return if (latest == null) null to emptyList()
        else latest.id to ensure108(readCells(latest))
    }

    /** Create a brand-new in-progress batch prefilled with 108 empty cells. */
    suspend fun createNewInProgressBatch(language: String): Pair<String, List<Any?>> {
        val uid = userId()
        val col = batchesCol(uid)

        // Determine next batch number (best-effort)
        val lastNum = try {
            col.orderBy("batchNumber", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.getLong("batchNumber")
                ?.toInt() ?: 0
        } catch (_: Exception) {
            0
        }
        val nextNum = lastNum + 1

        val initialCells = (0 until 108).map { i ->
            mapOf(
                "index" to (i + 1),
                "filled" to false,
                "value" to "",
                "lang" to language.uppercase(),
                "ts" to 0L
            )
        }

        val ref = col.document()
        val data = mapOf(
            "status" to "in_progress",
            "batchNumber" to nextNum,
            "createdAt" to FieldValue.serverTimestamp(),
            "completedCells" to 0,
            "cells" to initialCells
        )
        ref.set(data).await()
        return ref.id to initialCells
    }

    /** Fill one cell (1-based index). No-op if already filled. */
    suspend fun fillCell(
        batchId: String,
        index: Int,
        value: String,
        lang: String,
        ts: Long
    ) {
        val uid = userId()
        val ref = batchesCol(uid).document(batchId)

        db.runTransaction { t ->
            val snap = t.get(ref)
            val cells = readCells(snap).toMutableList()

            val pos = (index - 1).coerceIn(0, 107)
            val cur = (cells[pos] as? Map<*, *>)?.toMutableMap() ?: mutableMapOf()
            if (cur["filled"] == true) return@runTransaction null // already filled

            cur["filled"] = true
            cur["value"] = value
            cur["lang"] = lang
            cur["ts"] = ts
            cells[pos] = cur

            val completed = (snap.getLong("completedCells")?.toInt() ?: 0) + 1
            t.update(ref, mapOf("cells" to cells, "completedCells" to completed))
            null
        }.await()
    }

    /**
     * Mark a batch as committed (only if completedCells == 108) and increment
     * /ramakotiProgress/{uid}.totalCount by 108 — all within a single transaction.
     */
    suspend fun commitBatchAndIncrementTotal(batchId: String) {
        val uid = userId()
        val rootRef = db.collection("ramakotiProgress").document(uid)
        val batchRef = rootRef.collection("batches").document(batchId)

        db.runTransaction { t ->
            // 1) All reads FIRST
            val batchSnap = t.get(batchRef)
            val rootSnap = t.get(rootRef)

            // If already committed, exit gracefully
            if (batchSnap.getString("status") == "committed") return@runTransaction null

            val completed = batchSnap.getLong("completedCells")?.toInt() ?: 0
            require(completed == 108) { "Cannot commit before 108 cells" }

            val currentTotal = rootSnap.getLong("totalCount")?.toInt() ?: 0

            // 2) All writes AFTER reads
            t.update(
                batchRef,
                mapOf(
                    "status" to "committed",
                    "committedAt" to FieldValue.serverTimestamp()
                )
            )
            t.update(rootRef, mapOf("totalCount" to currentTotal + 108))

            null
        }.await()
    }

    /** Read totalCount from /ramakotiProgress/{uid}. */
    suspend fun readTotalCount(): Int? {
        val uid = userId()
        val snap = db.collection("ramakotiProgress").document(uid).get().await()
        return snap.getLong("totalCount")?.toInt()
    }

    /* --------------------------------- Helpers -------------------------------- */

    private fun userId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

    private fun batchesCol(uid: String) =
        db.collection("ramakotiProgress").document(uid).collection("batches")

    private fun readCells(snap: DocumentSnapshot): List<Any?> {
        val raw = snap.get("cells") as? List<Any?> ?: emptyList()
        return ensure108(raw)
    }

    /** Ensure there are always 108 entries (used by readers). */
    private fun ensure108(existing: List<Any?>): List<Any?> =
        (0 until 108).map { i ->
            existing.getOrNull(i) ?: mapOf(
                "index" to (i + 1),
                "filled" to false,
                "value" to "",
                "lang" to "EN",
                "ts" to 0L
            )
        }
}
