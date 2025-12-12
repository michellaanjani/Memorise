package com.mobile.memorise.ui.screen.createnew.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
import com.mobile.memorise.ui.screen.cards.CardItemData
import com.mobile.memorise.ui.screen.createnew.deck.CardOperationState
import com.mobile.memorise.ui.screen.createnew.deck.DeckViewModel
import kotlinx.coroutines.delay

@Composable
fun EditCardScreen(
    deckId: String,
    deckName: String,
    card: CardItemData,
    onBackClick: () -> Unit,
    // Menggunakan DeckViewModel (Hilt) untuk update ke Database
    deckViewModel: DeckViewModel = hiltViewModel()
) {
    // ORIGINAL VALUES
    val originalFront = card.front
    val originalBack = card.back

    // FORM VALUES
    var front by remember { mutableStateOf(TextFieldValue(card.front)) }
    var back by remember { mutableStateOf(TextFieldValue(card.back)) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    // OBSERVE STATE DARI VIEWMODEL
    val operationState = deckViewModel.cardOperationState

    // BUTTON ENABLED CONDITION: Teks berubah DAN tidak loading
    val isEdited = (front.text != originalFront || back.text != originalBack) &&
            operationState !is CardOperationState.Loading

    // HANDLE SUCCESS / NAVIGATION
    LaunchedEffect(operationState) {
        if (operationState is CardOperationState.Success) {
            showSuccessPopup = true
            delay(1000) // Tampilkan popup sebentar
            deckViewModel.resetCardState() // Reset state agar bersih
            onBackClick()
        }
    }

    // Root menggunakan Box agar Popup bisa overlay di tengah
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                // Fitur scroll dari Upstream (Penting)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
        ) {

            // ---------- TOP BAR ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() }
                )

                Text(
                    text = "Edit Card",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 28.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------- FORM CONTAINER ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {

                // FRONT SIDE
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Front Side", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("*", color = Color.Red, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    placeholder = { Text("Enter front text...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE7ECF5),
                        focusedBorderColor = Color(0xFF3D5CFF),
                        cursorColor = Color(0xFF3D5CFF)
                    )
                )

                // BACK SIDE
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Back Side", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("*", color = Color.Red, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    placeholder = { Text("Enter back text...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE7ECF5),
                        focusedBorderColor = Color(0xFF3D5CFF),
                        cursorColor = Color(0xFF3D5CFF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------- UPDATE BUTTON ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        // Panggil fungsi updateCard dari ViewModel (Stashed Logic)
                        deckViewModel.updateCard(
                            cardId = card.id,
                            front = front.text.trim(),
                            back = back.text.trim()
                        )
                    },
                    enabled = isEdited,
                    modifier = Modifier
                        .widthIn(min = 160.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEdited) Color(0xFF3D5CFF) else Color(0xFFB9C4FF),
                        contentColor = Color.White
                    )
                ) {
                    if (operationState is CardOperationState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Update Card", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Menampilkan error jika ada
            if (operationState is CardOperationState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = operationState.message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Spacer bawah agar scroll nyaman
            Spacer(modifier = Modifier.height(50.dp))
        }

        // ---------- POPUP SUCCESS (Overlay) ----------
        if (showSuccessPopup) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF7CFF8A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Card Updated!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}