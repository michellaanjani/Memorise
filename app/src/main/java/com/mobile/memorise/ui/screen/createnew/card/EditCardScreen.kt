package com.mobile.memorise.ui.screen.createnew.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.mobile.memorise.R
import com.mobile.memorise.ui.screen.cards.CardItemData
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect


@Composable
fun EditCardScreen(
    deckName: String,
    card: CardItemData,
    onBackClick: () -> Unit,
    onCardUpdated: (CardItemData) -> Unit
) {
    val originalFront = card.front
    val originalBack = card.back

    var front by remember { mutableStateOf(TextFieldValue(card.front)) }
    var back by remember { mutableStateOf(TextFieldValue(card.back)) }

    val isEdited = front.text != originalFront || back.text != originalBack

    var showSuccessPopup by remember { mutableStateOf(false) }

    // FIX: Popup bekerja otomatis
    LaunchedEffect(showSuccessPopup) {
        if (showSuccessPopup) {
            kotlinx.coroutines.delay(1500)
            showSuccessPopup = false
        }
    }

    Box(   // <<< FIX: gunakan Box agar popup bisa overlay
        modifier = Modifier
            .fillMaxSize()
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

            // FORM BOX
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {

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
                    shape = RoundedCornerShape(12.dp)
                )

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
                    shape = RoundedCornerShape(12.dp)
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
                        onCardUpdated(
                            card.copy(
                                front = front.text.trim(),
                                back = back.text.trim()
                            )
                        )
                        showSuccessPopup = true
                    },
                    enabled = isEdited,
                    modifier = Modifier
                        .widthIn(min = 160.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Update Card", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // POPUP (overlay)
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

