package com.example.salubris.ui.screens.pages

import android.Manifest
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
import com.example.salubris.utils.Vocabulary
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onBarcodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    when {
        cameraPermissionState.status.isGranted -> {
            BarcodeScannerView(onBarcodeScanned, onClose, modifier)
        }

        cameraPermissionState.status.shouldShowRationale -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(Vocabulary.get().cameraPermissionNeeded)
                    Button(onClick = {
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text(Vocabulary.get().grantPermission)
                    }
                    Button(onClick = onClose) {
                        Text(Vocabulary.get().cancel)
                    }
                }
            }
        }

        else -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Text(Vocabulary.get().cameraPermissionDenied)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onClose) {
                        Text(Vocabulary.get().close)
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isScanning by remember { mutableStateOf(true) }
    var cameraInitialized by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scannedBarcodes by remember { mutableStateOf<List<String>>(emptyList()) }

    val previewView = remember { mutableStateOf<PreviewView?>(null) }

    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        val cameraExecutor = Executors.newSingleThreadExecutor()
        var cameraProvider: ProcessCameraProvider? = null
        var imageAnalysis: ImageAnalysis? = null
        var preview: Preview? = null
        var lastScanTime = 0L
        val debounceTime = 1000L

        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()

                    preview = Preview.Builder()
                        .build()

                    previewView.value?.let { view ->
                        preview?.setSurfaceProvider(view.surfaceProvider)
                    }

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
                                                    val isValidBarcode =
                                                        rawValue.matches(Regex("^[0-9]+$")) && rawValue.length >= 8

                                                    if (isValidBarcode) {
                                                        lastScanTime = currentTime
                                                        scannedBarcodes = scannedBarcodes + rawValue

                                                        if (scannedBarcodes.size >= 3) {
                                                            val barcodeCounts =
                                                                scannedBarcodes.groupingBy { it }
                                                                    .eachCount()
                                                            val mostCommonBarcode =
                                                                barcodeCounts.maxByOrNull { it.value }?.key

                                                            if (mostCommonBarcode != null && barcodeCounts[mostCommonBarcode]!! >= 2) {
                                                                isScanning = false
                                                                onBarcodeScanned(mostCommonBarcode)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            // ignore
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            } catch (e: Exception) {
                                imageProxy.close()
                            }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider?.unbindAll()
                        cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        cameraInitialized = true
                    } catch (e: Exception) {
                        errorMessage = "${Vocabulary.get().errorPrefix} ${e.message}"
                    }

                } catch (e: Exception) {
                    errorMessage = "${Vocabulary.get().errorPrefix} ${e.message}"
                }
            }, ContextCompat.getMainExecutor(context))

        } catch (e: Exception) {
            errorMessage = "${Vocabulary.get().errorPrefix} ${e.message}"
        }

        onDispose {
            isScanning = false
            cameraExecutor.shutdown()
            barcodeScanner.close()
            cameraProvider?.unbindAll()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView.value = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 8.dp)
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
        ) {
            Text("✕", color = Color.White, fontSize = MaterialTheme.typography.titleLarge.fontSize)
        }

        if (scannedBarcodes.isNotEmpty() && isScanning) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color.Black.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = String.format(Vocabulary.get().scanningProgress, scannedBarcodes.size),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

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
                    Text(Vocabulary.get().startingCamera, color = Color.White)
                }
            }
        }

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
                        text = errorMessage ?: Vocabulary.get().unknownError,
                        color = Color.White
                    )
                    Button(onClick = {
                        errorMessage = null
                        cameraInitialized = false
                    }) {
                        Text(Vocabulary.get().retry)
                    }
                    Button(onClick = onClose) {
                        Text(Vocabulary.get().close)
                    }
                }
            }
        } else if (cameraInitialized && errorMessage == null && isScanning) {
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
                        text = Vocabulary.get().positionBarcode,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}