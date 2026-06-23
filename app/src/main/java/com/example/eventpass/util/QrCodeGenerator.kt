package com.example.eventpass.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Generates QR code bitmaps using ZXing. Used by the "Add attendee" screen to
 * show a scannable code for the newly created attendee.
 */
object QrCodeGenerator {

    /**
     * Encodes [content] into a square QR [Bitmap] of the given [size] (pixels).
     *
     * @return the generated bitmap, or null if encoding failed.
     */
    fun generate(content: String, size: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /** Same as [generate] but returns a Compose [ImageBitmap] ready to draw. */
    fun generateImageBitmap(content: String, size: Int = 512): ImageBitmap? {
        return generate(content, size)?.asImageBitmap()
    }
}
