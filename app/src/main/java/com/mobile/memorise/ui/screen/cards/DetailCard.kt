package com.mobile.memorise.ui.screen.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.ui.theme.*
import kotlinx.coroutines.launch // Tambah import ini

// Warna khusus (Sama seperti sebelumnya)
private val BgColor = Color(0xFFF8F9FB)
private val LabelBgColor = Color(0xFFE3F2FD)
private val LabelTextColor = Color(0xFF2196F3)
private val TextDark = Color(0xFF1A1C24)

@Composable
fun DetailCardScreen(
    cards: List<CardItemData>,
    initialIndex: Int = 0,
    onClose: () -> Unit
) {
    // 1. Setup Pager State
    // pageCount memberitahu pager berapa banyak kartu yang ada
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { cards.size }
    )

    // 2. Coroutine Scope untuk animasi tombol Next/Prev
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = BgColor,
        topBar = {
            DetailTopBar(
                // Mengambil index langsung dari pagerState
                currentIndex = pagerState.currentPage,
                totalCards = cards.size,
                onClose = onClose
            )
        },
        bottomBar = {
            DetailBottomBar(
                currentIndex = pagerState.currentPage,
                totalCards = cards.size,
                onPrevClick = {
                    // Animasi geser ke kiri
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNextClick = {
                    // Animasi geser ke kanan
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            )
        }
    ) { innerPadding ->

        // 3. Horizontal Pager (Area Swipe)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 16.dp), // Hapus padding horizontal di container utama agar swipe lebih luas
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (cards.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f), // Isi sisa ruang
                    contentPadding = PaddingValues(horizontal = 24.dp), // Beri jarak antar kartu tetangga (opsional)
                    pageSpacing = 16.dp // Jarak antar kartu
                ) { pageIndex ->
                    // Ambil data berdasarkan index halaman
                    val currentCard = cards[pageIndex]

                    // Render Tampilan Kartu
                    CardContentView(currentCard = currentCard)
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No cards available")
                }
            }
        }
    }
}

// Saya pisahkan UI Kartu ke fungsi sendiri agar kode di atas lebih rapi
@Composable
fun CardContentView(currentCard: CardItemData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // --- FRONT SIDE ---
            SideLabel(text = "Front Side")

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentCard.front,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Divider
            HorizontalDivider(
                thickness = 4.dp,
                color = BgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- BACK SIDE ---
            SideLabel(text = "Back Side")

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentCard.back,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextDark.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- KOMPONEN PENDUKUNG (Tidak Berubah) ---

@Composable
fun DetailTopBar(
    currentIndex: Int,
    totalCards: Int,
    onClose: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${currentIndex + 1}/$totalCards",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 16.sp
            )
        }

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.Gray
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit", fontSize = 14.sp, color = TextBlack)
                        }
                    },
                    onClick = { expanded = false }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = Color.Gray.copy(alpha = 0.3f)
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete", fontSize = 14.sp, color = TextBlack)
                        }
                    },
                    onClick = { expanded = false }
                )
            }
        }
    }
}

@Composable
fun DetailBottomBar(
    currentIndex: Int,
    totalCards: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            enabled = currentIndex > 0,
            onClick = onPrevClick
        )

        Spacer(modifier = Modifier.width(24.dp))

        NavigationButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            enabled = currentIndex < totalCards - 1,
            onClick = onNextClick
        )
    }
}

@Composable
fun NavigationButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (enabled) Color(0xFFEEEEEE) else Color(0xFFF5F5F5))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) Color.Black else Color.LightGray,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun SideLabel(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LabelBgColor)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = LabelTextColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}