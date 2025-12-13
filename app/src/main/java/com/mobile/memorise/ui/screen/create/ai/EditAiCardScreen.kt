package com.mobile.memorise.ui.screen.create.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- COLORS ---
private val BgColor = Color(0xFFF8F9FB)
private val PrimaryBlue = Color(0xFF536DFE)

@Composable
fun EditAiCardScreen(
    cardId: String, // Terima ID
    onBackClick: () -> Unit,
    viewModel: AiViewModel = hiltViewModel()
) {
    // 1. Ambil data kartu dari ViewModel state 'draftSession' (bukan draftDeck)
    val draftData by viewModel.draftSession.collectAsState()

    // Cari kartu berdasarkan ID dari list cards yang ada di session
    val card = remember(draftData, cardId) {
        draftData?.cards?.find { it.id == cardId }
    }

    // State untuk form menggunakan TextFieldValue agar cursor stabil
    var front by remember(card) { mutableStateOf(TextFieldValue(card?.front ?: "")) }
    var back by remember(card) { mutableStateOf(TextFieldValue(card?.back ?: "")) }

    // State Popup
    var showSuccessPopup by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Cek apakah ada perubahan data
    val isEdited = card != null && (front.text != card.front || back.text != card.back)

    // Jika data card tidak ditemukan (misal error load / loading awal), tampilkan Loading
    if (card == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
        ) {

            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() }
                )

                Text(
                    text = "Edit AI Card",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 28.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // FORM BOX
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {

                // Front Side Input
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Front Side (AI)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
                    Text("*", color = Color.Red, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    placeholder = { Text("Enter AI front text...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                // Back Side Input
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Back Side (AI)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
                    Text("*", color = Color.Red, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    placeholder = { Text("Enter AI back text...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        // 2. Panggil ViewModel Update
                        viewModel.updateCard(
                            cardId = card.id,
                            front = front.text.trim(),
                            back = back.text.trim()
                        )

                        // 3. Tampilkan Popup & Navigasi Balik
                        showSuccessPopup = true
                        scope.launch {
                            delay(1500)
                            showSuccessPopup = false
                            onBackClick() // Kembali ke layar sebelumnya
                        }
                    },
                    enabled = isEdited,
                    modifier = Modifier
                        .widthIn(min = 160.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    )
                ) {
                    Text("Update Card", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }

        // POPUP SUCCESS
        if (showSuccessPopup) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF7CFF8A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "AI Card Updated!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}