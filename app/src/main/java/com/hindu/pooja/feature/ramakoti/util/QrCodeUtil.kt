package com.hindu.pooja.feature.ramakoti.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.Px
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeUtil {

    /**
     * Generate a QR bitmap with sane defaults for printing and scanning.
     * @param content Non-blank data to encode
     * @param sizePx  Bitmap size (square)
     * @param margin  Quiet zone modules (1–4 typical)
     */
    @JvmStatic
    fun generate(
        content: String,
        @Px sizePx: Int = 512,
        margin: Int = 1
    ): Bitmap {
        require(content.isNotBlank()) { "QR content must not be blank" }

        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.MARGIN to margin,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )

        val matrix: BitMatrix = MultiFormatWriter()
            .encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
