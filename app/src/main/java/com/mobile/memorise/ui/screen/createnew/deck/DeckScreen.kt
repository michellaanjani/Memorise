package com.mobile.memorise.ui.screen.createnew.deck

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
import com.mobile.memorise.domain.model.Deck
import com.mobile.memorise.navigation.MainRoute
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import com.mobile.memorise.ui.screen.main.CreateOptionItem
import com.mobile.memorise.ui.theme.*

// --- Definisi Warna Lokal (Agar tidak error TextBlack) ---
private val TextBlack = Color(0xFF1F2937)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckScreen(
    folderName: String,
    folderId: String,
    onBackClick: () -> Unit,
    // PERBAIKAN: Callback harus menerima Object Deck agar bisa mengirim ID dan Nama
    onDeckClick: (Deck) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    viewModel: DeckViewModel = hiltViewModel()
) {
    val deckList = viewModel.decks

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    LaunchedEffect(folderId) {
        viewModel.loadDecks(folderId)
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FB),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = folderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A1C24)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1A1C24)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FB)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = WhitePurple,
                contentColor = BrightBlue,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Deck",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->

        Column(modifier = Modifier.fillMaxSize()) {

            if (deckList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.empty),
                            contentDescription = "No Decks",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No Decks yet!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C24)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Let’s create a deck inside this folder!",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + 24.dp,
                        bottom = innerPadding.calculateBottomPadding() + 100.dp,
                        start = 24.dp,
                        end = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(deckList) { deck ->
                        DeckItemView(
                            data = deck,
                            // PERBAIKAN: Mengirim seluruh objek Deck ke callback
                            onClick = { onDeckClick(deck) },
                            onMoveClicked = {
                                onNavigate(MainRoute.MoveDeck.createRoute(deck.id))
                            },
                            onEditClicked = {
                                onNavigate(MainRoute.EditDeck.createRoute(deck.id))
                            },
                            onDeleteClicked = {
                                deckToDelete = deck
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                CreateBottomSheetContent(
                    folderId = folderId,
                    onNavigate = { route ->
                        showBottomSheet = false
                        onNavigate(route)
                    }
                )
            }
        }

        if (showDeleteDialog && deckToDelete != null) {
            DeleteConfirmDialog(
                onCancel = {
                    showDeleteDialog = false
                    deckToDelete = null
                },
                onDelete = {
                    viewModel.deleteDeck(deckToDelete!!.id)
                    showDeleteDialog = false
                    deckToDelete = null
                }
            )
        }
    }
}

@Composable
fun DeckItemView(
    data: Deck,
    onClick: () -> Unit,
    onMoveClicked: () -> Unit = {},
    onEditClicked: () -> Unit = {},
    onDeleteClicked: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEDE7F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.deck),
                    contentDescription = null,
                    tint = Color(0xFF5E35B1),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1C24)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.deck),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${data.cardCount} Cards",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
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
                    // --- MOVE ---
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.move), "Move", tint = Color(0xFF0961F5))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Move", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = {
                            expanded = false
                            onMoveClicked()
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.3f))

                    // --- EDIT ---
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFFF9800))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = {
                            expanded = false
                            onEditClicked()
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.3f))

                    // --- DELETE ---
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = {
                            expanded = false
                            onDeleteClicked()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CreateBottomSheetContent(
    folderId: String,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "Create New",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cdeck),
            title = "Create Deck",
            subtitle = "Organize flashcard into decks",
            onClick = {
                onNavigate(MainRoute.CreateDeck.createDeckWithFolder(folderId))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreateOptionItem(
            painter = painterResource(id = R.drawable.cai),
            title = "Generate With AI",
            subtitle = "Create cards with AI",
            onClick = {
                onNavigate(MainRoute.AiGeneration.route)
            }
        )
    }
}