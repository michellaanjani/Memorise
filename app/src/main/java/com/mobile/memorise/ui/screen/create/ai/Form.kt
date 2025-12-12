package com.mobile.memorise.ui.screen.create.ai

import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.material.icons.filled.Description // Icon untuk dokumen
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mobile.memorise.R

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
    onGenerateClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Camera, 1 = File

    // Form State
    var cardFormat by remember { mutableStateOf("Definition / Meaning") }
    var cardsAmount by remember { mutableStateOf("2") }
    var isFormatExpanded by remember { mutableStateOf(false) }

    // Validasi State
    var isAmountError by remember { mutableStateOf(false) }
    var amountErrorMessage by remember { mutableStateOf("") }

    val formatOptions = listOf("Definition / Meaning", "Question / Answer")
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // --- STATE KAMERA ---
    val currentBackStack = navController.currentBackStackEntry?.savedStateHandle
    val capturedImageUri by currentBackStack
        ?.getLiveData<String>("captured_image_uri")
        ?.observeAsState() ?: remember { mutableStateOf(null) }

    // --- STATE FILE PICKER ---
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }

    // Definisi Tipe File yang Diizinkan
    val mimeTypes = arrayOf(
        "image/jpeg",
        "image/png",
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    )

    // Launcher untuk Membuka File Explorer
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // Simpan URI
            selectedFileUri = uri
            // Ambil Nama File untuk ditampilkan
            selectedFileName = getFileName(context, uri)
        }
    }
    // --- BUTTON ACTIVE CONDITION ---
    val isGenerateEnabled =
        (selectedTab == 0 && capturedImageUri != null) ||
                (selectedTab == 1 && selectedFileUri != null)

    // Logic Validasi
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

    Scaffold(
        containerColor = BgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Generation", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor)
            )
        },
        bottomBar = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isGenerateEnabled) Color.White else Color(0xFFE0E0E0), // disabled look
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(46.dp)
            ) {
                TextButton(
                    onClick = {
                        if (!isGenerateEnabled) return@TextButton

                        validateCardAmount()
                        if (!isAmountError) onGenerateClick()
                    },
                    modifier = Modifier.fillMaxSize(),
                    enabled = isGenerateEnabled,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isGenerateEnabled) PrimaryBlue else Color(0xFF9E9E9E) // disabled text
                    )
                ) {
                    Text(
                        "Generate",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Custom Tab Selector
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
                            if (selectedTab == 0) {
                                // Buka Kamera
                                navController.navigate("camera_screen")
                            } else {
                                // Buka File Explorer dengan Filter
                                filePickerLauncher.launch(mimeTypes)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // --- LOGIKA TAMPILAN KONTEN BOX ---
                    if (selectedTab == 0) {
                        // --- TAB KAMERA ---
                        if (capturedImageUri != null) {
                            AsyncImage(
                                model = capturedImageUri,
                                contentDescription = "Captured Document",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            DefaultInputPlaceholder(isCamera = true)
                        }
                    } else {
                        // --- TAB FILE INPUT ---
                        if (selectedFileUri != null) {
                            // Cek apakah file adalah gambar atau dokumen
                            val isImage = context.contentResolver.getType(selectedFileUri!!)?.startsWith("image") == true

                            if (isImage) {
                                AsyncImage(
                                    model = selectedFileUri,
                                    contentDescription = "Selected File",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Tampilan untuk Dokumen (PDF/DOCX)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "Doc",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = selectedFileName ?: "Selected File",
                                        color = TextDark,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Text(
                                        text = "Tap to change",
                                        color = TextGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            DefaultInputPlaceholder(isCamera = false)
                        }
                    }
                }

                // Helper text di bawah box (hanya untuk File Input)
                if (selectedTab == 1 && selectedFileUri == null) {
                    Text(
                        text = "Accepted types: PDF, DOCX, JPG, PNG",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }

            // 3. Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                // Card Format
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RichLabel("Card format", true)
                    Box {
                        OutlinedTextField(
                            value = cardFormat,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = InputBg,
                                unfocusedContainerColor = InputBg,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark
                            ),
                            trailingIcon = {
                                IconButton(onClick = { isFormatExpanded = !isFormatExpanded }) {
                                    Icon(if (isFormatExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Expand")
                                }
                            }
                        )
                        Box(Modifier.matchParentSize().clip(RoundedCornerShape(12.dp)).clickable { isFormatExpanded = true })
                        DropdownMenu(
                            expanded = isFormatExpanded,
                            onDismissRequest = { isFormatExpanded = false },
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
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                cardsAmount = newValue
                                isAmountError = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) validateCardAmount()
                            },
                        shape = RoundedCornerShape(12.dp),
                        isError = isAmountError,
                        supportingText = {
                            if (isAmountError) Text(amountErrorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            validateCardAmount()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBg,
                            unfocusedContainerColor = InputBg,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderGray,
                            errorBorderColor = Color.Red,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            errorTextColor = TextDark
                        )
                    )
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

// --- HELPER COMPOSABLES ---

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

// --- HELPER FUNCTION: Get File Name from URI ---
fun getFileName(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

// ... TabButton, RichLabel, dashedBorder Tetap Sama ...
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