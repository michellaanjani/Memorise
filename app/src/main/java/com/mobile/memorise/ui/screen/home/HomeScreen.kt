package com.mobile.memorise.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import com.mobile.memorise.ui.theme.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/* =========================
        DATA MODELS
   ========================= */

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

/* =========================
        MAIN HOME SCREEN
   ========================= */

@Composable
fun HomeScreen(
    onFolderClick: (String) -> Unit,
    onDeckClick: (String) -> Unit,
    onEditFolder: (String, String) -> Unit,
    onEditDeck: (String) -> Unit
) {

    val context = LocalContext.current
    var homeData by remember { mutableStateOf(HomeData()) }

    // LOAD JSON
    LaunchedEffect(Unit) {
        try {
            val jsonString = context.assets.open("foldeck.json")
                .bufferedReader()
                .use { it.readText() }

            homeData = Json.decodeFromString(jsonString)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB)),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        item { HeaderSection() }

        /** EMPTY STATE */
        if (homeData.folders.isEmpty() && homeData.decks.isEmpty()) {
            item { EmptyView() }
        } else {

            /* ======================
                FOLDER LIST
               ====================== */
            if (homeData.folders.isNotEmpty()) {

                item {
                    Text(
                        text = "Choice your Folder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                val colorOptions = listOf("#E1FFBF", "#E8E9FE", "#FFF3CD")

                itemsIndexed(homeData.folders) { index, folder ->

                    val bgColor = Color(
                        android.graphics.Color.parseColor(
                            colorOptions[index % colorOptions.size]
                        )
                    )

                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {

                        FolderItemView(
                            data = folder,
                            bgColor = bgColor,
                            onClick = { onFolderClick(folder.name) },
                            onEditClick = { oldName, color ->
                                onEditFolder(oldName, color)
                            }
                        )
                    }
                }
            }

            /* ======================
                RECENT DECK LIST
               ====================== */
            if (homeData.decks.isNotEmpty()) {

                item {
                    Text(
                        text = "Recent Decks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                items(homeData.decks) { deck ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {

                        DeckItemView(
                            data = deck,
                            onClick = { onDeckClick(deck.deckName) },
                            onEditDeck = { deckName -> onEditDeck(deckName)}
                        )
                    }
                }
            }
        }
    }
}

/* =========================
        EMPTY VIEW
   ========================= */

@Composable
fun EmptyView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, bottom = 24.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.empty),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
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

/* =========================
        HEADER SECTION
   ========================= */

@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBlue)
            .statusBarsPadding()
            .padding(24.dp)
    ) {

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

        Spacer(modifier = Modifier.height(16.dp))

        /** ⬇️ INI YANG HILANG DAN SEKARANG SUDAH DIBALIKAN */
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


/* =========================
        FOLDER ITEM
   ========================= */

@Composable
fun FolderItemView(
    data: FolderItemData,
    bgColor: Color,
    onClick: () -> Unit,
    onEditClick: (String, String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFDFDFE) // ♻️ tampilan putih bersih
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    )
    {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
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

                Text(data.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextBlack)
                Text(data.date, fontSize = 12.sp, color = TextGray)
                Text("${data.deckCount} decks", fontSize = 14.sp, color = BrightBlue)
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, null, tint = Color(0xFFFF9800))
                                Spacer(Modifier.width(8.dp))
                                Text("Edit")
                            }
                        },
                        onClick = {
                            expanded = false
                            onEditClick(data.name, bgColor.toHex())
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red)
                                Spacer(Modifier.width(8.dp))
                                Text("Delete")
                            }
                        },
                        onClick = { expanded = false }
                    )
                }
            }
        }
    }
}

/* Convert Compose Color → Hex String */
fun Color.toHex(): String {
    return String.format("#%06X", (this.toArgb() and 0xFFFFFF))
}

/* =========================
        DECK ITEM
   ========================= */

@Composable
fun DeckItemView(
    data: DeckItemData,
    onClick: () -> Unit,
    onEditDeck: (String) -> Unit   // <-- harus menerima String
) {

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
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

                Text(data.deckName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${data.cardCount} Cards", fontSize = 12.sp, color = Color.Gray)
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {

                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick =
                            {
                                expanded = false
                                onEditDeck(data.deckName)
                        }
                    )

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { expanded = false }
                    )
                }
            }
        }
    }
}
