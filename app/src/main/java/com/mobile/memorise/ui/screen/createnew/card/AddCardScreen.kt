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
import com.mobile.memorise.R
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay

@Composable
fun AddCardScreen(
    deckId: String,
    deckName: String,
    onBackClick: () -> Unit,
    deckRemoteViewModel: com.mobile.memorise.ui.viewmodel.DeckRemoteViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var front by remember { mutableStateOf(TextFieldValue("")) }
    var back by remember { mutableStateOf(TextFieldValue("")) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    val mutationState by deckRemoteViewModel.cardMutationState.collectAsState()

    LaunchedEffect(mutationState) {
        if (mutationState is com.mobile.memorise.util.Resource.Success) {
            showSuccessPopup = true
            delay(800)
            onBackClick()
        }
    }

    val isFormValid = front.text.isNotBlank() && back.text.isNotBlank() && mutationState !is com.mobile.memorise.util.Resource.Loading

    Box(modifier = Modifier.fillMaxSize()) {

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
                    text = "Add Cards",
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

            // ---------- BUTTON (reduced width) ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        deckRemoteViewModel.createCard(
                            deckId = deckId,
                            front = front.text.trim(),
                            back = back.text.trim()
                        )
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .widthIn(min = 160.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) Color(0xFF3D5CFF) else Color(0xFFB9C4FF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Add Card", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // ---------- POPUP SUCCESS ----------
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
                        "Card added!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
