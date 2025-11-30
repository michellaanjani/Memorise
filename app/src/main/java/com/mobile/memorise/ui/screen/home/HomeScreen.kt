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
import androidx.compose.ui.text.style.TextAlign // Tambahan Import
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.mobile.memorise.ui.theme.*

@Serializable
data class FolderItemData(
    val name: String,
    val date: String,
    val deckCount: Int
)

@Composable
fun HomeScreen(onFolderClick: (String) -> Unit) {
    val context = LocalContext.current
    var folderList by remember { mutableStateOf(listOf<FolderItemData>()) }

    LaunchedEffect(Unit) {
        try {
            val jsonString = context.assets.open("foldername.json").bufferedReader().use { it.readText() }
            folderList = Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- PERUBAHAN UTAMA ---
    // Gunakan LazyColumn sebagai container UTAMA (pengganti Column biasa)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB))
        // HAPUS contentPadding di sini agar Header Biru bisa Full Width
    ) {

        // ITEM 1: HEADER
        // Masukkan HeaderSection sebagai item pertama di LazyColumn
        item {
            HeaderSection()
        }

        // ITEM 2: LOGIKA KONTEN
        if (folderList.isEmpty()) {
            // --- TAMPILAN KOSONG (EMPTY STATE) ---
            item {
                // Kita butuh Box dengan height tertentu agar terlihat di tengah
                // atau biarkan scrollable biasa
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp, bottom = 24.dp) // Dorong ke bawah manual karena di dalam scroll
                        .padding(horizontal = 24.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.empty),
                        contentDescription = "No Data",
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No Folders yet!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack // Pastikan warna ini didefinisikan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Let’s make a card for you learn!",
                        fontSize = 14.sp,
                        color = TextGray, // Pastikan warna ini didefinisikan
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // --- TAMPILAN LIST FOLDER ---

            // Sub-Header "Choice your Folder"
            item {
                Text(
                    text = "Choice your Folder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack, // Pastikan warna ini didefinisikan
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
                    // Padding dipindah ke sini karena LazyColumn polosan
                )
            }


            // List Items
            items(folderList) { folder ->
                // Bungkus item dengan Box/Column untuk memberi Padding kiri-kanan
                // karena LazyColumn utama tidak punya padding (demi Header Full Width)
                Box(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    FolderItemView(
                        data = folder,
                        onClick = { onFolderClick(folder.name) }
                    )
                }
            }

            // Spacer bawah agar item terakhir tidak kepotong navigasi/bawah layar
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
// ... (Sisa kode HeaderSection, ImageActionCard, FolderItemView TETAP SAMA seperti sebelumnya)

@Composable
fun HeaderSection() {
    // 1. Gunakan Column sebagai pembungkus utama (bukan Box)
// 2. HAPUS .height(220.dp) agar biru bisa memanjang otomatis mengikuti isi
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBlue)
            .statusBarsPadding()
            .padding(24.dp) // Padding pinggir (kiri, kanan, atas)
        // Hapus padding bottom di sini, kita atur pakai Spacer di bawah agar lebih presisi
    ) {
        // --- BAGIAN HEADER (Teks & Icon) ---
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

        // Jarak antara Teks dan Gambar
        Spacer(modifier = Modifier.height(16.dp))

        // --- BAGIAN GAMBAR (RESPONSIVE) ---
        // Box ini menjaga agar di Tablet posisinya di tengah
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    // Ini kuncinya agar di Tablet tidak raksasa
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                ImageActionCard(
                    drawableId = R.drawable.memorize,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.83f)
                )

                ImageActionCard(
                    drawableId = R.drawable.learn,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.83f)
                )
            }
        }

        // --- JARAK BIRU DI BAWAH GAMBAR ---
        // Spacer ini memastikan warna biru tidak putus pas di bawah gambar
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ImageActionCard(
    drawableId: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = drawableId),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { /* Aksi klik disini */ }
    )
}

@Composable
fun FolderItemView(data: FolderItemData, onClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FolderIconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.folder),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextBlack
                )
                Text(
                    text = data.date,
                    fontSize = 12.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${data.deckCount} decks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrightBlue
                )
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextGray)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(White)
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

//Preview ukuran gambar
@Composable
fun ImageActionRow(
    ratio: Float // Kita buat ini dinamis agar bisa dites
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp) // Tambah padding biar rapi di preview
    ) {
        ImageActionCard(
            drawableId = R.drawable.memorize, // Pastikan drawable ini ada
            modifier = Modifier
                .weight(1f)
                .aspectRatio(ratio) // Gunakan nilai parameter di sini
        )

        ImageActionCard(
            drawableId = R.drawable.learn, // Pastikan drawable ini ada
            modifier = Modifier
                .weight(1f)
                .aspectRatio(ratio)
        )
    }
}

@Preview(showBackground = true, widthDp = 400, name = "Tes Perbandingan Rasio")
@Composable
fun RatioComparisonPreview() {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        // 1. Rasio Kotak (1:1)
        Text("Rasio 1f (Kotak 1:1)", modifier = Modifier.padding(start=16.dp))
        ImageActionRow(ratio = 1f)

        // 2. Rasio Standar Foto (4:3 -> 1.33)
        Text("Rasio 1.33f (4:3)", modifier = Modifier.padding(start=16.dp))
        ImageActionRow(ratio = 4f / 3f)

        // 3. Rasio Lebar (3:2 -> 1.5) - Yang Anda pakai sekarang
        Text("Rasio 1.5f (3:2)", modifier = Modifier.padding(start=16.dp))
        ImageActionRow(ratio = 1.5f)

        // 4. Rasio Sangat Lebar (16:9 -> 1.77)
        Text("Rasio 1.83f (16:9 - Cinematic)", modifier = Modifier.padding(start=16.dp))
        ImageActionRow(ratio = 16f / 9f)
    }
}