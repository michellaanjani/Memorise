package com.mobile.memorise.ui.screen.create.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mobile.memorise.R
import com.mobile.memorise.navigation.MainRoute
import com.mobile.memorise.util.Resource
import com.mobile.memorise.util.getFileName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- COLORS ---
private val PrimaryBlue = Color(0xFF536DFE)
private val TextDark = Color(0xFF1A1C24)
private val TextGray = Color(0xFF757575)
private val BgColor = Color(0xFFF8F9FB)
private val BorderGray = Color(0xFFE0E0E0)
private val InputBg = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGenerationScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
    viewModel: AiViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- STATES ---
    val uploadState by viewModel.uploadState.collectAsState()
    val generateState by viewModel.generateState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var cardFormat by remember { mutableStateOf("Definition / Meaning") }
    var cardsAmount by remember { mutableStateOf("2") }
    var isFormatExpanded by remember { mutableStateOf(false) }

    var isAmountError by remember { mutableStateOf(false) }
    var amountErrorMessage by remember { mutableStateOf("") }

    val formatOptions = listOf("Definition / Meaning", "Question / Answer")
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // --- KAMERA & FILE ---
    val currentBackStack = navController.currentBackStackEntry?.savedStateHandle
    val capturedImageUri by currentBackStack
        ?.getLiveData<String>("captured_image_uri")
        ?.observeAsState() ?: remember { mutableStateOf(null) }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedMimeType by remember { mutableStateOf<String?>(null) }

    val mimeTypes = arrayOf(
        "image/jpeg", "image/png", "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = getFileName(context, uri)
            selectedMimeType = context.contentResolver.getType(uri)
        }
    }

    val isGenerateEnabled = (selectedTab == 0 && capturedImageUri != null) ||
            (selectedTab == 1 && selectedFileUri != null)

    fun validateCardAmount() {
        val amount = cardsAmount.toIntOrNull()
        if (amount == null || amount < 2 || amount > 40) {
            isAmountError = true
            amountErrorMessage = "Min 2, Max 40 cards. Resetting to 2."
            cardsAmount = "2"
        } else {
            isAmountError = false
            amountErrorMessage = ""
        }
    }

    // --- EFFECTS ---
    LaunchedEffect(generateState) {
        val state = generateState
        if (state is Resource.Success) {
            val deckId = state.data?.deckId
            if (deckId != null) {
                navController.navigate(MainRoute.AiDraft.createRoute(deckId))
                viewModel.resetStates()
            }
        } else if (state is Resource.Error) {
            snackbarHostState.showSnackbar(getFriendlyErrorMessage(state.message ?: "Generate gagal"))
            viewModel.resetStates()
        }
    }

    val isLoading = uploadState is Resource.Loading || generateState is Resource.Loading
    val loadingMessage = when {
        uploadState is Resource.Loading -> "Mengunggah dokumen..."
        generateState is Resource.Loading -> "AI sedang menganalisa..."
        else -> ""
    }

    // PERBAIKAN 1: Bungkus Scaffold dengan Box agar LoadingOverlay bisa menutup SEMUANYA (termasuk TopBar & BottomBar)
    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            containerColor = BgColor,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("AI Generation", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick, enabled = !isLoading) { // Disable back saat loading
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextDark)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor)
                )
            },
            bottomBar = {
                // PERBAIKAN 2: Gunakan Box + Button biasa (Filled) agar warna jelas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        // Beri padding bawah jika ada navigasi bar HP, atau biarkan default
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            if (!isGenerateEnabled || isLoading) return@Button
                            validateCardAmount()
                            if (!isAmountError) {
                                val finalUri = if (selectedTab == 0 && capturedImageUri != null) Uri.parse(capturedImageUri) else selectedFileUri
                                if (finalUri != null) {
                                    viewModel.uploadAndGenerate(
                                        context.contentResolver, finalUri, selectedMimeType, selectedFileName,
                                        cardsAmount, cardFormat
                                    )
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("File tidak valid.") }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp), // Tinggi button standar agar mudah ditekan
                        shape = RoundedCornerShape(12.dp),
                        enabled = isGenerateEnabled && !isLoading,
                        // PERBAIKAN 3: Atur warna Button secara eksplisit
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,        // Biru saat aktif
                            contentColor = Color.White,          // Teks putih saat aktif
                            disabledContainerColor = Color(0xFFE0E0E0), // Abu-abu saat disabled/loading
                            disabledContentColor = Color(0xFF9E9E9E)    // Teks abu tua saat disabled
                        )
                    ) {
                        Text(
                            text = if (isLoading) "Processing..." else "Generate Flashcards",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        ) { innerPadding ->
            // --- KONTEN FORM (Sama seperti sebelumnya) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ... (Tab Selector, Input Area, Form Fields - COPY PASTE code lama Anda di sini) ...

                // 1. Tab Selector
                Row(
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                        .background(Color(0xFFF0F2F5), RoundedCornerShape(12.dp)).padding(4.dp)
                ) {
                    TabButton("Camera Input", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                    TabButton("File Input", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                }

                // 2. Input Area
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (selectedTab == 0) "Camera Input" else "File Input", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextDark)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .dashedBorder(1.5.dp, Color(0xFFBDBDBD), 12.dp)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (!isLoading) { // Cegah klik saat loading
                                    if (selectedTab == 0) navController.navigate("camera_screen") else filePickerLauncher.launch(mimeTypes)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // ... Logic Image preview (sama seperti kode Anda) ...
                        if (selectedTab == 0) {
                            if (capturedImageUri != null) {
                                AsyncImage(
                                    model = capturedImageUri, contentDescription = "Captured",
                                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                )
                            } else { DefaultInputPlaceholder(isCamera = true) }
                        } else {
                            if (selectedFileUri != null) {
                                val isImage = context.contentResolver.getType(selectedFileUri!!)?.startsWith("image") == true
                                if (isImage) {
                                    AsyncImage(
                                        model = selectedFileUri, contentDescription = "File",
                                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Description, "Doc", tint = PrimaryBlue, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(selectedFileName ?: "Selected File", color = TextDark, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 16.dp))
                                        Text("Tap to change", color = TextGray, fontSize = 12.sp)
                                    }
                                }
                            } else { DefaultInputPlaceholder(isCamera = false) }
                        }
                    }
                    if (selectedTab == 1 && selectedFileUri == null) {
                        Text("Accepted types: PDF, DOCX, JPG, PNG", fontSize = 12.sp, color = TextGray)
                    }
                }

                // 3. Form Fields
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Card Format
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RichLabel("Card format", true)
                        Box {
                            OutlinedTextField(
                                value = cardFormat, onValueChange = {}, readOnly = true,
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = InputBg, unfocusedContainerColor = InputBg,
                                    focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderGray,
                                    focusedTextColor = TextDark, unfocusedTextColor = TextDark
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { if(!isLoading) isFormatExpanded = !isFormatExpanded }) {
                                        Icon(if (isFormatExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Expand")
                                    }
                                }
                            )
                            Box(Modifier.matchParentSize().clip(RoundedCornerShape(12.dp)).clickable { if(!isLoading) isFormatExpanded = true })
                            DropdownMenu(
                                expanded = isFormatExpanded, onDismissRequest = { isFormatExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f).background(Color.White)
                            ) {
                                formatOptions.forEach { option ->
                                    DropdownMenuItem(text = { Text(option, color = TextDark) }, onClick = { cardFormat = option; isFormatExpanded = false })
                                }
                            }
                        }
                    }

                    // Cards Amount
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RichLabel("Cards amount", true)
                        OutlinedTextField(
                            value = cardsAmount,
                            onValueChange = { newValue -> if (newValue.all { it.isDigit() }) { cardsAmount = newValue; isAmountError = false } },
                            modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) validateCardAmount() },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            isError = isAmountError,
                            supportingText = { if (isAmountError) Text(amountErrorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { validateCardAmount(); keyboardController?.hide(); focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InputBg, unfocusedContainerColor = InputBg,
                                focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderGray,
                                errorBorderColor = Color.Red, focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark, errorTextColor = TextDark
                            )
                        )
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }

        // PERBAIKAN 4: Overlay ditaruh di LUAR Scaffold (paling bawah dalam Box)
        // Ini memastikan overlay menutupi TopBar dan BottomBar
        if (isLoading) {
            AiLoadingOverlay(technicalMessage = loadingMessage)
        }
    }
}

