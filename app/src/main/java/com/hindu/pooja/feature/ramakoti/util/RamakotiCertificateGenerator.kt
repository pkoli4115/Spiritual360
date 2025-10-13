package com.hindu.pooja.feature.ramakoti.util

import android.content.Context
import com.hindu.pooja.feature.ramakoti.data.CertificateInput
import java.io.File

object RamakotiCertificateGenerator {
    data class Result(val pdf: File, val certificateId: String)
    fun generate(context: Context, input: CertificateInput): Result {
        val out = PdfGenerator.createCertificatePdf(context, input)
        return Result(out.file, out.certificateId)
    }
}
