package com.example.salubris.ui.screens.pages

import android.Manifest
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private const val TAG = "BarcodeScanner"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onBarcodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "CameraScreen: Composable created")
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        Log.d(TAG, "CameraScreen: Requesting camera permission")
        cameraPermissionState.launchPermissionRequest()
    }

    when {
        cameraPermissionState.status.isGranted -> {
            Log.d(TAG, "CameraScreen: Camera permission granted")
            BarcodeScannerView(onBarcodeScanned, onClose, modifier)
        }
        cameraPermissionState.status.shouldShowRationale -> {
            Log.d(TAG, "CameraScreen: Showing permission rationale")
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Camera permission is needed to scan barcodes")
                    Button(onClick = {
                        Log.d(TAG, "CameraScreen: User clicked grant permission button")
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text("Grant Permission")
                    }
                    Button(onClick = onClose) {
                        Text("Cancel")
                    }
                }
            }
        }
        else -> {
            Log.e(TAG, "CameraScreen: Camera permission denied")
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Text("Camera permission denied")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onClose) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScannerView(
    onBarcodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "BarcodeScannerView: Composable created")
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isScanning by remember { mutableStateOf(true) }
    var cameraInitialized by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scannedBarcodes by remember { mutableStateOf<List<String>>(emptyList()) }

    // Create a reference to the PreviewView
    val previewView = remember { mutableStateOf<PreviewView?>(null) }

    val barcodeScanner = remember {
        Log.d(TAG, "BarcodeScannerView: Initializing ML Kit barcode scanner")
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        Log.d(TAG, "BarcodeScannerView: DisposableEffect started")
        val cameraExecutor = Executors.newSingleThreadExecutor()
        var cameraProvider: ProcessCameraProvider? = null
        var imageAnalysis: ImageAnalysis? = null
        var preview: Preview? = null
        var lastScanTime = 0L
        val debounceTime = 1000L // 1 second debounce to avoid duplicate scans

        try {
            Log.d(TAG, "BarcodeScannerView: Getting camera provider instance")
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                try {
                    Log.d(TAG, "BarcodeScannerView: Camera provider future completed")
                    cameraProvider = cameraProviderFuture.get()
                    Log.d(TAG, "BarcodeScannerView: Camera provider obtained: ${cameraProvider != null}")

                    // Set up Preview
                    Log.d(TAG, "BarcodeScannerView: Building preview")
                    preview = Preview.Builder()
                        .build()

                    // Connect preview to the PreviewView
                    previewView.value?.let { view ->
                        Log.d(TAG, "BarcodeScannerView: Setting preview surface provider")
                        preview?.setSurfaceProvider(view.surfaceProvider)
                    } ?: run {
                        Log.w(TAG, "BarcodeScannerView: PreviewView not yet available")
                    }

                    // Set up ImageAnalysis
                    Log.d(TAG, "BarcodeScannerView: Building image analysis")
                    imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(android.util.Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis?.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (isScanning) {
                            try {
                                val currentTime = System.currentTimeMillis()
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && (currentTime - lastScanTime) > debounceTime) {
                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )

                                    barcodeScanner.process(inputImage)
                                        .addOnSuccessListener { barcodes ->
                                            barcodes.forEach { barcode ->
                                                val rawValue = barcode.rawValue
                                                if (!rawValue.isNullOrBlank()) {
                                                    Log.d(TAG, "BarcodeScannerView: Barcode detected: $rawValue")

                                                    // Validate barcode (check if it's a valid number)
                                                    val isValidBarcode = rawValue.matches(Regex("^[0-9]+$")) && rawValue.length >= 8

                                                    if (isValidBarcode) {
                                                        lastScanTime = currentTime
                                                        scannedBarcodes = scannedBarcodes + rawValue

                                                        // If we have multiple scans, check consistency
                                                        if (scannedBarcodes.size >= 3) {
                                                            // Find the most common barcode
                                                            val barcodeCounts = scannedBarcodes.groupingBy { it }.eachCount()
                                                            val mostCommonBarcode = barcodeCounts.maxByOrNull { it.value }?.key

                                                            if (mostCommonBarcode != null && barcodeCounts[mostCommonBarcode]!! >= 2) {
                                                                // We have a consistent barcode after multiple scans
                                                                isScanning = false
                                                                Log.d(TAG, "BarcodeScannerView: Valid barcode confirmed: $mostCommonBarcode")
                                                                onBarcodeScanned(mostCommonBarcode)
                                                            }
                                                        }
                                                    } else {
                                                        Log.d(TAG, "BarcodeScannerView: Invalid barcode format: $rawValue")
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "BarcodeScannerView: Barcode scanning failed", e)
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "BarcodeScannerView: Error processing image", e)
                                imageProxy.close()
                            }
                        } else {
                            imageProxy.close()
                        }
                    }

                    // Select back camera
                    Log.d(TAG, "BarcodeScannerView: Selecting back camera")
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // Bind lifecycle
                    try {
                        Log.d(TAG, "BarcodeScannerView: Binding camera to lifecycle")
                        cameraProvider?.unbindAll()
                        cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        cameraInitialized = true
                        Log.d(TAG, "BarcodeScannerView: Camera successfully initialized and bound")
                    } catch (e: Exception) {
                        Log.e(TAG, "BarcodeScannerView: Failed to bind camera to lifecycle", e)
                        errorMessage = "Failed to start camera: ${e.message}"
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "BarcodeScannerView: Error in camera provider listener", e)
                    errorMessage = "Camera initialization failed: ${e.message}"
                }
            }, ContextCompat.getMainExecutor(context))

        } catch (e: Exception) {
            Log.e(TAG, "BarcodeScannerView: Failed to get camera provider instance", e)
            errorMessage = "Failed to get camera provider: ${e.message}"
        }

        onDispose {
            Log.d(TAG, "BarcodeScannerView: Disposing resources")
            isScanning = false
            cameraExecutor.shutdown()
            barcodeScanner.close()
            cameraProvider?.unbindAll()
            Log.d(TAG, "BarcodeScannerView: Resources disposed")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview - Full screen
        AndroidView(
            factory = { ctx ->
                Log.d(TAG, "BarcodeScannerView: Creating PreviewView")
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER

                    // Store reference to PreviewView
                    previewView.value = this

                    Log.d(TAG, "BarcodeScannerView: PreviewView created with scaleType FILL_CENTER")
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close button overlay
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
        ) {
            Text("✕", color = Color.White, fontSize = MaterialTheme.typography.titleLarge.fontSize)
        }

        // Scanning status overlay
        if (scannedBarcodes.isNotEmpty() && isScanning) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color.Black.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Scanning... (${scannedBarcodes.size}/3)",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Show loading while camera initializes
        if (!cameraInitialized && errorMessage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Starting camera...", color = Color.White)
                }
            }
        }

        // Show error if any
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error: ${errorMessage ?: "Unknown error"}",
                        color = Color.White
                    )
                    Button(onClick = {
                        Log.d(TAG, "BarcodeScannerView: User clicked retry")
                        errorMessage = null
                        cameraInitialized = false
                    }) {
                        Text("Retry")
                    }
                    Button(onClick = onClose) {
                        Text("Close")
                    }
                }
            }
        } else if (cameraInitialized && errorMessage == null && isScanning) {
            // Scanning overlay with frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .border(3.dp, Color.White, shape = MaterialTheme.shapes.medium)
                    )

                    Text(
                        text = "Position barcode inside the frame\nScanning multiple times for accuracy...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}