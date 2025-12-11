package com.mobile.memorise.ui.screen.home

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import com.mobile.memorise.ui.screen.createnew.deck.DeckViewModel
import com.mobile.memorise.ui.screen.createnew.folder.FolderViewModel
import com.mobile.memorise.ui.theme.*
import com.mobile.memorise.util.Resource
import kotlinx.serialization.Serializable

/* =========================
        DATA MODELS
   ========================= */

@Serializable
data class HomeData(
    val folders: List<FolderItemData> = emptyList(),
    val decks: List<DeckItemData> = emptyList()
)

@Serializable
data class FolderItemData(
    val id: String,
    val name: String,
    val date: String,
    val deckCount: Int,
    val serverColor: String? = null
)

@Serializable
data class DeckItemData(
    val id: String,
    val deckName: String,
    val cardCount: Int
)

/* =========================
     HELPER COLORS
   ========================= */
fun Color.darken(factor: Float = 0.6f): Color {
    return Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}

fun Color.toHex(): String {
    return String.format("#%06X", (this.toArgb() and 0xFFFFFF))
}


/* =========================
        MAIN HOME SCREEN
   ========================= */

@Composable
fun FolderIconWithColor(
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val folderColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFFEAEAFF) // Default color
    }

    Box(modifier = modifier) {
        // Layer Bawah (Body)
        Icon(
            painter = painterResource(id = R.drawable.ic_folder_base),
            contentDescription = null,
            tint = folderColor,
            modifier = Modifier.matchParentSize()
        )

        // Layer Atas (Overlay)
        Icon(
            painter = painterResource(id = R.drawable.ic_folder_overlay),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.matchParentSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Wajib untuk PullToRefresh
@Composable
fun HomeScreen(
    onFolderClick: (String) -> Unit,
    onDeckClick: (String) -> Unit,
    onEditFolder: (String, String, String) -> Unit,
    onEditDeck: (String) -> Unit,
    folderViewModel: FolderViewModel,
    deckViewModel: DeckViewModel,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteId  by remember { mutableStateOf("") }
    var deleteType by remember { mutableStateOf("") }

    // State dari API
    val homeState by homeViewModel.homeState.collectAsState()

    var homeData by remember { mutableStateOf(HomeData()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State untuk Pull To Refresh
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(homeState) {
        when(val result = homeState) {
            is Resource.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is Resource.Success -> {
                isLoading = false
                result.data?.let { homeData = it }
            }
            is Resource.Error -> {
                isLoading = false
                errorMessage = result.message
            }
        }
    }

    // ---------------------------------------------------------
    // CONTAINER UTAMA DENGAN PULL TO REFRESH
    // ---------------------------------------------------------
    PullToRefreshBox(
        isRefreshing = isLoading,
        state = pullRefreshState,
        onRefresh = {
            // Aksi saat di-swipe ke bawah
            homeViewModel.getHomeData()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FB)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            item { HeaderSection() }

            // LOGIKA LOADING:
            // Tampilkan spinner tengah HANYA jika data kosong (load pertama).
            // Jika data sudah ada (pull to refresh), spinner dihandle oleh PullToRefreshBox.
            if (isLoading && homeData.folders.isEmpty() && homeData.decks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            // TAMPILKAN ERROR
            else if (errorMessage != null && homeData.folders.isEmpty() && homeData.decks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Gagal memuat data", color = Color.Red, fontWeight = FontWeight.Bold)
                        Text(errorMessage ?: "", fontSize = 12.sp, color = Color.Gray)
                        Button(onClick = { homeViewModel.getHomeData() }) { Text("Coba Lagi") }
                    }
                }
            }
            else {
                // TAMPILKAN DATA
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

                        val fallbackColors = listOf("#E1FFBF", "#E8E9FE", "#FFF3CD")

                        itemsIndexed(homeData.folders) { index, folder ->
                            val colorString = folder.serverColor ?: fallbackColors[index % fallbackColors.size]

                            val bgColor = try {
                                Color(android.graphics.Color.parseColor(colorString))
                            } catch (e: Exception) {
                                Color(0xFFE8E9FE)
                            }

                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                FolderItemView(
                                    data = folder,
                                    bgColor = bgColor,
                                    onClick = { onFolderClick(folder.name) },
                                    onMoveClicked = { /*TODO*/ },
                                    onEditClick = { oldName, color ->
                                        val encodedName = Uri.encode(oldName)
                                        val safeColor = color.replace("#", "")
                                        onEditFolder(folder.id, encodedName, safeColor)
                                    },
                                    onDeleteClick = {
                                        deleteType = "folder"
                                        deleteId = folder.id
                                        showDeleteDialog = true
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
                                    onMoveClicked = { /*TODO*/ },
                                    onEditDeck = { deckName ->
                                        onEditDeck(Uri.encode(deckName))
                                    },
                                    onDeleteDeck = {
                                        deleteType = "deck"
                                        deleteId = deck.id
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    } // End of PullToRefreshBox

    // Popup Delete
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                showDeleteDialog = false
                when (deleteType) {
                    "folder" -> folderViewModel.deleteFolder(deleteId)
                    "deck" -> deckViewModel.deleteDeck(deleteId)
                }
                // Refresh data setelah hapus
                homeViewModel.getHomeData()

                deleteId = ""
                deleteType = ""
            }
        )
    }
}


/* =========================
        COMPONENTS
   ========================= */

@Composable
fun FolderItemView(
    data: FolderItemData,
    bgColor: Color,
    onClick: () -> Unit,
    onMoveClicked: () -> Unit,
    onEditClick: (String, String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFE)),
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
                val iconColor = bgColor.darken(0.6f)
                FolderIconWithColor(
                    colorHex = iconColor.toHex(),
                    modifier = Modifier.size(42.dp)
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
                    Icon(Icons.Default.MoreVert, "Options", tint = Color.Gray)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFFF9800))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = { expanded = false; onEditClick(data.name, bgColor.toHex()) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(0.3f))
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = { expanded = false; onDeleteClick(data.name) }
                    )
                }
            }
        }
    }
}

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

@Composable
fun DeckItemView(
    data: DeckItemData,
    onClick: () -> Unit,
    onMoveClicked: () -> Unit,
    onEditDeck: (String) -> Unit,
    onDeleteDeck: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFDFDFE)
        ),
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
                                Icon(painterResource(R.drawable.move), "Move", tint = Color(0xFF0961F5))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Move", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = { expanded = false; onMoveClicked() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.3f))
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFFF9800))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = { expanded = false; onEditDeck(data.deckName) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.3f))
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = { expanded = false; onDeleteDeck(data.deckName) }
                    )
                }
            }
        }
    }
}