package com.mobile.memorise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.mobile.memorise.ui.theme.* // Import warna dari Theme.kt kamu

// Model Data JSON
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

    // Membaca JSON dari assets/foldername.json
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
            .background(Color(0xFFF8F9FB)) // Background abu-abu sangat muda
    ) {
        // 1. Header Biru
        HeaderSection()

        // 2. Konten Scrollable
        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Judul "Choice your Folder"
            item {
                Text(
                    text = "Choice your Folder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Looping Data JSON ke UI
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
            .height(220.dp) // Tinggi Header Biru
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
                // Avatar Placeholder
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

            // Dua Kartu "Memorize" & "Learn"
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    text = "Memorize",
                    color = Color(0xFFBBDEFB),
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    text = "Learn",
                    color = Color(0xFFF8BBD0),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ActionCard(text: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier.height(80.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Bold,
                color = DeepBlue
            )
        }
    }
}

@Composable
fun FolderItemView(data: FolderItemData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Tombol Folder bisa diklik
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Folder Kotak Kiri
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FolderIconBg),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder Icon Garis-garis
                Icon(
                    painter = painterResource(id = R.drawable.folder), // Ganti dengan painterResource icon folder kamu
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Teks Tengah
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

            // Tombol Delete & Edit
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallActionButton(text = "delete", bg = DeleteBtnBg, txtColor = DeleteBtnText)
                    SmallActionButton(text = "edit", bg = Color(0xFFFFF3E0), txtColor = Color(0xFFFF9800))
                }
            }
        }
    }
}

@Composable
fun SmallActionButton(text: String, bg: Color, txtColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = txtColor, fontWeight = FontWeight.SemiBold)
    }
}