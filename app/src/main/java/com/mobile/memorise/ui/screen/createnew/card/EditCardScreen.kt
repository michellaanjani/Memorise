package com.mobile.memorise.ui.screen.createnew.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.ui.screen.cards.CardItemData
import com.mobile.memorise.ui.screen.createnew.deck.CardOperationState
import com.mobile.memorise.ui.screen.createnew.deck.DeckViewModel
import kotlinx.coroutines.delay

// Definisi Warna Konsisten
private val PrimaryBlue = Color(0xFF3D5CFF)
private val TextBlack = Color(0xFF111827)
private val TextGray = Color(0xFF6B7280)
private val BgColor = Color(0xFFF8F9FB)

@Composable
fun EditCardScreen(
    deckId: String,
    deckName: String,
    card: CardItemData,
    onBackClick: () -> Unit,
    // Menggunakan DeckViewModel (Hilt)
    deckViewModel: DeckViewModel = hiltViewModel()
) {
    // --- STATE ---

    // 1. Simpan nilai asli untuk validasi tombol "Update"
    val originalFront = remember { card.front }
    val originalBack = remember { card.back }

    // 2. State Form
    var front by remember { mutableStateOf(TextFieldValue(card.front)) }
    var back by remember { mutableStateOf(TextFieldValue(card.back)) }

    // State Popup Animasi
    var showSuccessPopup by remember { mutableStateOf(false) }

    // 3. State dari ViewModel
    val operationState = deckViewModel.cardOperationState

    // --- LOGIC ---

    // Reset state ViewModel saat layar dibuka (agar bersih dari error/sukses sebelumnya)
    LaunchedEffect(Unit) {
        deckViewModel.resetCardState()
    }

    // Tombol aktif HANYA JIKA ada perubahan teks DAN tidak sedang loading
    val isEdited = (front.text.trim() != originalFront || back.text.trim() != originalBack) &&
            front.text.isNotBlank() &&
            back.text.isNotBlank() &&
            operationState !is CardOperationState.Loading

    // Handle Success State
    LaunchedEffect(operationState) {
        if (operationState is CardOperationState.Success) {
            showSuccessPopup = true
            delay(1500) // Tampilkan popup 1.5 detik

            // Reset & Navigasi
            deckViewModel.resetCardState()
            onBackClick()
        }
    }

    // --- UI ROOT (Box untuk Overlay) ---
    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {

        Scaffold(
            containerColor = BgColor,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { onBackClick() },
                        tint = TextBlack
                    )

                    Text(
                        text = "Edit Card",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextBlack
                    )

                    // Spacer dummy agar judul di tengah
                    Spacer(modifier = Modifier.size(28.dp))
                }
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // ---------- FORM CONTAINER ----------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(24.dp)
                ) {

                    // FRONT SIDE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Front Side", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextBlack)
                        Text("*", color = Color.Red, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = front,
                        onValueChange = { front = it },
                        placeholder = { Text("Enter front text...", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            // ✅ PERBAIKAN 1: Teks Hitam
                            focusedTextColor = TextBlack,
                            unfocusedTextColor = TextBlack,
                            cursorColor = PrimaryBlue,
                            // Border
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = PrimaryBlue,
                            // Background
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // BACK SIDE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Back Side", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextBlack)
                        Text("*", color = Color.Red, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = back,
                        onValueChange = { back = it },
                        placeholder = { Text("Enter back text...", color = Color.LightGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            // ✅ PERBAIKAN 1: Teks Hitam
                            focusedTextColor = TextBlack,
                            unfocusedTextColor = TextBlack,
                            cursorColor = PrimaryBlue,
                            // Border
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = PrimaryBlue,
                            // Background
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ---------- ERROR MESSAGE ----------
                if (operationState is CardOperationState.Error) {
                    Text(
                        text = operationState.message,
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ---------- UPDATE BUTTON ----------
                Button(
                    onClick = {
                        deckViewModel.updateCard(
                            cardId = card.id,
                            front = front.text.trim(),
                            back = back.text.trim()
                        )
                    },
                    enabled = isEdited,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        // ✅ PERBAIKAN 2: Warna Disabled dibuat lebih gelap
                        disabledContainerColor = Color(0xFFD1D5DB), // Abu-abu solid terlihat
                        contentColor = Color.White,
                        disabledContentColor = Color.White // Teks putih
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (isEdited) 4.dp else 0.dp
                    )
                ) {
                    if (operationState is CardOperationState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // =========================================================
        // ANIMATED SUCCESS POPUP (Overlay)
        // =========================================================
        if (showSuccessPopup) {
            val alphaAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "alpha"
            )
            val scaleAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0.8f,
                animationSpec = tween(durationMillis = 300),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .padding(32.dp)
                        .graphicsLayer {
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                            alpha = alphaAnim
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 40.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon Lingkaran Hijau
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Updated!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Card changes have been saved.",
                            fontSize = 14.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}