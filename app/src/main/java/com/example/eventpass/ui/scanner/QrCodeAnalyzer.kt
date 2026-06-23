package com.example.eventpass.ui.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * A CameraX [ImageAnalysis.Analyzer] that runs each camera frame through ML Kit
 * barcode scanning. When a QR code is decoded, its raw value is delivered to
 * [onQrDetected].
 *
 * The analyzer always closes the [ImageProxy] when finished so the camera
 * pipeline can deliver the next frame.
 */
class QrCodeAnalyzer(
    private val onQrDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Configure ML Kit to look specifically for QR codes for efficiency.
    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                // Take the first QR code that has a usable raw value.
                barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                    ?.rawValue
                    ?.let(onQrDetected)
            }
            .addOnCompleteListener {
                // Always close so the next frame can be analyzed.
                imageProxy.close()
            }
    }
}
