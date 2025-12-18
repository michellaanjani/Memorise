package com.mobile.memorise.ui.screen.create.ai

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mobile.memorise.R
import com.mobile.memorise.domain.model.Card
import com.mobile.memorise.navigation.MainRoute
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import com.mobile.memorise.util.Resource
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
    // 1. Validasi deckId & Load Data
    LaunchedEffect(deckId) {
        if (deckId.isNotBlank()) {
            viewModel.loadDraft(deckId)
        }
    }

    // 2. Observe States
    val draftState by viewModel.draftState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    val deckName by viewModel.currentDeckName.collectAsState()

    val isProcessing = saveState is Resource.Loading || saveState is Resource.Success

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 3. Local UI States (Dialogs & Popups)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCardId by remember { mutableStateOf<String?>(null) }

    // Popup States
    var showSaveSuccessPopup by remember { mutableStateOf(false) }
    var showDeleteSuccessPopup by remember { mutableStateOf(false) }

    // 4. Handle Save Result
    LaunchedEffect(saveState) {
        if (saveState is Resource.Success) {
            showSaveSuccessPopup = true
            delay(1500)
            navController.navigate(MainRoute.Home.route) {
                popUpTo(MainRoute.Home.route) { inclusive = false }
                launchSingleTop = true
            }
            viewModel.resetStates()
        } else if (saveState is Resource.Error) {
            val msg = saveState?.message ?: ""
            if (msg.contains("NOT_DRAFT_DECK", ignoreCase = true)) {
                showSaveSuccessPopup = true
                delay(1000)
                navController.navigate(MainRoute.Home.route) {
                    popUpTo(MainRoute.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
                viewModel.resetStates()
            } else {
                snackbarHostState.showSnackbar(msg)
            }
        }
    }

    // 5. Handle Delete Result
    LaunchedEffect(deleteState) {
        if (deleteState is Resource.Success) {
            showDeleteSuccessPopup = true
            delay(1500)
            showDeleteSuccessPopup = false
            viewModel.resetDeleteState()
        }
        if (deleteState is Resource.Error) {
            snackbarHostState.showSnackbar(deleteState.message ?: "Failed to delete")
            viewModel.resetDeleteState()
        }
    }

    if (draftState is Resource.Loading && deckName.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    if (draftState is Resource.Error) {
        val errorMsg = draftState.message ?: "Terjadi kesalahan"
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Gagal memuat draft", fontWeight = FontWeight.Bold)
                Text(errorMsg, color = Color.Red, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBackClick) { Text("Kembali") }
            }
        }
        return
    }

    val draftData = (draftState as? Resource.Success)?.data
    val cards = draftData?.cards ?: emptyList()

    // 🔥 PERBAIKAN PENTING DI SINI:
    // Pastikan ID tidak kosong. Jika kosong, gunakan ID dari Navigasi.
    val currentDeckId = draftData?.deck?.id?.takeIf { it.isNotBlank() } ?: deckId

    Scaffold(
        containerColor = BgColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (deckName.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Nama deck tidak boleh kosong") }
                            return@Button
                        }
                        // Kirim currentDeckId yang sudah divalidasi
                        viewModel.saveDraft(currentDeckId, null, deckName)
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardWhite,
                        contentColor = PrimaryBlue,
                        // Tambahkan dua baris ini agar saat loading (disabled) warnanya tetap muncul
                        disabledContainerColor = CardWhite,
                        disabledContentColor = PrimaryBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (saveState is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("Saving...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    } else if (saveState is Resource.Success) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("Changes saved!", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Save Deck", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
            // HEADER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                        .background(HeaderBlue)
                        .padding(bottom = 20.dp)
                ) {
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logm),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            // DECK NAME INPUT
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
                        value = deckName,
                        onValueChange = { viewModel.updateDeckNameLocal(it) },
                        singleLine = true,
                        enabled = !isProcessing,
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
                            unfocusedTextColor = TextDark,
                            disabledContainerColor = Color.White,
                            disabledTextColor = Color.Gray
                        ),
                        placeholder = { Text("Enter deck name", color = TextGray) }
                    )
                }
            }

            item {
                Text(
                    text = "Cards generated — ${cards.size} cards",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // CARD LIST
            itemsIndexed(items = cards, key = { _, card -> card.id }) { index, card ->
                MinimalAppleCardItem(
                    card = card,
                    onClick = {
                        if (!isProcessing) {
                            navController.navigate(MainRoute.AiCardDetail.createRoute(index))
                        }
                    },
                    onEditClick = {
                        if (!isProcessing) {
                            navController.navigate(MainRoute.AiEditCard.createRoute(card.id))
                        }
                    },
                    onDeleteClick = {
                        if (!isProcessing) {
                            selectedCardId = card.id
                            showDeleteDialog = true
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    // ================= SAVE SUCCESS POPUP =================
    if (showSaveSuccessPopup) {
        val alphaAnim by animateFloatAsState(if (showSaveSuccessPopup) 1f else 0f, tween(250), label = "alpha")
        val scaleAnim by animateFloatAsState(if (showSaveSuccessPopup) 1f else 0.95f, tween(250), label = "scale")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(32.dp)
                    .graphicsLayer { scaleX = scaleAnim; scaleY = scaleAnim }
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
                        Icon(Icons.Default.Check, null, tint = Color(0xFF166534), modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Success!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Deck saved successfully.", fontSize = 14.sp, color = TextGray, textAlign = TextAlign.Center)
                }
            }
        }
    }

    // 🔥 ================= DELETE SUCCESS POPUP ================= 🔥
    if (showDeleteSuccessPopup) {
        val alphaAnim by animateFloatAsState(if (showDeleteSuccessPopup) 1f else 0f, tween(250), label = "alpha")
        val scaleAnim by animateFloatAsState(if (showDeleteSuccessPopup) 1f else 0.95f, tween(250), label = "scale")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(32.dp)
                    .graphicsLayer { scaleX = scaleAnim; scaleY = scaleAnim }
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
                        Icon(Icons.Default.Check, null, tint = Color(0xFF166534), modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Success!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Card deleted successfully.", fontSize = 14.sp, color = TextGray, textAlign = TextAlign.Center)
                }
            }
        }
    }

    // ================= DELETE DIALOG =================
    if (showDeleteDialog && selectedCardId != null) {
        DeleteConfirmDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                selectedCardId?.let { cardId ->
                    viewModel.deleteDraftCard(currentDeckId, cardId)
                }
                showDeleteDialog = false
                selectedCardId = null
            }
        )
    }
}

// --- KOMPONEN ITEM CARD DENGAN TITIK TIGA ---
@Composable
fun MinimalAppleCardItem(
    card: Card,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
                Text(
                    text = card.front,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = card.back,
                    fontSize = 13.sp,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFFFF9800) // Orange
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit", fontSize = 14.sp, color = TextDark)
                            }
                        },
                        onClick = {
                            expanded = false
                            onEditClick()
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete", fontSize = 14.sp, color = TextDark)
                            }
                        },
                        onClick = {
                            expanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}