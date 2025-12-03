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
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

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
        // Jika diizinkan -> Tampilkan Kamera
        CameraContent(
            onImageCaptured = onImageCaptured,
            onClose = onClose
        )
    } else {
        // Jika ditolak -> Tampilkan UI Minta Izin
        PermissionDeniedContent(
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onOpenSettings = {
                openAppSettings(context)
            },
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
        // Tombol Close di pojok kiri atas
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
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Camera Permission Required",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We need camera access to scan your documents. Please grant the permission to continue.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Grant Permission
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF536DFE))
            ) {
                Text("Grant Permission", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Open Settings (Jaga-jaga jika user pilih "Don't ask again")
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings", color = Color.White)
            }
        }
    }
}

// --- FUNGSI HELPER MEMBUKA SETTINGS HP ---
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

// ==========================================
// KODE DI BAWAH INI TETAP SAMA SEPERTI SEBELUMNYA
// (CameraContent, ImagePreviewScreen, takePhoto, saveBitmapToCache)
// ==========================================

@Composable
fun CameraContent(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Setup Controller Kamera
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    // State Data
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    // --- TAMPILAN UTAMA ---
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding().background(Color.Black)) {

        if (capturedUri != null) {
            // 2. PREVIEW IMAGE SCREEN (Setelah Foto)
            ImagePreviewScreen(
                imageUri = capturedUri!!,
                onTryAgain = { capturedUri = null }, // Reset URI untuk foto ulang
                onDone = { onImageCaptured(capturedUri!!) } // Kirim hasil
            )
        } else {
            // 1. CAMERA VIEW SCREEN

            // Preview Kamera (AndroidView)
            AndroidView(
                factory = {
                    PreviewView(it).apply {
                        this.controller = controller
                        controller.bindToLifecycle(lifecycleOwner)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay UI (Tombol-tombol di atas kamera)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Top Bar (Close & Flash) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Close
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    // Tombol Flash
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            controller.enableTorch(isFlashOn) // Menyalakan lampu senter
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isFlashOn) Color.Yellow else Color.White
                        )
                    }
                }

                // --- Bottom Bar (Shutter) ---
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Shutter Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                takePhoto(
                                    context = context,
                                    controller = controller,
                                    onPhotoTaken = { uri ->
                                        capturedUri = uri // Pindah ke mode preview
                                        controller.enableTorch(false) // Matikan flash otomatis
                                        isFlashOn = false
                                    }
                                )
                            }
                    )
                }
            }

            // Hint Text
            Box(
                modifier = Modifier.fillMaxSize().padding(bottom = 120.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Tap screen to focus • Center document",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

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
        // Gambar Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit // Agar seluruh dokumen terlihat
            )
        }

        // Tombol Action (Try Again & Done)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tombol Try Again
            OutlinedButton(
                onClick = onTryAgain,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
            ) {
                Text("Try Again")
            }

            // Tombol Done
            Button(
                onClick = onDone,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF536DFE)) // Biru Primary
            ) {
                Text("Done", color = Color.White)
            }
        }
    }
}

// --- FUNGSI LOGIC MENGAMBIL FOTO ---
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

                // Konversi ImageProxy ke Bitmap (Rotasi otomatis ditangani CameraX biasanya, tapi perlu handling manual untuk save ke File)
                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val bitmap = image.toBitmap()
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )

                // Simpan ke File Temporary
                val uri = saveBitmapToCache(context, rotatedBitmap)

                image.close() // Jangan lupa close image proxy
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

// Helper untuk menyimpan Bitmap ke Cache App
private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val imagesDir = File(context.cacheDir, "images")
    if (!imagesDir.exists()) imagesDir.mkdirs()

    val file = File(imagesDir, "scanned_doc_${System.currentTimeMillis()}.jpg")
    val outputStream = file.outputStream()

    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    outputStream.flush()
    outputStream.close()

    return Uri.fromFile(file)
}