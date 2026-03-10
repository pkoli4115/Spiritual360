package com.hindu.pooja.feature.ramakoti.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.hindu.pooja.R
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object RamakotiPdfGenerator {

    data class GridInput(
        val languageCode: String,
        val pageTitle: String,
        val lifetimeCount: Int,   // 🔸 total lifetime counts
        val currentBatchCount: Int // (optional UI display; we fill using lifetime)
    )

    private const val PAGE_W = 1240
    private const val PAGE_H = 1754

    private fun fadedGrayscale(context: Context, resId: Int): Bitmap {
        val src = BitmapFactory.decodeResource(context.resources, resId)
        val out = Bitmap.createBitmap(PAGE_W, PAGE_H, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        val cm = ColorMatrix().apply { setSaturation(0f) }
        val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
            alpha = 24 // very light watermark
        }
        val srcR = Rect(0, 0, src.width, src.height)
        val dstR = Rect(0, 0, PAGE_W, PAGE_H)
        c.drawBitmap(src, srcR, dstR, p)
        src.recycle()
        return out
    }

    fun generateGridPdf(context: Context, input: GridInput): File {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page = pdf.startPage(pageInfo)
        val c = page.canvas

        // Watermark background
        runCatching {
            val wm = fadedGrayscale(context, R.drawable.ramakoti_certificate_bg)
            c.drawBitmap(wm, 0f, 0f, null)
            wm.recycle()
        }

        // Title
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2A1F")
            textSize = 46f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        c.drawText(input.pageTitle, PAGE_W / 2f, 90f, titlePaint)

        // Grid geometry
        val cols = 9
        val rows = 12
        val marginH = 60f
        val marginV = 140f
        val spacing = 8f
        val availW = PAGE_W - marginH * 2
        val availH = PAGE_H - marginV * 2
        val cell = min(
            (availW - (cols - 1) * spacing) / cols,
            (availH - (rows - 1) * spacing) / rows
        )

        // Fill count from lifetime: show *all completed up to 108*.
        var filled = input.lifetimeCount % 108
        if (input.lifetimeCount > 0 && filled == 0) filled = 108
        filled = min(108, filled)

        val boxPaint = Paint().apply { color = Color.argb(32, 0, 0, 0) }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3A2A00")
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = cell * 0.22f // auto-scale
        }

        // Draw 9×12 grid; write "Jai Sri Ram" in filled cells
        var idx = 0
        for (r in 0 until rows) {
            for (cidx in 0 until cols) {
                val left = marginH + cidx * (cell + spacing)
                val top = marginV + r * (cell + spacing)
                val rect = RectF(left, top, left + cell, top + cell)

                // light box background
                c.drawRoundRect(rect, 8f, 8f, boxPaint)

                if (idx < filled) {
                    val cx = rect.centerX()
                    // Vertically center the baseline: y = centerY + (textSize/3) ≈ good baseline compensation
                    val cy = rect.centerY() + textPaint.textSize / 3f
                    c.drawText("Jai Sri Ram", cx, cy, textPaint)
                }
                idx++
            }
        }

        pdf.finishPage(page)

        val outDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val out = File(outDir, "ramakoti_grid_${System.currentTimeMillis()}.pdf")
        FileOutputStream(out).use { pdf.writeTo(it) }
        pdf.close()
        return out
    }
}
