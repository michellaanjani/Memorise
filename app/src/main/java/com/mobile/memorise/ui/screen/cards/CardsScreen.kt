package com.mobile.memorise.ui.screen.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import com.mobile.memorise.ui.theme.BrightBlue
import com.mobile.memorise.ui.theme.WhitePurple
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.compose.ui.window.Dialog

// --- 1. DATA MODELS ---
@Serializable
data class CardListResponse(
    @SerialName("card_count") val count: Int = 0,
    @SerialName("cards") val cards: List<CardItemData> = emptyList()
)

@Serializable
data class CardItemData(
    val id: Int,
    val front: String,
    val back: String
)

// --- 2. COLORS (Hardcoded to match screenshot) ---
private val BlueHeader = Color(0xFF5391F5) // Warna Biru Header
private val OrangeButton = Color(0xFFFF851B) // Warna Orange Tombol
private val BlueButton = Color(0xFF4285F4)   // Warna Biru Tombol
private val BgColor = Color(0xFFF8F9FB)      // Background Abu Muda
private val TextDark = Color(0xFF1A1C24)     // Hitam Teks
private val TextGray = Color(0xFF757575)     // Abu Teks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    deckName: String,
    onBackClick: () -> Unit,
    onStudyClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onAddCardClick: () -> Unit = {}  // Parameter diminta tapi belum dipakai di UI
) {
    val context = LocalContext.current
    var cardData by remember { mutableStateOf(CardListResponse()) }

    // 1. STATE UNTUK POPUP
    var showQuizAlert by remember { mutableStateOf(false) }

    // Load JSON cards.json
    LaunchedEffect(Unit) {
        try {
            val jsonString = context.assets.open("cards.json").bufferedReader().use { it.readText() }
            cardData = Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            TopAppBar(
                title = {}, // Title kosong karena kita pakai custom header di body
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                },
                actions = {
                    // LOGO MEMORISE DI KANAN
                    Image(
                        painter = painterResource(id = R.drawable.memorisey),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit, // Menjaga rasio asli
                        modifier = Modifier
                            .height(28.dp) // Tinggi fix, lebar menyesuaikan rasio
                            .padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor
                )
            )
        },
        // --- TAMBAHAN 1: FLOATING ACTION BUTTON ---
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCardClick, // Panggil action saat diklik
                containerColor = WhitePurple, // Pastikan warna ini ada di Theme.kt
                contentColor = BrightBlue,  // Icon warna putih
                shape = CircleShape,         // Bentuk bulat penuh
                modifier = Modifier.padding(bottom = 16.dp) // Jarak sedikit dari bawah agar tidak nempel tepi
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Deck",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->

        // Gunakan LazyColumn untuk seluruh konten agar bisa discroll
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 100.dp, // Padding bawah list
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- ITEM 1: JUDUL DECK ---
            item {
                Text(
                    text = deckName, // "Agile for Methodology"
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // --- ITEM 2: SUMMARY CARD (BIRU BESAR) ---
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
                            text = "${cardData.count}", // Angka Besar
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

            // --- ITEM 3: TOMBOL ACTION (STUDY & QUIZ) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tombol Study (Orange)
                    Button(
                        onClick = {
                            // 1. Serialize List Cards ke JSON String
                            val jsonList = Json.encodeToString(cardData.cards)
                            // 2. Encode agar aman untuk URL (menghindari error karakter khusus)
                            val encodedJson = Uri.encode(jsonList)
                            // 3. Panggil Callback navigasi
                            onStudyClick(encodedJson)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeButton)
                    ) {
                        Text(
                            "Study cards",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold)
                    }

                    // Tombol Quiz (Biru) - UPDATE DISINI
                    Button(
                        onClick = {
                            // 2. LOGIKA CEK JUMLAH KARTU
                            if (cardData.cards.size < 3) {
                                showQuizAlert = true // Munculkan Popup
                            } else {
                                // 1. Serialize List Cards ke JSON String
                                val jsonList = Json.encodeToString(cardData.cards)
                                // 2. Encode agar aman untuk URL (menghindari error karakter khusus)
                                val encodedJson = Uri.encode(jsonList)
                                // 3. Panggil Callback navigasi
                                onQuizClick(encodedJson)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueButton)
                    ) {
                        Text(
                            "Start Quiz",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // --- ITEM 4: SECTION HEADER LIST ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cards in deck (${cardData.cards.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            // --- ITEM 5: LIST KARTU ---
            items(cardData.cards) { card ->
                CardItemView(card)
            }
        }
        // 3. PANGGIL DIALOG DI SINI (Di luar LazyColumn tapi di dalam Scaffold)
        if (showQuizAlert) {
            MinimalCardsDialog(
                onDismiss = { showQuizAlert = false }
            )
        }
    }
}

@Composable
fun CardItemView(card: CardItemData) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Bayangan tipis
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Konten Teks
            Column(modifier = Modifier.weight(1f)) {
                // Front (Pertanyaan)
                Text(
                    text = card.front,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Back (Jawaban)
                Text(
                    text = card.back,
                    fontSize = 14.sp,
                    color = TextGray,
                    lineHeight = 20.sp,
                    maxLines = 3, // Batasi 3 baris agar rapi
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Icon Titik Tiga (More)
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { /* Handle Edit/Delete */ }
            )
        }
    }
}

// --- 4. KOMPONEN UI POPUP (Simpan di bagian bawah file CardsScreen.kt) ---

@Composable
fun MinimalCardsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp), // Sudut tumpul sesuai gambar
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // A. Icon Lingkaran Biru Muda
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)), // Warna biru sangat muda
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.threecard), // Pastikan icon ada
                        contentDescription = "Icon Cards",
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // B. Judul
                Text(
                    text = "Minimal 3 Kartu Diperlukan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C24),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // C. Deskripsi
                Text(
                    text = "Anda memerlukan minimal 3 kartu untuk memulai mode Quiz. Silakan tambah kartu.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // D. Tombol Tutup
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueButton)
                ) {
                    Text(
                        text = "Tutup",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}