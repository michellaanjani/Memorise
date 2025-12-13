package com.mobile.memorise.ui.screen.create.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// 1. Ganti Import DTO ke Domain Model
import com.mobile.memorise.domain.model.Card
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import com.mobile.memorise.ui.screen.cards.DetailBottomBar
import com.mobile.memorise.ui.screen.cards.DetailTopBar
import com.mobile.memorise.ui.screen.cards.SideLabel

// --- COLORS ---
private val BgColor = Color(0xFFF8F9FB)
private val TextDark = Color(0xFF1A1C24)
private val PrimaryBlue = Color(0xFF536DFE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDetailCardScreen(
    navController: NavController,
    initialIndex: Int,
    viewModel: AiViewModel = hiltViewModel() // Inject ViewModel
) {
    // 2. Observe Data dari 'draftSession' (bukan draftDeck)
    val draftData by viewModel.draftSession.collectAsState()

    // Ambil list kartu terbaru. Jika null, gunakan empty list.
    val cardList = remember(draftData) { draftData?.cards ?: emptyList() }

    // Jika list kosong (misal semua dihapus), kembali ke screen sebelumnya
    LaunchedEffect(cardList.size) {
        // Cek jika draftData sudah terload tapi kosong, baru popBackStack
        // (Hindari pop saat inisialisasi awal/loading)
        if (draftData != null && cardList.isEmpty()) {
            navController.popBackStack()
        }
    }

    // Safety check
    if (cardList.isEmpty()) return

    // 3. Pager State
    // Pastikan initial page valid
    val validInitialIndex = initialIndex.coerceIn(0, (cardList.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(
        initialPage = validInitialIndex,
        pageCount = { cardList.size }
    )

    val scope = rememberCoroutineScope()

    // 4. Dialog States
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Temp variables untuk Edit Dialog
    var editFront by remember { mutableStateOf("") }
    var editBack by remember { mutableStateOf("") }

    // Mendapatkan kartu yang sedang aktif
    // Menggunakan getOrNull untuk keamanan jika index out of bounds saat delete
    val currentCard = cardList.getOrNull(pagerState.currentPage)

    Scaffold(
        containerColor = BgColor,
        topBar = {
            DetailTopBar(
                currentIndex = pagerState.currentPage,
                totalCards = cardList.size,
                onClose = { navController.popBackStack() },
                onEditClick = {
                    currentCard?.let { card ->
                        editFront = card.front // Asumsi Domain Model Card punya property 'front'
                        editBack = card.back   // Asumsi Domain Model Card punya property 'back'
                        showEditDialog = true
                    }
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
                        val prev = (pagerState.currentPage - 1).coerceAtLeast(0)
                        pagerState.animateScrollToPage(prev)
                    }
                },
                onNextClick = {
                    scope.launch {
                        val next = (pagerState.currentPage + 1).coerceAtMost(cardList.lastIndex)
                        pagerState.animateScrollToPage(next)
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
                pageSpacing = 16.dp,
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                // Guard agar tidak out of bounds saat list berubah cepat
                if (pageIndex < cardList.size) {
                    AiCardContentView(cardList[pageIndex])
                }
            }
        }
    }

    // --- DELETE DIALOG ---
    if (showDeleteDialog && currentCard != null) {
        DeleteConfirmDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                // Panggil ViewModel untuk hapus di API & Local
                viewModel.deleteCard(currentCard.id)
                showDeleteDialog = false
                // Logic untuk handle pager index setelah delete ditangani oleh reactivity compose
            }
        )
    }

    // --- EDIT DIALOG ---
    if (showEditDialog && currentCard != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Card", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editFront,
                        onValueChange = { editFront = it },
                        label = { Text("Front Side") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editBack,
                        onValueChange = { editBack = it },
                        label = { Text("Back Side") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Panggil ViewModel untuk update API & Local
                        viewModel.updateCard(currentCard.id, editFront, editBack)
                        showEditDialog = false
                    }
                ) {
                    Text("Save", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// 5. Ubah parameter 'AiCard' (DTO) menjadi 'Card' (Domain Model)
@Composable
fun AiCardContentView(card: Card) {
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
                text = card.front, // Domain Model Property
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            HorizontalDivider(
                thickness = 4.dp,
                color = BgColor,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp))
            )

            Spacer(Modifier.height(32.dp))

            SideLabel(text = "Back Side")
            Spacer(Modifier.height(24.dp))

            Text(
                text = card.back, // Domain Model Property
                fontSize = 16.sp,
                color = TextDark.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}