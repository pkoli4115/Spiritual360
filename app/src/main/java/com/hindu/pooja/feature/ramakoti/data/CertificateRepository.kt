package com.hindu.pooja.feature.ramakoti.data

import android.content.Context
import android.graphics.Bitmap
import com.hindu.pooja.feature.ramakoti.util.PdfGenerator
import java.io.File

data class CertificateInput(
    val devoteeName: String,
    val countText: String,      // e.g., "Completed 1 Crore Sri Rama Namas"
    val dateText: String,       // "dd MMM yyyy"
    val language: String,       // "en" | "te" | "hi"
    val verificationUrl: String = "", // may be blank -> offline payload
    val templateBitmap: Bitmap? = null
)

data class CertificateResult(
    val localPdf: File,
    val certificateId: String
)

class CertificateRepository {

    /**
     * Generates the certificate PDF to app cache.
     * Upload is handled by RamakotiExportUploader (call that separately).
     */
    suspend fun generateAndOptionallyUpload(
        context: Context,
        uid: String,
        input: CertificateInput,
        uploadToStorage: Boolean = false // ignored on purpose; kept for API compatibility
    ): CertificateResult {
        // PdfGenerator already builds a non-empty payload if verificationUrl is blank
        val out = PdfGenerator.createCertificatePdf(context, input)
        return CertificateResult(localPdf = out.file, certificateId = out.certificateId)
    }
}
