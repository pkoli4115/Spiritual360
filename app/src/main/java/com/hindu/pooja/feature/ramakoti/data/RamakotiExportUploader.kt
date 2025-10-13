package com.hindu.pooja.feature.ramakoti.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class RamakotiExportUploader {

    enum class ExportType { CERTIFICATE, PDF_GRID, BLESSING_CARD }

    companion object {

        /**
         * Uploads a local file (PDF/PNG, etc.) to:
         *   ramakoti/{uid}/exports/{yyyy}/{MM}/{dd}/{fileName}
         * and records a metadata doc at:
         *   ramakotiExports/{uid}/files/{autoId}
         *
         * Returns the download URL string.
         */
        suspend fun uploadAndRecord(
            auth: FirebaseAuth,
            storage: FirebaseStorage,
            db: FirebaseFirestore,
            localFileUri: Uri,
            fileName: String,
            type: ExportType,
            extraMeta: Map<String, Any?> = emptyMap()
        ): String {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")

            // yyyy/MM/dd directories
            val now = Date()
            val yyyy = SimpleDateFormat("yyyy", Locale.US).format(now)
            val MM = SimpleDateFormat("MM", Locale.US).format(now)
            val dd = SimpleDateFormat("dd", Locale.US).format(now)

            val path = "ramakoti/$uid/exports/$yyyy/$MM/$dd/$fileName" // ✅ matches storage.rules
            val ref = storage.reference.child(path)

            // Upload (contentType set for PDFs; image types can be inferred by name if needed)
            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType(when {
                    fileName.endsWith(".pdf", true) -> "application/pdf"
                    fileName.endsWith(".png", true) -> "image/png"
                    fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
                    else -> "application/octet-stream"
                })
                .build()

            ref.putFile(localFileUri, metadata).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            // Record Firestore metadata
            val meta = mutableMapOf<String, Any?>(
                "filename" to fileName,
                "downloadUrl" to downloadUrl,
                "type" to type.name,
                "createdAt" to FieldValue.serverTimestamp()
            )
            // add optional fields like certificateId
            extraMeta.forEach { (k, v) -> if (v != null) meta[k] = v }

            db.collection("ramakotiExports").document(uid)
                .collection("files")
                .add(meta)
                .await()

            return downloadUrl
        }
    }
}
