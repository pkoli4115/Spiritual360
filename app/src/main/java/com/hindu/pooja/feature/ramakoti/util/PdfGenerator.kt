package com.hindu.pooja.feature.ramakoti.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.hindu.pooja.R
import com.hindu.pooja.feature.ramakoti.data.CertificateInput
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PdfGenerator {

    data class CertificatePdf(val file: File, val certificateId: String)

    private const val PAGE_W = 1240
    private const val PAGE_H = 1754
    private val HEADER_RES = R.drawable.ramakoti_certificate_bg

    private fun decodeBitmap(context: Context, resId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, resId)
            ?: throw IllegalArgumentException("Drawable $resId not found")
    }

    /** Draw bitmap scaled to fit width, preserving aspect ratio. */
    private fun Canvas.drawImageFitWidth(
        bitmap: Bitmap,
        left: Float,
        top: Float,
        maxWidth: Float
    ): RectF {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height
        val scaledHeight = maxWidth / aspectRatio
        val dest = RectF(left, top, left + maxWidth, top + scaledHeight)
        drawBitmap(bitmap, null, dest, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        return dest
    }

    fun createCertificatePdf(
        context: Context,
        input: CertificateInput
    ): CertificatePdf {
        val certificateId = UUID.randomUUID().toString().replace("-", "")
        val qrPayload = if (input.verificationUrl.isBlank()) {
            buildString {
                append("Hindu Pooja – Ramakoti Certificate\n")
                append("Devotee: ${input.devoteeName}\n")
                append("Milestone: ${input.countText}\n")
                append("Date: ${input.dateText}\n")
                append("CertificateId: $certificateId")
            }
        } else input.verificationUrl

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page = pdf.startPage(pageInfo)
        val c = page.canvas

        // Background
        c.drawColor(Color.parseColor("#F8EED8"))

        // Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C59C50")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        val inset = 24f
        c.drawRect(inset, inset, PAGE_W - inset, PAGE_H - inset, borderPaint)

        // Title at top
        val centerX = PAGE_W / 2f
        val titleY = inset + 80f
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2A1F")
            textSize = 56f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        c.drawText("RAMAKOTI COMPLETION CERTIFICATE", centerX, titleY, titlePaint)

        // Image below title
        val imageTop = titleY + 40f
        val bmp = decodeBitmap(context, HEADER_RES)
        val imageRect = c.drawImageFitWidth(bmp, inset, imageTop, PAGE_W - 2 * inset)
        bmp.recycle()

        // Text styles
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3C3526")
            textSize = 36f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2A1F")
            textSize = 70f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val mantraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5A503E")
            textSize = 40f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6D6A5F")
            textSize = 26f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }

        // Certificate content block
        var y = imageRect.bottom + 80f
        c.drawText("This is to certify that", centerX, y, subPaint)
        y += 66f
        c.drawText(input.devoteeName, centerX, y, namePaint)
        y += 62f
        c.drawText(input.countText, centerX, y, subPaint)
        y += 56f
        c.drawText("Om Sri Ramaya Namaha", centerX, y, mantraPaint)

        // Footer
        val footerY1 = PAGE_H - inset - 60f
        val footerY2 = PAGE_H - inset - 28f
        c.drawText("Date: ${input.dateText}", inset + 12f, footerY1, smallPaint)
        c.drawText("Certificate ID: $certificateId", inset + 12f, footerY2, smallPaint)

        // QR code
        runCatching {
            val qr = QrCodeUtil.generate(qrPayload, sizePx = 360)
            val left = PAGE_W - inset - 20f - qr.width
            val top = PAGE_H - inset - 24f - qr.height
            c.drawBitmap(qr, left, top, null)
            qr.recycle()
        }

        pdf.finishPage(page)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val out = File(dir, "ramakoti_certificate_${System.currentTimeMillis()}.pdf")
        FileOutputStream(out).use { pdf.writeTo(it) }
        pdf.close()

        return CertificatePdf(out, certificateId)
    }
}
