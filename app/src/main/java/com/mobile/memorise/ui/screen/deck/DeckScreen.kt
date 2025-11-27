package com.mobile.memorise.ui.screen.deck

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.mobile.memorise.ui.theme.* // Pastikan import Theme.kt ada

// 1. Model Data Deck (Sesuai JSON)
@Serializable
data class DeckItemData(
    @SerialName("deck_name") val deckName: String,
    @SerialName("card_count") val cardCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckScreen(
    folderName: String,
    onBackClick: () -> Unit, // Callback untuk tombol back
    onDeckClick: (String) -> Unit = {} // Callback saat deck diklik
) {
    val context = LocalContext.current
    var deckList by remember { mutableStateOf(listOf<DeckItemData>()) }

    // Load JSON deckname.json
    LaunchedEffect(Unit) {
        try {
            val jsonString = context.assets.open("deckname.json").bufferedReader().use { it.readText() }
            deckList = Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            // List tetap kosong jika error/file tidak ditemukan
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FB), // Background abu muda
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1A1C24)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FB)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (deckList.isEmpty()) {
                // --- 3. TAMPILAN EMPTY STATE (Mirip Folder) ---
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.empty), // Icon orang empty
                            contentDescription = "No Decks",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No Decks yet!", // Ganti folder jadi Deck
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C24)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Let’s create a deck for you to learn!",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // --- LIST DECK ---
                LazyColumn(
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(deckList) { deck ->
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

@Composable
fun DeckItemView(
    data: DeckItemData,
    onClick: () -> Unit
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
            // 2. Icon Deck (Drawable: deck.xml)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEDE7F6)), // Background ungu muda (sesuai gambar referensi)
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.deck), // Icon Deck kamu
                    contentDescription = null,
                    tint = Color(0xFF5E35B1), // Warna tint ungu tua (opsional, sesuaikan icon asli)
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Deck
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.deckName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1C24)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Menampilkan jumlah kartu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.deck), // Icon kecil (opsional)
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