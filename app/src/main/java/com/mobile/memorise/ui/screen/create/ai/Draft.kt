package com.mobile.memorise.ui.screen.create.ai

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mobile.memorise.R
import com.mobile.memorise.domain.model.Card
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- COLORS ---
private val BgColor = Color(0xFFF5F5F7)
private val HeaderBlue = Color(0xFF0961F5)
private val CardWhite = Color.White
private val TextDark = Color(0xFF1A1C24)
private val TextGray = Color(0xFF7A7A7A)
private val PrimaryBlue = HeaderBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGeneratedDraftScreen(
    navController: NavController,
    deckId: String,
    onBackClick: () -> Unit,
    viewModel: AiViewModel = hiltViewModel()
) {
    // 1. Load Draft Data saat masuk screen pertama kali
    LaunchedEffect(deckId) {
        viewModel.loadDraft(deckId)
    }

    // 2. Observe States
    val draftData by viewModel.draftSession.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 3. Local UI States
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCardId by remember { mutableStateOf<String?>(null) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    // State untuk Nama Deck
    var deckNameState by remember { mutableStateOf("") }

    // Inisialisasi nama deck saat data selesai dimuat dari API
    // Kita gunakan LaunchedEffect key(draftData) agar update hanya jika nama lokal masih kosong
    LaunchedEffect(draftData) {
        if (draftData != null && deckNameState.isEmpty()) {
            deckNameState = draftData!!.deck.name
        }
    }

    // --- LOADING STATE (Full Screen saat awal) ---
    if (uiState is AiUiState.Loading && draftData == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    // --- ERROR STATE ---
    if (uiState is AiUiState.Error) {
        val errorMsg = (uiState as AiUiState.Error).message
        // Tampilkan error jika data benar-benar kosong
        if (draftData == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gagal memuat draft", fontWeight = FontWeight.Bold)
                    Text(errorMsg, color = Color.Red, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onBackClick) { Text("Kembali") }
                }
            }
            return
        }
    }

    val cards = draftData?.cards ?: emptyList()

    Scaffold(
        containerColor = BgColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Save Button Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardWhite,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(46.dp)
                ) {
                    val isSaving = uiState is AiUiState.Loading

                    TextButton(
                        onClick = {
                            if (deckNameState.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Nama deck tidak boleh kosong") }
                                return@TextButton
                            }

                            if (!isSaving) {
                                viewModel.saveDeck {
                                    showSuccessPopup = true
                                    scope.launch {
                                        delay(1500) // Tahan sebentar biar user lihat popup sukses
                                        // Navigasi ke Home, hapus backstack agar tidak bisa 'Back' ke draft yang sudah disave
                                        navController.navigate("home_screen") {
                                            popUpTo("home_screen") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlue)
                    ) {
                        if (isSaving) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryBlue)
                                Spacer(Modifier.width(8.dp))
                                Text("Menyimpan...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Text("Save Deck", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ================= HEADER =================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                        .background(HeaderBlue)
                        .padding(bottom = 20.dp)
                ) {
                    // TOP BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        Text(
                            text = "Draft Preview",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // LOGO / ILLUSTRATION
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logm), // Pastikan resource ada
                            contentDescription = "App Logo",
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            // ============ DECK NAME INPUT ============
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Deck Name", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Text(" *", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = deckNameState,
                        onValueChange = {
                            deckNameState = it
                            // PENTING: Update ke ViewModel agar saat save, nama baru yang dipakai
                            viewModel.updateLocalDeckName(it)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = PrimaryBlue,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        placeholder = { Text("Enter deck name", color = TextGray) }
                    )
                }
            }

            // ============ INFO COUNT ============
            item {
                Text(
                    text = "Cards generated — ${cards.size} cards",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // ================= CARD LIST =================
            itemsIndexed(items = cards, key = { _, card -> card.id }) { index, card ->
                MinimalAppleCardItem(
                    card = card,
                    onClick = {
                        // Navigasi ke detail card (pastikan route sesuai graph kamu)
                        // Menggunakan index agar pager bisa langsung ke posisi kartu ini
                        navController.navigate("ai_card_detail/$index")
                    },
                    onEditClick = {
                        navController.navigate("ai_card_detail/$index")
                    },
                    onDeleteClick = {
                        selectedCardId = card.id
                        showDeleteDialog = true
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    // ================= SUCCESS POPUP =================
    if (showSuccessPopup) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 100.dp)
                    .background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    "Deck Saved Successfully!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }

    // ================= DELETE DIALOG =================
    if (showDeleteDialog && selectedCardId != null) {
        DeleteConfirmDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                selectedCardId?.let { viewModel.deleteCard(it) }
                showDeleteDialog = false
                selectedCardId = null
            }
        )
    }
}

@Composable
fun MinimalAppleCardItem(
    card: Card,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardWhite,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Front
                Text(
                    text = card.front,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Back
                Text(
                    text = card.back,
                    fontSize = 13.sp,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}