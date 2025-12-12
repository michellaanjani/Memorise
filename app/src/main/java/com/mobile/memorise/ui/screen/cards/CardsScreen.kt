package com.mobile.memorise.ui.screen.cards

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog as Dialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import com.mobile.memorise.R
import com.mobile.memorise.ui.theme.BrightBlue
import com.mobile.memorise.ui.theme.TextBlack
import com.mobile.memorise.ui.theme.WhitePurple
import kotlinx.serialization.SerialName
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
private val TextDark = Color(0xFF1A1C24)
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
    onCardClick: (String, Int) -> Unit, // <--- TAMBAHAN BARU (Kirim JSON list & Index)
    onEditCardClick: (String, Int) -> Unit,
    deckRemoteViewModel: com.mobile.memorise.ui.viewmodel.DeckRemoteViewModel = androidx.hilt.navigation.compose.hiltViewModel()


) {
    val context = LocalContext.current
    val cardsState by deckRemoteViewModel.cardsState.collectAsState()

    // State untuk Popup
    var showQuizAlert by remember { mutableStateOf(false) }
    var showStudyAlert by remember { mutableStateOf(false) } // Popup baru untuk Study

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(deckId) {
        deckRemoteViewModel.loadCards(deckId)
    }

    val cardList: List<CardItemData> = when (val result = cardsState) {
        is com.mobile.memorise.util.Resource.Success -> {
            result.data?.map { CardItemData(id = it.id, front = it.front, back = it.back, explanation = it.notes) } ?: emptyList()
        }
        else -> emptyList()
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = {},
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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Deck", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 100.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Judul
            item {
                Text(
                    text = deckName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // 2. Summary Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BlueHeader),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${deckRemoteViewModel.cardsState.value.data?.size ?: 0}", // Using cards state
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

            // 3. Tombol Actions (Study & Quiz)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tombol Study (Orange)
                    Button(
                        onClick = {
                            // VALIDASI: Minimal 1 Kartu
                            if (cardList.isEmpty()) {
                                showStudyAlert = true
                            } else {
                                onStudyClick()
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeButton)
                    ) {
                        Text("Study cards", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    // Tombol Quiz (Biru)
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

            // --- LOGIKA TAMPILAN LIST VS EMPTY STATE ---

            if (cardList.isNotEmpty()) {
                // A. Jika Ada Kartu: Tampilkan Header List & Item Kartu
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cards in deck (${cardList.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
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

                            // Navigate ke EditCardScreen
                                onEditCardClick(encodedJson, index)   // ← ini yang benar
                        },
                        onDeleteClick = {
                            selectedIndex = index   // simpan index kartu
                            showDeleteDialog = true // buka dialog
                        }
                    )
                }


            } else {
                // B. Jika KOSONG: Tampilkan Empty State UI (Sesuai Request)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp), // Jarak dari tombol
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.empty), // Pastikan file ini ada
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
                }
            }
        }

        // --- DIALOG POPUPS ---

        // Dialog untuk Quiz (Minimal 3)
        if (showQuizAlert) {
            ValidationDialog(
                iconRes = R.drawable.threecard, // Icon 3 kartu
                title = "Minimal 3 Kartu Diperlukan",
                description = "Anda memerlukan minimal 3 kartu untuk memulai mode Quiz. Silakan tambah kartu.",
                onDismiss = { showQuizAlert = false }
            )
        }

        // Dialog untuk Study (Minimal 1)
        if (showStudyAlert) {
            ValidationDialog(
                iconRes = R.drawable.empty, // Bisa pakai icon empty atau icon single card
                title = "Kartu Kosong",
                description = "Anda harus memiliki minimal 1 kartu untuk mulai belajar. Yuk buat kartu barumu!",
                onDismiss = { showStudyAlert = false }
            )
        }
    }
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                val targetId = cardList.getOrNull(selectedIndex)?.id
                if (targetId != null) {
                    deckRemoteViewModel.deleteCard(targetId, deckId)
                }
                showDeleteDialog = false
            }
        )
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
                    onDismissRequest = { expanded = false }
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
                // Icon Lingkaran Biru Muda
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

                // Judul
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C24),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Deskripsi
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Tutup
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueButton)
                ) {
                    Text(
                        text = "Mengerti", // Atau "Tutup"
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}