package com.mobile.memorise.ui.screen.cards

import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.mobile.memorise.ui.theme.CalmBlue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
// Pastikan R di-import. Jika error, sesuaikan dengan package name Anda
import com.mobile.memorise.R

// --- Warna ---
private val BgColor = Color(0xFFF8F9FB)
private val TextDark = Color(0xFF000000)

// Warna untuk Question (Biru)
private val LightBlueBadge = Color(0xFFE3F2FD)
private val BlueText = Color(0xFF2196F3)

// Warna untuk Answer (Hijau) - Agar beda visualnya
private val LightGreenBadge = Color(0xFFE8F5E9)
private val GreenText = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckName: String,
    cardList: List<CardItemData>,
    onBackClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { cardList.size })
    val scope = rememberCoroutineScope() // Untuk animasi scroll button

    Scaffold(
        containerColor = BgColor,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${cardList.size}",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center, // Konten di tengah vertikal
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Area Kartu (Pager)
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Mengambil space terbesar
            ) { page ->
                val cardData = cardList[page]
                FlipCardItem(card = cardData)
            }

            // 2. Tombol Navigasi (Prev / Next)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 40.dp), // Jarak dari bawah
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Previous
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    enabled = pagerState.currentPage > 0, // Disable jika di awal
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = BlueText, // Warna biru biar menonjol
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous Card",
                        tint = Color.White
                    )
                }

                // Tombol Next
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    enabled = pagerState.currentPage < cardList.size - 1, // Disable jika di akhir
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = BlueText, // Warna biru biar menonjol
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Card",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FlipCardItem(card: CardItemData) {
    var isFlipped by remember { mutableStateOf(false) }
    val context = LocalContext.current // Context untuk Audio

    // Fungsi Helper untuk Audio
    fun playFlipSound() {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.flip)
            mediaPlayer.setOnCompletionListener { mp -> mp.release() } // Hapus memori setelah selesai
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "FlipAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                isFlipped = !isFlipped
                playFlipSound() // Play Audio saat klik
            }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        // KARTU DEPAN (QUESTION)
        if (rotation <= 90f) {
            CardFace(
                badgeText = "Questions",
                badgeBgColor = LightBlueBadge, // Biru Muda
                badgeTextColor = BlueText,     // Biru
                mainText = card.front,
                footerText = "Tap to show answer",
                backgroundColor = Color.White
            )
        }
        // KARTU BELAKANG (ANSWER)
        else {
            CardFace(
                badgeText = "Answer",
                badgeBgColor = LightGreenBadge, // Hijau Muda (Beda Visual)
                badgeTextColor = GreenText,     // Hijau
                mainText = card.back,
                footerText = "Tap to show question",
                backgroundColor = Color.White,
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f // Balik text agar terbaca normal
                }
            )
        }
    }
}

@Composable
fun CardFace(
    badgeText: String,
    badgeBgColor: Color,
    badgeTextColor: Color,
    mainText: String,
    footerText: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Badge Atas (Questions/Answer) - Warna dinamis
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBgColor) // Warna Background Badge Berubah
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = badgeText,
                    color = badgeTextColor, // Warna Text Badge Berubah
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // 2. Text Utama
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mainText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
            }

            // 3. Footer
            Text(
                text = footerText,
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}