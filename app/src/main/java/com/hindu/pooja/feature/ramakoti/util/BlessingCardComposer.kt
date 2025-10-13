package com.hindu.pooja.feature.ramakoti.util

import android.content.Context
import android.graphics.*
import java.io.File
import java.io.FileOutputStream

object BlessingCardComposer {

    data class Input(
        val devoteeName: String,
        val message: String,  // e.g., "I completed 1 Crore Sri Rama Namas"
        val dateText: String,
        val language: String,
        val qrUrl: String,
        val width: Int = 1080,
        val height: Int = 1350
    )

    fun createCard(context: Context, input: Input): File {
        val bmp = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // Background gradient
        val bg = Paint().apply {
            shader = LinearGradient(
                0f, 0f, input.width.toFloat(), input.height.toFloat(),
                Color.parseColor("#FFFDE7"),
                Color.parseColor("#FFE082"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, input.width.toFloat(), input.height.toFloat(), bg)

        val title = LocaleText.certificateCopy(input.language).footer // "Jai Sri Ram" or localized
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3E2723")
            textSize = 80f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(title, (input.width / 2).toFloat(), 160f, titlePaint)

        val msgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4E342E")
            textSize = 56f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(input.message, (input.width / 2).toFloat(), 340f, msgPaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 64f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(input.devoteeName, (input.width / 2).toFloat(), 440f, namePaint)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(input.dateText, (input.width / 2).toFloat(), 520f, datePaint)

        // QR
        val qr = QrCodeUtil.generate(input.qrUrl, sizePx = 480)
        val left = (input.width - qr.width) / 2f
        canvas.drawBitmap(qr, left, 620f, null)

        // Footer
        val f = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6D4C41")
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Share your blessings", (input.width / 2).toFloat(), (input.height - 80).toFloat(), f)

        // Save
        val dir = File(context.cacheDir, "blessings").apply { mkdirs() }
        val out = File(dir, "blessing_${System.currentTimeMillis()}.jpg")
        FileOutputStream(out).use {
            bmp.compress(Bitmap.CompressFormat.JPEG, 92, it)
        }
        bmp.recycle()
        return out
    }
}
