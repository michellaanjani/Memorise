package com.mobile.memorise.ui.screen.cards

import android.net.Uri
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
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Warna
private val BgColor = Color(0xFFF8F9FB)
private val LabelBgColor = Color(0xFFE3F2FD)
private val LabelTextColor = Color(0xFF2196F3)
private val TextDark = Color(0xFF1A1C24)

@Composable
fun DetailCardScreen(
    deckId: String, // Diperlukan untuk konteks database
    deckName: String,
    cards: List<CardItemData>,
    initialIndex: Int = 0,
    onClose: () -> Unit,
    onEditCard: (Int, String) -> Unit,
    onDeleteCard: (Int) -> Unit
) {

    // 👉 STATE LOKAL LIST KARTU
    // Kita copy cards ke mutableStateListOf agar bisa dihapus secara UI instant
    val cardList = remember { mutableStateListOf<CardItemData>().apply { addAll(cards) } }

    // 👉 STATE INDEX HALAMAN
    var currentIndex by remember { mutableStateOf(initialIndex) }

    // Jika list kosong, langsung tutup
    if (cardList.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = currentIndex,
        pageCount = { cardList.size }
    )

    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            DetailTopBar(
                currentIndex = pagerState.currentPage,
                totalCards = cardList.size,
                onClose = onClose,
                onEditClick = { index ->
                    val json = Json.encodeToString(cardList.toList())
                    val encodedJson = Uri.encode(json)
                    onEditCard(index, encodedJson)
                },
                onDeleteClick = {
                    showDeleteDialog = true
                }
            )
        },
        bottomBar = {
            DetailBottomBar(
                currentIndex = pagerState.currentPage,
                totalCards = cardList.size,
                onPrevClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNextClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 16.dp
            ) { pageIndex ->
                // Pastikan index aman (cegah crash saat delete item terakhir)
                val currentCard = cardList.getOrNull(pageIndex)
                if (currentCard != null) {
                    CardContentView(currentCard)
                }
            }
        }
    }

    // 👉 DIALOG HAPUS
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                val currentPage = pagerState.currentPage

                // 1. Panggil Callback ke Parent (untuk hapus di Database/ViewModel)
                onDeleteCard(currentPage)

                // 2. Hapus dari State UI Lokal (supaya hilang visualnya)
                if (currentPage < cardList.size) {
                    cardList.removeAt(currentPage)
                }

                // 3. Atur Navigasi
                if (cardList.isNotEmpty()) {
                    // Geser index jika perlu
                    val newIndex = currentPage.coerceAtMost(cardList.lastIndex)
                    currentIndex = newIndex
                    // Paksa scroll jika index berubah drastis (opsional, pagerState handle otomatis biasanya)
                } else {
                    onClose()
                }

                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun CardContentView(currentCard: CardItemData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SideLabel(text = "Front Side")
            Spacer(Modifier.height(24.dp))

            Text(
                text = currentCard.front,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            HorizontalDivider(
                thickness = 4.dp,
                color = BgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
            )

            Spacer(Modifier.height(32.dp))

            SideLabel(text = "Back Side")
            Spacer(Modifier.height(24.dp))

            Text(
                text = currentCard.back,
                fontSize = 16.sp,
                color = TextDark.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DetailTopBar(
    currentIndex: Int,
    totalCards: Int,
    onClose: () -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit
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
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
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
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFFF9800))
                            Spacer(Modifier.width(8.dp))
                            Text("Edit", color = Color.Black)
                        }
                    },
                    onClick = {
                        expanded = false
                        onEditClick(currentIndex)
                    }
                )

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete", color = Color.Black)
                        }
                    },
                    onClick = {
                        expanded = false
                        onDeleteClick(currentIndex)
                    }
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

        Spacer(Modifier.width(24.dp))

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