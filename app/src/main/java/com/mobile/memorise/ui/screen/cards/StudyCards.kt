package com.mobile.memorise.ui.screen.cards

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.ui.screen.cards.CardItemData

// Warna sesuai desain
private val BgColor = Color(0xFFF8F9FB)
private val TextDark = Color(0xFF1A1C24)
private val LightBlueBadge = Color(0xFFE3F2FD) // Background tulisan Questions
private val BlueText = Color(0xFF2196F3)       // Warna tulisan Questions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckName: String, // Tidak ditampilkan di desain tapi mungkin butuh context
    cardList: List<CardItemData>,
    onBackClick: () -> Unit
) {
    // State untuk Pager (Swipe)
    val pagerState = rememberPagerState(pageCount = { cardList.size })

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
                // Tombol Back (Kiri)
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

                // Indikator Halaman (Tengah) - 1/5
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Horizontal Pager untuk Swipe
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 24.dp), // Biar ada margin kiri kanan
                pageSpacing = 16.dp, // Jarak antar kartu
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Isi sisa ruang vertikal
                    .padding(bottom = 32.dp) // Jarak dari bawah
            ) { page ->
                val cardData = cardList[page]
                FlipCardItem(card = cardData)
            }
        }
    }
}

@Composable
fun FlipCardItem(card: CardItemData) {
    // State untuk membalik kartu (Front/Back)
    var isFlipped by remember { mutableStateOf(false) }

    // Animasi Rotasi
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "FlipAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { isFlipped = !isFlipped } // Klik untuk balik
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density // Efek 3D
            }
    ) {
        // KARTU DEPAN (QUESTION)
        // Tampilkan jika rotasi <= 90 derajat
        if (rotation <= 90f) {
            CardFace(
                badgeText = "Questions",
                mainText = card.front,
                footerText = "Tap to show answer",
                backgroundColor = Color.White
            )
        }
        // KARTU BELAKANG (ANSWER)
        // Tampilkan jika rotasi > 90 derajat
        else {
            CardFace(
                badgeText = "Answer",
                mainText = card.back,
                footerText = "Tap to show question",
                backgroundColor = Color.White,
                // PENTING: Balik lagi textnya agar terbaca normal (karena card diputar 180)
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f
                }
            )
        }
    }
}

@Composable
fun CardFace(
    badgeText: String,
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
            verticalArrangement = Arrangement.SpaceBetween // Atas, Tengah, Bawah
        ) {
            // 1. Badge Atas (Questions/Answer)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightBlueBadge)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = badgeText,
                    color = BlueText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // 2. Text Utama (Tengah)
            Box(
                modifier = Modifier.weight(1f), // Ambil ruang tengah
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

            // 3. Footer (Tap to show...)
            Text(
                text = footerText,
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}