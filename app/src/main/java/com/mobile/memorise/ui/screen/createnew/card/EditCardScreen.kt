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

@Composable
fun EditCardScreen(
    deckName: String,
    card: CardItemData,
    onBackClick: () -> Unit,
    onCardUpdated: (CardItemData) -> Unit
) {
    // ORIGINAL VALUES
    val originalFront = card.front
    val originalBack = card.back

    // FORM VALUES
    var front by remember { mutableStateOf(TextFieldValue(card.front)) }
    var back by remember { mutableStateOf(TextFieldValue(card.back)) }

    // BUTTON ENABLED CONDITION
    val isEdited = front.text != originalFront || back.text != originalBack

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        // ---------- FORM CONTAINER (SAMA PERSIS ADD CARD) ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {

            // FRONT
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

            // BACK
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
                    val updatedCard = card.copy(
                        front = front.text.trim(),
                        back = back.text.trim()
                    )
                    onCardUpdated(updatedCard)
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
                Text("Update Card", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
