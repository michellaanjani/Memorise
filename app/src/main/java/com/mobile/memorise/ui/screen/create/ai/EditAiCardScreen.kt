package com.mobile.memorise.ui.screen.create.ai

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import kotlinx.coroutines.delay

@Composable
fun EditAiCardScreen(
    card: AiDraftCard,
    onBackClick: () -> Unit,
    onCardUpdated: (AiDraftCard) -> Unit
) {
    val originalFront = card.frontSide
    val originalBack = card.backSide

    var front by remember { mutableStateOf(TextFieldValue(card.frontSide)) }
    var back by remember { mutableStateOf(TextFieldValue(card.backSide)) }

    val isEdited = front.text != originalFront || back.text != originalBack

    var showSuccessPopup by remember { mutableStateOf(false) }

    // Auto-close popup
    LaunchedEffect(showSuccessPopup) {
        if (showSuccessPopup) {
            delay(1500)
            showSuccessPopup = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
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
                    text = "Edit AI Card",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 28.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Front Side (AI)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
                    shape = RoundedCornerShape(12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Back Side (AI)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
                            AiDraftCard(
                                frontSide = front.text.trim(),
                                backSide = back.text.trim()
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

        // POPUP SUCCESS
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
