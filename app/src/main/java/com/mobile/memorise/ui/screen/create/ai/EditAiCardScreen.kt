package com.mobile.memorise.ui.screen.create.ai

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
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- COLORS ---
private val BgColor = Color(0xFFF8F9FB)
private val PrimaryBlue = Color(0xFF0C3DF4)
private val InactiveBg = Color(0xFFB3D4FC)
private val InactiveText = Color(0xFF757575)

@Composable
fun EditAiCardScreen(
    cardId: String,
    onBackClick: () -> Unit,
    viewModel: AiViewModel = hiltViewModel()
) {
    val draftState by viewModel.draftState.collectAsState()

    val draftData = (draftState as? Resource.Success)?.data
    val deckId = draftData?.deck?.id

    val card = remember(draftData, cardId) {
        draftData?.cards?.find { it.id == cardId }
    }

    var front by remember(card) { mutableStateOf(TextFieldValue(card?.front ?: "")) }
    var back by remember(card) { mutableStateOf(TextFieldValue(card?.back ?: "")) }

    var showSuccessPopup by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isEdited = card != null && (front.text != card.front || back.text != card.back)

    if (card == null || deckId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
                    placeholder = { Text("Enter AI front text...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
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
                    placeholder = { Text("Enter AI back text...", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
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
                        viewModel.updateDraftCard(
                            deckId = deckId,
                            cardId = card.id,
                            front = front.text.trim(),
                            back = back.text.trim()
                        )

                        showSuccessPopup = true
                        scope.launch {
                            delay(1500) // Waktu tunggu animasi selesai
                            showSuccessPopup = false
                            onBackClick()
                        }
                    },
                    enabled = isEdited,
                    modifier = Modifier
                        .widthIn(min = 160.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        disabledContainerColor = InactiveBg,
                        disabledContentColor = InactiveText
                    )
                ) {
                    Text("Update Card", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // --- SUCCESS POPUP ANIMATION ---
        if (showSuccessPopup) {
            // State animasi
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

            // Overlay Background (Semi-transparent black)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                    .clickable(enabled = false) {}, // Mencegah klik di belakang
                contentAlignment = Alignment.Center
            ) {
                // Card Popup
                Card(
                    shape = RoundedCornerShape(20.dp),
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
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Green Circle Icon
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = "Success!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subtitle
                        Text(
                            text = "Card updated successfully.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}