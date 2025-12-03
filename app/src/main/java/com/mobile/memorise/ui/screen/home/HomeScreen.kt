package com.mobile.memorise.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import com.mobile.memorise.ui.theme.* // Pastikan theme terimport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- 1. DATA MODEL (SESUAI JSON) ---

@Serializable
data class HomeData(
    @SerialName("folder_list") val folders: List<FolderItemData> = emptyList(),
    @SerialName("deck_list") val decks: List<DeckItemData> = emptyList()
)

@Serializable
data class FolderItemData(
    val name: String,
    val date: String,
    val deckCount: Int
)

@Serializable
data class DeckItemData(
    @SerialName("deck_name") val deckName: String,
    @SerialName("card_count") val cardCount: Int
)

// --- 2. SCREEN UTAMA ---

@Composable
fun HomeScreen(onFolderClick: (String) -> Unit, onDeckClick: (String) -> Unit) {
    val context = LocalContext.current
    // Menggunakan HomeData sebagai state utama (bukan list terpisah)
    var homeData by remember { mutableStateOf(HomeData()) }

    // Load JSON
    LaunchedEffect(Unit) {
        try {
            // Pastikan nama file JSON sesuai (misal: home_data.json atau foldername.json)
            val jsonString = context.assets.open("foldeck.json").bufferedReader().use { it.readText() }
            // Decode ke object HomeData
            homeData = Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB)),
        // Padding bawah agar item terakhir tidak tertutup Nav Bar HP
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        // ITEM 1: HEADER BIRU
        item {
            HeaderSection()
        }

        // ITEM 2: LOGIKA KONTEN
        // Cek apakah KEDUANYA kosong?
        if (homeData.folders.isEmpty() && homeData.decks.isEmpty()) {
            // --- TAMPILAN KOSONG (EMPTY STATE) ---
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp, bottom = 24.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.empty), // Pastikan icon ada
                        contentDescription = "No Data",
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No Data found!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Let’s create folders or decks to start!",
                        fontSize = 14.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // --- BAGIAN 1: FOLDER LIST ---
            if (homeData.folders.isNotEmpty()) {
                // Judul Folder
                item {
                    Text(
                        text = "Choice your Folder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
                    )
                }
                // List Folder Items
                items(homeData.folders) { folder ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        FolderItemView(
                            data = folder,
                            onClick = { onFolderClick(folder.name) }
                        )
                    }
                }
            }

            // --- BAGIAN 2: DECK LIST ---
            if (homeData.decks.isNotEmpty()) {
                // Judul Deck (Memisahkan antara Folder dan Deck)
                item {
                    Text(
                        text = "Recent Decks", // Judul untuk bagian Deck
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
                    )
                }
                // List Deck Items
                items(homeData.decks) { deck ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        DeckItemView(
                            data = deck,
                            onClick = { onDeckClick(deck.deckName) }
                        )
                    }
                }
            }
        }
    }
}

// --- 3. KOMPONEN PENDUKUNG (HEADER, ITEMS) ---

@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBlue)
            .statusBarsPadding() // PENTING: Agar konten turun di bawah status bar
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hi, Reynard",
                    color = White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Let's train your memory!",
                    color = White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                ImageActionCard(
                    drawableId = R.drawable.memorize,
                    modifier = Modifier.weight(1f).aspectRatio(1.83f)
                )
                ImageActionCard(
                    drawableId = R.drawable.learn,
                    modifier = Modifier.weight(1f).aspectRatio(1.83f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ImageActionCard(drawableId: Int, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = drawableId),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { }
    )
}

@Composable
fun FolderItemView(data: FolderItemData, onClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(FolderIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.folder), contentDescription = null, tint = Color.Unspecified)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = data.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextBlack)
                Text(text = data.date, fontSize = 12.sp, color = TextGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${data.deckCount} decks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrightBlue)
            }
            // 5. Menu Titik Tiga (Edit & Delete)
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
}

@Composable
fun DeckItemView(data: DeckItemData, onClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEDE7F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(id = R.drawable.deck), contentDescription = null, tint = Color(0xFF5E35B1), modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = data.deckName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C24))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.deck), contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${data.cardCount} Cards", fontSize = 12.sp, color = Color.Gray)
                }
            }
            // 5. Menu Titik Tiga (Edit & Delete)
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
}