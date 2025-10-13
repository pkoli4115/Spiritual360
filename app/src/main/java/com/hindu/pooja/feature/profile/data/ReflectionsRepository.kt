package com.hindu.pooja.feature.profile.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReflectionsRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    data class Reflection(
        val id: String,
        val text: String,
        val createdAtMs: Long?
    )

    private fun uid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("Not signed in")

    private fun col() = db.collection("users")
        .document(uid())
        .collection("reflections")

    suspend fun list(limit: Long = 100): List<Reflection> {
        val snap = col()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        return snap.documents.map { d ->
            Reflection(
                id = d.id,
                text = d.getString("text") ?: "",
                createdAtMs = d.getTimestamp("createdAt")?.toDate()?.time
            )
        }
    }

    suspend fun add(text: String) {
        val data = hashMapOf(
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp()
        )
        col().add(data).await()
    }

    suspend fun delete(id: String) {
        col().document(id).delete().await()
    }
}
