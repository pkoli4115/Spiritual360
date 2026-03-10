package com.hindu.pooja.feature.ramakoti.data

import android.content.Context
import com.hindu.pooja.feature.ramakoti.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class CertificateInput(
    val devoteeName: String,
    val countText: String,
    val dateText: String,
    val language: String,
    val verificationUrl: String = "",
    val templateBitmap: android.graphics.Bitmap? = null
)

data class CertificateResult(
    val localPdf: File,
    val certificateId: String
)

class CertificateRepository {

    /**
     * Heavy work must happen on IO to avoid UI freeze.
     */
    suspend fun generateAndOptionallyUpload(
        context: Context,
        uid: String,
        input: CertificateInput,
        uploadToStorage: Boolean = false
    ): CertificateResult = withContext(Dispatchers.IO) {
        val out = PdfGenerator.createCertificatePdf(context, input)
        CertificateResult(
            localPdf = out.file,
            certificateId = out.certificateId
        )
    }
}