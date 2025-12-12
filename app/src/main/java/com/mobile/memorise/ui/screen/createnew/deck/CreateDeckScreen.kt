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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mobile.memorise.R
import kotlinx.coroutines.delay

// --- Warna ---
private val BgColor = Color.White
private val TextBlack = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF0961F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeckScreen(
    navController: NavHostController,
    folderId: String?,
    onBackClick: () -> Unit,
    deckViewModel: DeckViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // State Lokal
    var deckName by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    // State dari ViewModel
    val createDeckState = deckViewModel.createDeckState
// Cek apakah state saat ini adalah Loading dari Sealed Class
    val isLoading = createDeckState is CreateDeckState.Loading

    // Logic Validasi Form
    val isFormValid = deckName.trim().isNotEmpty() && localError == null

    // --- SIDE EFFECT: Handle Success / Error ---
    LaunchedEffect(createDeckState) {
        when (createDeckState) {
            is CreateDeckState.Success -> {
                // 1. Tampilkan Popup Animasi
                showSuccessPopup = true
                // 2. Tunggu sebentar agar user lihat
                delay(1500)
                // 3. Reset state & Navigasi balik
                deckViewModel.resetState()
                onBackClick()
            }
            is CreateDeckState.Error -> {
                // Tampilkan pesan error String (cegah "scientific code")
                Toast.makeText(context, createDeckState.message, Toast.LENGTH_LONG).show()
                // Jangan reset state disini agar user bisa perbaiki input
            }
            else -> {} // Idle atau Loading
        }
    }

    // Bungkus dengan Box agar Popup bisa muncul di atas (overlay)
    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Scaffold(
            containerColor = BgColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "New Deck",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextBlack
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = TextBlack
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BgColor
                    )
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {

                Spacer(modifier = Modifier.height(10.dp))

                // --- Banner Images (Diisi Gambar) ---
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    ImageDeckCard(R.drawable.memorize, Modifier.weight(1f))
                    ImageDeckCard(R.drawable.learn, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Form Input ---
                Text("Deck Name", fontSize = 14.sp, color = TextGray)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = deckName,
                    onValueChange = { input ->
                        deckName = input
                        val trimmed = input.trim()

                        // Reset error state global jika user mengetik ulang
                        if (createDeckState is CreateDeckState.Error) {
                            deckViewModel.resetState()
                        }

                        // Validasi Lokal (Duplicate Check)
                        localError = when {
                            trimmed.isEmpty() -> "Name is required"
                            deckViewModel.decks.any { it.name.equals(trimmed, ignoreCase = true) } ->
                                "Deck name already exists!"
                            else -> null
                        }
                    },
                    placeholder = { Text("e.g. English Vocab", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = localError != null,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextBlack
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        errorBorderColor = Color.Red,
                        cursorColor = PrimaryBlue,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                // Error Message Animation
                AnimatedVisibility(
                    visible = localError != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = localError ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- Submit Button ---
                Button(
                    onClick = {
                        deckViewModel.createDeck(
                            name = deckName.trim(),
                            description = "Created from app",
                            folderId = folderId
                        )
                    },
                    enabled = isFormValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = if (isFormValid) 4.dp else 0.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = PrimaryBlue.copy(0.5f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Color(0xFF9CA3AF)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text("Create Deck", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // --- SUCCESS POPUP OVERLAY ---
        if (showSuccessPopup) {
            val alphaAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0f,
                animationSpec = tween(250), label = "alpha"
            )
            val scaleAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0.95f,
                animationSpec = tween(250), label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                    .clickable(enabled = false) {}, // Block clicks behind
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .padding(32.dp)
                        .graphicsLayer {
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Success!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Deck created successfully.",
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

// Helper Composable untuk Gambar
@Composable
private fun ImageDeckCard(
    drawableId: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}