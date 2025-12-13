package com.mobile.memorise.ui.screen.create.ai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.TimeUnit

// --- CONSTANTS ---
private val PrimaryBlue = Color(0xFF536DFE)

@Composable
fun CameraCaptureScreen(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    // State Permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher Permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    // Cek otomatis saat pertama kali dibuka
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraContent(
            onImageCaptured = onImageCaptured,
            onClose = onClose
        )
    } else {
        PermissionDeniedContent(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onOpenSettings = { openAppSettings(context) },
            onClose = onClose
        )
    }
}

// --- UI SAAT PERMISSION DITOLAK ---
@Composable
fun PermissionDeniedContent(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Camera",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Camera Access Needed",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We need camera access to capture documents. Please grant permission to continue.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Grant Permission", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onOpenSettings) {
                Text("Open Settings", color = Color.White)
            }
        }
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

// ==========================================
// CAMERA CONTENT & FOCUS LOGIC
// ==========================================

@Composable
fun CameraContent(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current

    // Setup Controller Kamera
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            isTapToFocusEnabled = false // Kita handle manual
        }
    }

    // State Data
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    // --- FOCUS STATE ---
    var focusOffset by remember { mutableStateOf(Offset.Zero) }
    var showFocusRing by remember { mutableStateOf(false) }

    // --- TAMPILAN UTAMA ---
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().background(Color.Black)) {

        if (capturedUri != null) {
            ImagePreviewScreen(
                imageUri = capturedUri!!,
                onTryAgain = { capturedUri = null },
                onDone = { onImageCaptured(capturedUri!!) }
            )
        } else {
            // 1. CAMERA PREVIEW LAYER
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // 1. Update UI Animasi
                            focusOffset = offset
                            showFocusRing = true

                            // 2. Logic Fokus CameraX
                            val factory = SurfaceOrientedMeteringPointFactory(
                                size.width.toFloat(), size.height.toFloat()
                            )
                            val point = factory.createPoint(offset.x, offset.y)
                            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                                .setAutoCancelDuration(2, TimeUnit.SECONDS)
                                .build()

                            try {
                                // PERBAIKAN 1: Gunakan safe call (?.)
                                controller.cameraControl?.startFocusAndMetering(action)
                            } catch (e: Exception) {
                                Log.e("CameraFocus", "Focus failed", e)
                            }
                        }
                    }
            ) {
                AndroidView(
                    factory = {
                        PreviewView(it).apply {
                            this.controller = controller
                            controller.bindToLifecycle(lifecycleOwner)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Render Focus Animation Ring
                if (showFocusRing) {
                    FocusRing(
                        offset = focusOffset,
                        onAnimationEnd = { showFocusRing = false }
                    )
                }
            }

            // 2. OVERLAY GRADIENT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )

            // 3. UI CONTROLS LAYER
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Top Bar ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            controller.enableTorch(isFlashOn)
                        },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isFlashOn) Color(0xFFFFD600) else Color.White
                        )
                    }
                }

                // --- Bottom Bar ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tap to focus • Center document",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ShutterButton {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        takePhoto(
                            context = context,
                            controller = controller,
                            onPhotoTaken = { uri ->
                                capturedUri = uri
                                controller.enableTorch(false)
                                isFlashOn = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// ==========================================
// ANIMASI & COMPONENT CANTIK
// ==========================================

@Composable
fun FocusRing(
    offset: Offset,
    onAnimationEnd: () -> Unit
) {
    val scaleAnim = remember { Animatable(1.5f) }
    val alphaAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
        delay(500)
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 300)
        )
        onAnimationEnd()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = 40.dp.toPx()
        val strokeWidth = 2.dp.toPx()
        val topLeft = Offset(offset.x, offset.y)

        drawCircle(
            color = Color.White.copy(alpha = alphaAnim.value),
            radius = radius * scaleAnim.value,
            center = topLeft,
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun ShutterButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        label = "ShutterScale"
    )

    Box(
        modifier = Modifier
            .size(84.dp)
            .scale(scale)
            .border(4.dp, Color.White, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(Color.White)
            // PERBAIKAN 2: Penulisan clickable yang eksplisit
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Tidak ada ripple
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
        )
    }
}

// ==========================================
// PREVIEW & HELPER
// ==========================================

@Composable
fun ImagePreviewScreen(
    imageUri: Uri,
    onTryAgain: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E1E1E))
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onTryAgain,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Text("Retake", style = MaterialTheme.typography.titleMedium)
            }

            Button(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Use Photo", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    controller: LifecycleCameraController,
    onPhotoTaken: (Uri) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val bitmap = image.toBitmap()
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )

                val uri = saveBitmapToCache(context, rotatedBitmap)
                image.close()
                onPhotoTaken(uri)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Error taking photo", exception)
                Toast.makeText(context, "Failed to take photo", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val imagesDir = File(context.cacheDir, "images")
    if (!imagesDir.exists()) imagesDir.mkdirs()

    val file = File(imagesDir, "capture_${System.currentTimeMillis()}.jpg")
    val outputStream = file.outputStream()

    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    outputStream.flush()
    outputStream.close()

    return Uri.fromFile(file)
}