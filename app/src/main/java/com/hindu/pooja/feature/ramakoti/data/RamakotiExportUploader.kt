package com.hindu.pooja.feature.ramakoti.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RamakotiExportUploader {

    enum class ExportType { CERTIFICATE, PDF_GRID, BLESSING_CARD }

    companion object {
        private const val TAG = "RamakotiUpload"

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

            val now = Date()
            val yyyy = SimpleDateFormat("yyyy", Locale.US).format(now)
            val MM = SimpleDateFormat("MM", Locale.US).format(now)
            val dd = SimpleDateFormat("dd", Locale.US).format(now)

            val safeFileName = fileName.replace("\\s+".toRegex(), "_")
            val path = "ramakoti/$uid/exports/$yyyy/$MM/$dd/$safeFileName"
            val ref = storage.reference.child(path)

            val metadata = StorageMetadata.Builder()
                .setContentType(
                    when {
                        safeFileName.endsWith(".pdf", true) -> "application/pdf"
                        safeFileName.endsWith(".png", true) -> "image/png"
                        safeFileName.endsWith(".jpg", true) || safeFileName.endsWith(".jpeg", true) -> "image/jpeg"
                        else -> "application/octet-stream"
                    }
                )
                .setCacheControl("public,max-age=3600")
                .build()

            val start = System.currentTimeMillis()
            Log.d(TAG, "Upload start: file=$safeFileName path=$path uri=$localFileUri")

            var lastError: Throwable? = null
            repeat(3) { attempt ->
                try {
                    ref.putFile(localFileUri, metadata).await()
                    val downloadUrl = ref.downloadUrl.await().toString()

                    val meta = mutableMapOf<String, Any?>(
                        "filename" to safeFileName,
                        "storagePath" to path,
                        "downloadUrl" to downloadUrl,
                        "type" to type.name,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    extraMeta.forEach { (k, v) ->
                        if (v != null) meta[k] = v
                    }

                    db.collection("ramakotiExports")
                        .document(uid)
                        .collection("files")
                        .add(meta)
                        .await()

                    Log.d(TAG, "Upload success in ${System.currentTimeMillis() - start} ms")
                    return downloadUrl
                } catch (t: Throwable) {
                    lastError = t
                    Log.e(TAG, "Upload attempt ${attempt + 1} failed", t)
                    if (attempt < 2) delay(1200L * (attempt + 1))
                }
            }

            throw lastError ?: IllegalStateException("Upload failed")
        }
    }
}