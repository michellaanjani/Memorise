package com.mobile.memorise.ui.screen.createnew.deck

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import kotlinx.coroutines.delay

private val TextBlack = Color(0xFF111827)
private val TextGray = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF0961F5)
private val BgColor = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeckScreen(
    deckId: String,
    deckViewModel: DeckViewModel, // Pastikan ini adalah ViewModel yang sudah diperbaiki (menerima folderId di updateDeck)
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val editState = deckViewModel.editDeckState
    val isLoading = editState is EditDeckState.Loading

    // 1. Ambil data deck yang sedang diedit
    // Menggunakan derivedStateOf agar hanya recompose jika item spesifik ini berubah
    val targetDeck by remember(deckId) {
        derivedStateOf { deckViewModel.getDeckById(deckId) }
    }

    // 2. State Form
    var deckName by remember { mutableStateOf("") }
    var isDataLoaded by remember { mutableStateOf(false) }

    // =====================================================================
    // PERBAIKAN UTAMA: SIMPAN FOLDER ID ASLI
    // =====================================================================
    // Kita simpan folderId dari data deck yang diambil.
    // Jika deck ada di dalam folder, ini akan berisi ID Folder. Jika di Home, ini null.
    val originalFolderId = remember(targetDeck) { targetDeck?.folderId }
    val originalDesc = targetDeck?.description ?: ""

    // Logic: Segera isi deckName saat data tersedia pertama kali
    LaunchedEffect(targetDeck) {
        if (!isDataLoaded && targetDeck != null) {
            deckName = targetDeck!!.name
            isDataLoaded = true
        }
    }

    var localError by remember { mutableStateOf<String?>(null) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    // Validasi sederhana
    val isChanged = if (targetDeck != null) deckName.trim() != targetDeck!!.name else deckName.isNotEmpty()
    val isFormValid = deckName.isNotBlank() && localError == null && isChanged

    // Side Effects untuk menangani Success/Error dari ViewModel
    LaunchedEffect(editState) {
        when (editState) {
            is EditDeckState.Success -> {
                showSuccessPopup = true
                delay(1500)
                deckViewModel.onEditSuccessHandled()
                onBackClick()
            }
            is EditDeckState.Error -> {
                Toast.makeText(context, editState.message, Toast.LENGTH_LONG).show()
                deckViewModel.resetEditStateOnly()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Scaffold(
            containerColor = BgColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Edit Deck", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextBlack) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = TextBlack)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Header Image
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().height(80.dp)) {
                    ImageDeckCard(R.drawable.memorize, Modifier.weight(1f))
                    ImageDeckCard(R.drawable.learn, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(32.dp))

                // Form Input
                Text("Deck Name", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deckName,
                    onValueChange = { input ->
                        deckName = input
                        val trimmed = input.trim()
                        if (editState is EditDeckState.Error) deckViewModel.resetEditStateOnly()

                        // Validasi Lokal
                        localError = if (trimmed.isEmpty()) {
                            "Name cannot be empty"
                        } else if (deckViewModel.decks.any {
                                it.name.equals(trimmed, ignoreCase = true) && it.id != deckId
                            }) {
                            "Deck name already exists!"
                        } else {
                            null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = localError != null,
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextBlack),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        errorBorderColor = Color.Red,
                        cursorColor = PrimaryBlue,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    ),
                    placeholder = { Text("Enter deck name", color = Color.LightGray) }
                )

                AnimatedVisibility(visible = localError != null, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                    Text(localError ?: "", color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Tombol Save
                Button(
                    onClick = {
                        // =================================================
                        // PANGGIL VIEWMODEL DENGAN 4 PARAMETER
                        // =================================================
                        deckViewModel.updateDeck(
                            deckId = deckId,
                            name = deckName.trim(),
                            description = originalDesc, // Pertahankan deskripsi lama
                            folderId = originalFolderId // <--- JANGAN KIRIM NULL, tapi kirim ID yang ada
                        )
                    },
                    enabled = isFormValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(if (isFormValid) 4.dp else 0.dp, RoundedCornerShape(16.dp), spotColor = PrimaryBlue.copy(0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Color(0xFF9CA3AF)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Success Popup
        if (showSuccessPopup) {
            val alphaAnim by animateFloatAsState(if (showSuccessPopup) 1f else 0f, tween(250), label = "alpha")
            val scaleAnim by animateFloatAsState(if (showSuccessPopup) 1f else 0.95f, tween(250), label = "scale")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .padding(32.dp)
                        .graphicsLayer { scaleX = scaleAnim; scaleY = scaleAnim }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF166534), modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Success!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Deck updated successfully.", fontSize = 14.sp, color = TextGray, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageDeckCard(drawableId: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}