// ... Loading Overlay, TabButton, DefaultInputPlaceholder, dll ...
// Copy paste saja komponen UI lain yang tidak berubah
@Composable
fun AiLoadingOverlay(technicalMessage: String) {
    val loadingMessages = remember {
        listOf(
            "Uploading your document...",
            "AI is reading the material...",
            "Analyzing key points..",
            "Generating flashcard questions...",
            "Almost there...",
            "Polishing card layout..."
        )
    }

    var currentMessageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = loadingMessages[currentMessageIndex],
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Status: $technicalMessage",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DefaultInputPlaceholder(isCamera: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(if (isCamera) R.drawable.upload_camera else R.drawable.upload_file),
            contentDescription = null,
            tint = Color(0xFF546E7A),
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (isCamera) "Tap to open camera" else "Click to upload file",
            color = TextGray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) PrimaryBlue else TextGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun RichLabel(text: String, isRequired: Boolean = false) {
    Row {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
        if (isRequired) Text(" *", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Red)
    }
}

fun Modifier.dashedBorder(width: Dp, color: Color, cornerRadius: Dp) = drawBehind {
    val stroke = Stroke(width = width.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f))
    drawRoundRect(color = color, style = stroke, cornerRadius = CornerRadius(cornerRadius.toPx()))
}

