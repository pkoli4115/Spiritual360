package com.hindu.pooja.feature.ramakoti.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.hindu.pooja.R
import com.hindu.pooja.feature.ramakoti.data.CertificateInput
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max

object PdfGenerator {

    data class CertificatePdf(val file: File, val certificateId: String)

    /**
     * Keep PDF page moderate.
     * Large page size + large embedded bitmap = large PDF.
     */
    private const val PAGE_W = 842
    private const val PAGE_H = 1191

    private const val SIDE_MARGIN = 36f
    private val HEADER_RES = R.drawable.ramakoti_certificate_bg

    @Volatile
    private var cachedHeaderBitmap: Bitmap? = null

    private fun getOrCreateHeaderBitmap(context: Context): Bitmap {
        cachedHeaderBitmap?.let { existing ->
            if (!existing.isRecycled) return existing
        }

        synchronized(this) {
            cachedHeaderBitmap?.let { existing ->
                if (!existing.isRecycled) return existing
            }

            val targetWidth = (PAGE_W - 2 * SIDE_MARGIN).toInt().coerceAtLeast(600)

            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeResource(context.resources, HEADER_RES, bounds)

            val srcWidth = max(bounds.outWidth, 1)
            var sample = 1
            while (srcWidth / (sample * 2) >= targetWidth) {
                sample *= 2
            }

            val bitmap = BitmapFactory.decodeResource(
                context.resources,
                HEADER_RES,
                BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.RGB_565
                    inDither = true
                }
            ) ?: throw IllegalArgumentException("Drawable $HEADER_RES not found")

            cachedHeaderBitmap = bitmap
            return bitmap
        }
    }
    private fun compressBitmapForPdf(bitmap: Bitmap): Bitmap {
        val maxWidth = 1000

        if (bitmap.width <= maxWidth) return bitmap

        val ratio = maxWidth.toFloat() / bitmap.width
        val newHeight = (bitmap.height * ratio).toInt()

        val scaled = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)

        return scaled
    }
    private fun Canvas.drawImageFitWidth(
        bitmap: Bitmap,
        left: Float,
        top: Float,
        maxWidth: Float
    ): RectF {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val scaledHeight = maxWidth / aspectRatio
        val dest = RectF(left, top, left + maxWidth, top + scaledHeight)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        drawBitmap(bitmap, null, dest, paint)
        return dest
    }

    fun createCertificatePdf(
        context: Context,
        input: CertificateInput
    ): CertificatePdf {
        val certificateId = UUID.randomUUID().toString().replace("-", "")

        val qrPayload = if (input.verificationUrl.isBlank()) {
            buildString {
                append("Hindu Pooja - Ramakoti Certificate\n")
                append("Devotee: ${input.devoteeName}\n")
                append("Milestone: ${input.countText}\n")
                append("Date: ${input.dateText}\n")
                append("CertificateId: $certificateId")
            }
        } else {
            input.verificationUrl
        }

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        canvas.drawColor(Color.parseColor("#F8EED8"))

        // Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C59C50")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val inset = 20f
        canvas.drawRect(inset, inset, PAGE_W - inset, PAGE_H - inset, borderPaint)

        val centerX = PAGE_W / 2f

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2A1F")
            textSize = 34f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3C3526")
            textSize = 24f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2A1F")
            textSize = 42f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val mantraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5A503E")
            textSize = 26f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }

        val smallPaintLeft = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6D6A5F")
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }

        // Title
        var y = 82f
        canvas.drawText("RAMANAMA COMPLETION CERTIFICATE", centerX, y, titlePaint)

        // Header image
        val imageTop = y + 28f
        val headerBitmap = compressBitmapForPdf(getOrCreateHeaderBitmap(context))
        val imageRect = canvas.drawImageFitWidth(
            bitmap = headerBitmap,
            left = SIDE_MARGIN,
            top = imageTop,
            maxWidth = PAGE_W - 2 * SIDE_MARGIN
        )

        // Main content
        y = imageRect.bottom + 46f
        canvas.drawText("This is to certify that", centerX, y, subPaint)

        y += 46f
        canvas.drawText(input.devoteeName.take(60), centerX, y, namePaint)

        y += 44f
        canvas.drawText(input.countText.take(80), centerX, y, subPaint)

        y += 40f
        canvas.drawText("Om Sri Ramaya Namaha", centerX, y, mantraPaint)

        // Footer
        val footerY1 = PAGE_H - 58f
        val footerY2 = PAGE_H - 34f
        canvas.drawText("Date: ${input.dateText}", inset + 8f, footerY1, smallPaintLeft)
        canvas.drawText("Certificate ID: $certificateId", inset + 8f, footerY2, smallPaintLeft)

        // Smaller QR = smaller PDF
        runCatching {
            val qr = QrCodeUtil.generate(qrPayload, sizePx = 160)
            val left = PAGE_W - inset - qr.width - 14f
            val top = PAGE_H - inset - qr.height - 14f
            canvas.drawBitmap(qr, left, top, null)
            qr.recycle()
        }

        pdf.finishPage(page)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val out = File(dir, "ramakoti_certificate_${certificateId}.pdf")

        FileOutputStream(out).use { fos ->
            pdf.writeTo(fos)
            fos.flush()
        }
        pdf.close()

        return CertificatePdf(out, certificateId)
    }
}