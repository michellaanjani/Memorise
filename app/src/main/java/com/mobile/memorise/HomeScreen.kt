package com.mobile.memorise

import androidx.compose.foundation.Image // Import Image
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
import androidx.compose.ui.layout.ContentScale // Import ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.mobile.memorise.ui.theme.* // Model Data JSON
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB))
    ) {
        HeaderSection()

        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Choice your Folder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(folderList) { folder ->
                FolderItemView(
                    data = folder,
                    onClick = { onFolderClick(folder.name) }
                )
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(DeepBlue)
            .padding(24.dp)
    ) {
        Column {
            // Baris Atas: Teks Salam & Avatar
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

            Spacer(modifier = Modifier.height(24.dp))

            // --- BAGIAN INI YANG DIPERBAIKI ---
            // Dua Tombol Gambar "Memorize" & "Learn"
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Memanggil resource drawable/memorize.xml
                ImageActionCard(
                    drawableId = R.drawable.memorize,
                    modifier = Modifier.weight(1f)
                )

                // Memanggil resource drawable/learn.xml
                ImageActionCard(
                    drawableId = R.drawable.learn,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// --- KOMPONEN BARU UNTUK GAMBAR ---
@Composable
fun ImageActionCard(
    drawableId: Int,
    modifier: Modifier = Modifier
) {
    // Menggunakan Image bukan Card+Box, karena desain sudah ada di XML
    Image(
        painter = painterResource(id = drawableId),
        contentDescription = null,
        contentScale = ContentScale.FillBounds, // Agar gambar memenuhi kotak
        modifier = modifier
            .height(80.dp) // Menjaga tinggi tetap sama
            .clip(RoundedCornerShape(16.dp)) // Memotong sudut gambar agar rounded
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