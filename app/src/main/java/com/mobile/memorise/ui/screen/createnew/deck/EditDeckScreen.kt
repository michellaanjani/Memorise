package com.mobile.memorise.ui.screen.createnew.deck

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditDeckScreen(
    oldName: String,
    deckViewModel: DeckViewModel,
    onBackClick: () -> Unit
) {

    var deckName by remember { mutableStateOf(oldName) }
    var deckError by remember { mutableStateOf<String?>(null) }

    var showSuccessPopup by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isFormValid =
        deckName.isNotBlank() &&
                deckError == null &&
                deckName.trim() != oldName.trim()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {

        Spacer(modifier = Modifier.height(52.dp))

        /* TOP BAR */
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() }
            )

            Text(
                "Edit Deck",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(26.dp))

        /* Banner Cards */
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            ImageDeckCard(
                drawableId = R.drawable.memorize,
                modifier = Modifier.weight(1f)
            )
            ImageDeckCard(
                drawableId = R.drawable.learn,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))


        /* FORM */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF3F3FA))
                .padding(26.dp)
        ) {
            Row {
                Text("Deck Name", fontWeight = FontWeight.SemiBold)
                Text("*", color = Color(0xFFC53636))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = deckName,
                onValueChange = {
                    deckName = it
                    val trimmed = it.trim()

                    deckError =
                        if (trimmed.isNotEmpty() &&
                            trimmed != oldName &&
                            deckViewModel.deckList.any { d ->
                                d.name.equals(trimmed, ignoreCase = true)
                            }
                        ) {
                            "Deck name already exists!"
                        } else null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = deckError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor =
                        if (deckError != null) Color.Red else Color(0xFFDBE1F3),
                    focusedBorderColor =
                        if (deckError != null) Color.Red else Color(0xFF0961F5),
                    cursorColor = Color(0xFF0961F5)
                )
            )

            if (deckError != null) {
                Text(
                    deckError!!,
                    color = Color(0xFFFF6905),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))


        /* BUTTON UPDATE */
        Button(
            onClick = {
                deckViewModel.updateDeck(oldName, deckName.trim())
                showSuccessPopup = true

                // auto close then back
                scope.launch {
                    delay(1200)
                    showSuccessPopup = false
                    delay(200)
                    onBackClick()
                }
            },
            enabled = isFormValid,
            modifier = Modifier
                .width(200.dp)
                .height(50.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor =
                    if (isFormValid) Color(0xFF0961F5) else Color(0xFFB9C4FF)
            )
        ) {
            Text("Update Deck", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }


    // =================================================================
    // SUCCESS POPUP
    // =================================================================
    if (showSuccessPopup) {

        val alphaAnim by animateFloatAsState(
            targetValue = if (showSuccessPopup) 1f else 0f,
            animationSpec = tween(250)
        )

        val scaleAnim by animateFloatAsState(
            targetValue = if (showSuccessPopup) 1f else 0.95f,
            animationSpec = tween(250)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f * alphaAnim)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier
                    .padding(32.dp)
                    .graphicsLayer {
                        scaleX = scaleAnim
                        scaleY = scaleAnim
                    }
            ) {

                Column(
                    modifier = Modifier
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Deck Updated!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C24)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Your changes have been saved successfully.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showSuccessPopup = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
