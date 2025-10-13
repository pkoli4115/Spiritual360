package com.hindu.pooja.feature.ramakoti.util

import android.content.Context
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * PrintDocumentAdapter that prints an existing PDF File (API 24+ safe).
 */
class PdfPrintAdapter(
    private val context: Context,
    private val file: File
) : PrintDocumentAdapter() {

    private var inputPfd: ParcelFileDescriptor? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: android.os.Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder(file.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        try {
            inputPfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val inStream: InputStream = FileInputStream(inputPfd!!.fileDescriptor)
            val outStream: OutputStream = FileOutputStream(destination!!.fileDescriptor)

            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes: Int
            while (inStream.read(buffer).also { bytes = it } >= 0 && (cancellationSignal?.isCanceled != true)) {
                outStream.write(buffer, 0, bytes)
            }
            outStream.flush()

            if (cancellationSignal?.isCanceled == true) {
                callback?.onWriteCancelled()
            } else {
                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
        } finally {
            try { inputPfd?.close() } catch (_: Exception) {}
        }
    }

    override fun onFinish() {
        try { inputPfd?.close() } catch (_: Exception) {}
    }
}
