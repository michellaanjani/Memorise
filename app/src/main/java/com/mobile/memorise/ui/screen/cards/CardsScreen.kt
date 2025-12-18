package com.mobile.memorise.ui.screen.cards

import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import com.mobile.memorise.ui.screen.createnew.deck.DeckViewModel
import com.mobile.memorise.ui.theme.BrightBlue
import com.mobile.memorise.ui.theme.TextBlack
import com.mobile.memorise.ui.theme.WhitePurple
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// --- 1. DATA MODELS ---
@Serializable
data class CardItemData(
    val id: String,
    val front: String,
    val back: String,
    val explanation: String? = null
)

// --- 2. COLORS ---
private val BlueHeader = Color(0xFF5391F5)
private val OrangeButton = Color(0xFFFF851B)
private val BlueButton = Color(0xFF4285F4)
private val BgColor = Color(0xFFF8F9FB)
private val TextGray = Color(0xFF757575)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    deckId: String,
    deckName: String,
    onBackClick: () -> Unit,
    onStudyClick: () -> Unit,
    onQuizClick: () -> Unit,
    onAddCardClick: () -> Unit = {},
    onCardClick: (String, Int) -> Unit,
    onEditCardClick: (String, Int) -> Unit,
    // Menggunakan DeckViewModel (Hilt)
    deckViewModel: DeckViewModel = hiltViewModel()
) {
    // --- STATE MANAGEMENT ---
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // State dari ViewModel
    val rawCards = deckViewModel.cards
    val isLoading = deckViewModel.areCardsLoading // 🔥 Mengambil status loading
    val errorMessage = deckViewModel.errorMessage

    // State UI Lokal
    var showQuizAlert by remember { mutableStateOf(false) }
    var showStudyAlert by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Gunakan ID untuk delete agar lebih aman daripada index
    var cardIdToDelete by remember { mutableStateOf<String?>(null) }

    // 1. Load data saat pertama kali dibuka
    LaunchedEffect(deckId) {
        deckViewModel.loadCards(deckId)
    }

    // 2. Tampilkan Error Snackbar jika ada masalah jaringan/API
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            deckViewModel.clearError()
        }
    }

    // 3. Mapping data Domain ke UI Model
    val cardList = remember(rawCards.size, rawCards) {
        rawCards.map {
            CardItemData(
                id = it.id,
                front = it.front,
                back = it.back
            )
        }
    }

    Scaffold(
        containerColor = BgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    // Opsional: Tampilkan nama Deck di TopBar agar tetap terlihat saat Empty State
                    // Text(text = deckName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                },
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.memorisey),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(20.dp)
                            .padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCardClick,
                containerColor = WhitePurple,
                contentColor = BrightBlue,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Card", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->

        // 🔥 STRUKTUR UTAMA (Box)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                // 1. TAMPILAN LOADING
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BlueButton)
                }

            } else if (cardList.isEmpty()) {
                // 2. TAMPILAN KOSONG (EMPTY STATE)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.empty),
                            contentDescription = "No Cards",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No Cards yet!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C24)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Let’s create a card for you to learn!",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            } else {
                // 3. TAMPILAN DATA (HEADER + LIST)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 0.dp, // Reset top padding karena sudah di handle Scaffold
                        bottom = 100.dp, // Space untuk FAB
                        start = 24.dp,
                        end = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // A. Judul Deck
                    item {
                        Text(
                            text = deckName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // B. Summary Card (Kotak Biru)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF4B89F3), Color(0xFF3366FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${cardList.size}",
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Cards in deck",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // C. Tombol Actions (Study & Quiz)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Tombol Study
                            Button(
                                onClick = onStudyClick,
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeButton)
                            ) {
                                Text("Study cards", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }

                            // Tombol Quiz
                            Button(
                                onClick = {
                                    if (cardList.size < 3) {
                                        showQuizAlert = true
                                    } else {
                                        onQuizClick()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueButton)
                            ) {
                                Text("Start Quiz", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // D. Header List
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cards in deck (${cardList.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }

                    // E. List Item Cards
                    itemsIndexed(cardList) { index, card ->
                        CardItemView(
                            card = card,
                            onClick = {
                                val jsonList = Json.encodeToString(cardList)
                                val encodedJson = Uri.encode(jsonList)
                                onCardClick(encodedJson, index)
                            },
                            onEditClick = {
                                val jsonList = Json.encodeToString(cardList)
                                val encodedJson = Uri.encode(jsonList)
                                onEditCardClick(encodedJson, index)
                            },
                            onDeleteClick = {
                                cardIdToDelete = card.id
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        // --- DIALOGS ---

        if (showQuizAlert) {
            ValidationDialog(
                iconRes = R.drawable.threecard, // Pastikan drawable ini ada
                title = "Minimal 3 Kartu Diperlukan",
                description = "Anda memerlukan minimal 3 kartu untuk memulai mode Quiz. Silakan tambah kartu.",
                onDismiss = { showQuizAlert = false }
            )
        }

        if (showStudyAlert) {
            ValidationDialog(
                iconRes = R.drawable.empty,
                title = "Kartu Kosong",
                description = "Anda harus memiliki minimal 1 kartu untuk mulai belajar. Yuk buat kartu barumu!",
                onDismiss = { showStudyAlert = false }
            )
        }

        // --- CONFIRM DELETE DIALOG ---
        if (showDeleteDialog) {
            DeleteConfirmDialog(
                onCancel = {
                    showDeleteDialog = false
                    cardIdToDelete = null
                },
                onDelete = {
                    cardIdToDelete?.let { id ->
                        deckViewModel.deleteCard(id)
                    }
                    showDeleteDialog = false
                    cardIdToDelete = null
                }
            )
        }
    }
}

@Composable
fun CardItemView(
    card: CardItemData,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.front,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = card.back,
                    fontSize = 14.sp,
                    color = TextGray,
                    maxLines = 3,
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
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFFF9800))
                                Spacer(Modifier.width(8.dp))
                                Text("Edit", color = TextBlack)
                            }
                        },
                        onClick = {
                            expanded = false
                            onEditClick()
                        }
                    )

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                Spacer(Modifier.width(8.dp))
                                Text("Delete", color = TextBlack)
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

// --- KOMPONEN DIALOG REUSABLE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidationDialog(
    iconRes: Int,
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Icon Alert",
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C24),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueButton)
                ) {
                    Text(
                        text = "Mengerti",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}