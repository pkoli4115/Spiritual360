package com.hindu.pooja.feature.ramakoti.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.core.content.FileProvider
import java.io.File

/**
 * Centralized open/share/print helpers for PDFs stored in app cache.
 * Manifest must declare:
 *   <provider
 *     android:name="androidx.core.content.FileProvider"
 *     android:authorities="${applicationId}.fileprovider"
 *     android:exported="false"
 *     android:grantUriPermissions="true">
 *     <meta-data android:name="android.support.FILE_PROVIDER_PATHS"
 *                android:resource="@xml/file_paths"/>
 *   </provider>
 */
object ShareHelper {

    private fun authority(context: Context) = "${context.packageName}.fileprovider"

    private fun fileUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, authority(context), file)

    fun openPdf(context: Context, file: File) {
        val uri = fileUri(context, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (_: ActivityNotFoundException) {
            // No PDF viewer installed; fallback to share
            sharePdf(context, file)
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri = fileUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    fun printPdf(context: Context, file: File, jobName: String = file.name) {
        val pm = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter: PrintDocumentAdapter = PdfPrintAdapter(context, file)
        pm.print(
            jobName,
            adapter,
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()
        )
    }
}
