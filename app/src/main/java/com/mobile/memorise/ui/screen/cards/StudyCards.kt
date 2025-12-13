package com.mobile.memorise.ui.screen.cards

import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
import com.mobile.memorise.ui.screen.createnew.deck.DeckViewModel
import kotlinx.coroutines.launch

// --- WARNA ---
private val BgColor = Color(0xFFF8F9FB)
private val TextDark = Color(0xFF000000)

// Warna Question (Biru)
private val LightBlueBadge = Color(0xFFE3F2FD)
private val BlueText = Color(0xFF2196F3)

// Warna Answer (Hijau)
private val LightGreenBadge = Color(0xFFE8F5E9)
private val GreenText = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckName: String,
    cardList: List<CardItemData>, // Fallback data dari Navigasi
    onBackClick: () -> Unit,
    deckId: String? = null, // Opsional: jika ingin load fresh data
    deckViewModel: DeckViewModel = hiltViewModel()
) {
    // 1. Fetch data terbaru jika deckId tersedia
    LaunchedEffect(deckId) {
        if (!deckId.isNullOrEmpty()) {
            deckViewModel.loadCards(deckId)
        }
    }

    // 2. Ambil State dari ViewModel
    val rawCards = deckViewModel.cards
    val isLoading = deckViewModel.areCardsLoading

    // 3. Tentukan List yang Dipakai (Prioritas: ViewModel > NavParam)
    val displayCards = remember(rawCards.size, rawCards, cardList) {
        if (rawCards.isNotEmpty()) {
            rawCards.map { CardItemData(id = it.id, front = it.front, back = it.back) }
        } else {
            cardList
        }
    }

    // 4. Loading State
    if (!deckId.isNullOrEmpty() && isLoading && displayCards.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BlueText)
        }
        return
    }

    // Pager & Scope
    val pagerState = rememberPagerState(pageCount = { displayCards.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = BgColor,
        topBar = {
            // Custom Top Bar dengan Indikator Halaman
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Tombol Back di Kiri
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

                // Indikator Halaman di Tengah
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val total = if (displayCards.isEmpty()) 0 else displayCards.size
                    val current = if (displayCards.isEmpty()) 0 else pagerState.currentPage + 1
                    Text(
                        text = "$current/$total",
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

            // 1. AREA KARTU UTAMA
            if (displayCards.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Ambil sisa ruang vertikal
                ) { page ->
                    FlipCardItem(card = displayCards[page])
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No cards available.", color = Color.Gray)
                }
            }

            // 2. TOMBOL NAVIGASI BAWAH
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PREV Button
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = BlueText,
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Prev",
                        tint = Color.White
                    )
                }

                // NEXT Button
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    enabled = pagerState.currentPage < displayCards.size - 1,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = BlueText,
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
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
    val context = LocalContext.current

    // Audio Playback
    fun playFlipSound() {
        try {
            val mp = MediaPlayer.create(context, R.raw.flip)
            mp.setOnCompletionListener { it.release() }
            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "FlipAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                isFlipped = !isFlipped
                playFlipSound()
            }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        // TAMPILAN DEPAN (QUESTION)
        if (rotation <= 90f) {
            CardFace(
                badgeText = "Question",
                badgeBgColor = LightBlueBadge,
                badgeTextColor = BlueText,
                mainText = card.front,
                footerText = "Tap to show answer",
                backgroundColor = Color.White
            )
        }
        // TAMPILAN BELAKANG (ANSWER)
        else {
            CardFace(
                badgeText = "Answer",
                badgeBgColor = LightGreenBadge,
                badgeTextColor = GreenText,
                mainText = card.back,
                footerText = "Tap to show question",
                backgroundColor = Color.White,
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f // Agar teks tidak terbalik
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
            // BADGE
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = badgeText,
                    color = badgeTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // KONTEN UTAMA
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

            // FOOTER HINT
            Text(
                text = footerText,
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}