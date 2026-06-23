package com.example.eventpass.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.eventpass.data.repository.CheckInResult
import com.example.eventpass.util.DateFormatter
import java.util.concurrent.Executors

/**
 * QR scanner screen.
 *
 * Flow:
 * 1. Ask for camera permission (Activity Result API). Show rationale if denied.
 * 2. When granted, render a CameraX preview bound to an ML Kit analyzer.
 * 3. On a decoded QR, the ViewModel checks the attendee in and a result dialog
 *    appears (success / already checked in / not found).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Track whether we currently hold the camera permission.
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (hasCameraPermission) {
                // --- Live camera preview + QR analysis ---
                CameraPreview(
                    onQrDetected = viewModel::onQrCodeScanned,
                    modifier = Modifier.fillMaxSize()
                )

                // Helper hint at the bottom.
                Text(
                    text = "Point the camera at an attendee's QR code",
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                )
            } else {
                // --- Permission rationale / request UI ---
                CameraPermissionRequest(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Transient error (e.g. unreadable QR) shown as a small dialog.
            state.error?.let { message ->
                AlertDialog(
                    onDismissRequest = viewModel::dismissError,
                    confirmButton = {
                        TextButton(onClick = viewModel::dismissError) { Text("OK") }
                    },
                    title = { Text("Heads up") },
                    text = { Text(message) }
                )
            }

            // Result dialog for a completed check-in attempt.
            state.result?.let { result ->
                CheckInResultDialog(result = result, onDismiss = viewModel::dismissResult)
            }
        }
    }
}

/**
 * Embeds a CameraX [PreviewView] using [AndroidView] and binds a preview +
 * image-analysis use case to the current lifecycle. ML Kit decoding happens on
 * a dedicated single-thread executor.
 */
@Composable
private fun CameraPreview(
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // One background thread for ML Kit analysis.
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Preview use case -> render to the PreviewView surface.
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Analysis use case -> feed frames to our QR analyzer.
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(analysisExecutor, QrCodeAnalyzer(onQrDetected)) }

                try {
                    // Rebind cleanly to avoid duplicate use cases on recomposition.
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                } catch (e: Exception) {
                    // Binding can fail on devices without a back camera, etc.
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

/** Shown when camera permission has not been granted yet. */
@Composable
private fun CameraPermissionRequest(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.size(16.dp))
        Text(
            text = "Camera permission needed",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = "EventPass uses the camera to scan attendee QR codes for check-in.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant camera access")
        }
    }
}

/**
 * Result dialog summarising the outcome of a scan. The icon, title, and message
 * vary by the [CheckInResult] subtype.
 */
@Composable
private fun CheckInResultDialog(
    result: CheckInResult,
    onDismiss: () -> Unit
) {
    val (title, message) = when (result) {
        is CheckInResult.Success ->
            "✅ Checked in!" to "${result.attendee.name} (${result.attendee.ticketType}) is now checked in."

        is CheckInResult.AlreadyCheckedIn ->
            "⚠️ Already checked in" to
                "${result.attendee.name} was checked in at ${DateFormatter.format(result.attendee.checkInTime)}."

        is CheckInResult.NotFound ->
            "❌ Not found" to "No attendee matches id \"${result.scannedId}\"."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Scan next") }
        },
        title = { Text(title) },
        text = { Text(message) }
    )
}