// =========================================================================
// FITUR PENTING: BERSIHKAN PESAN ERROR DARI SERVER YANG ANEH
// =========================================================================
fun getFriendlyErrorMessage(rawMessage: String): String {
    val msg = rawMessage.lowercase()

    // Log pesan error asli ke console untuk debugging (opsional, tapi berguna)
    android.util.Log.e("AI_ERROR", "Raw Error: $rawMessage")

    return when {
        // 1. Tangani error "Overloaded" (Server Penuh/Sibuk)
        msg.contains("overloaded") || msg.contains("too many requests") || msg.contains("quota") ->
            "Server AI sedang sibuk. Mohon tunggu 1-2 menit lalu coba lagi."

        // 2. Tangani error OCR/Vision (Gagal baca gambar)
        msg.contains("ocr_failed") || msg.contains("vision generation failed") ->
            "Gagal membaca teks dari gambar. Pastikan gambar jelas, tidak buram, dan pencahayaan cukup."

        // 3. Tangani error kode server ("err is not defined" dll)
        msg.contains("is not defined") || msg.contains("referenceerror") || msg.contains("internal server error") ->
            "Terjadi kesalahan internal server. Tim kami sedang memperbaikinya. Coba lagi nanti."

        // 4. Koneksi Jaringan
        msg.contains("timeout") -> "Koneksi lambat. Coba cek menu Draft, mungkin sudah berhasil dibuat."
        msg.contains("connect") || msg.contains("host") -> "Gagal terhubung ke server. Periksa koneksi internet Anda."

        // 5. Masalah Data/File
        msg.contains("json") || msg.contains("serialize") -> "Gagal memproses respon server. Coba gambar lain."
        msg.contains("413") || msg.contains("payload too large") -> "Ukuran file terlalu besar. Gunakan file/gambar yang lebih kecil."

        // 6. Fallback (Tampilkan pesan asli jika pendek, potong jika terlalu panjang)
        else -> if (rawMessage.length > 50) "Gagal memproses permintaan. Coba lagi nanti." else "Gagal: $rawMessage"
    }
}