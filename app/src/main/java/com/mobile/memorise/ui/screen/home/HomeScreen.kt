package com.mobile.memorise.ui.screen.home

import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.shape.CircleShape // Tambahkan ini
import androidx.compose.material.icons.filled.History // Tambahkan ini
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
import com.mobile.memorise.ui.screen.profile.ProfileViewModel
import com.mobile.memorise.ui.theme.*
import com.mobile.memorise.util.Resource
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val cardCount: Int,
    val updatedAt: String // 🔥 TAMBAHAN: Field untuk tanggal update
)

/* =========================
     HELPER COLORS & DATE
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

// 🔥 HELPER FORMAT TANGGAL
fun formatHomeDeckDate(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val zone = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        "Updated: " + instant.atZone(zone).format(formatter)
    } catch (e: Exception) {
        "Updated recently"
    }
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
        Color(colorHex.toColorInt())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFolderClick: (FolderItemData) -> Unit,
    onDeckClick: (DeckItemData) -> Unit,
    onEditFolder: (String, String, String) -> Unit,
    onEditDeck: (String) -> Unit,
    onMoveDeck: (String) -> Unit,
    onHistoryClick: () -> Unit, // <--- 1. TAMBAHKAN PARAMETER INI
    folderViewModel: FolderViewModel,
    deckViewModel: DeckViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    // 🔥 TAMBAHAN 1: Ambil Context untuk menampilkan Toast
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteId by remember { mutableStateOf("") }
    var deleteType by remember { mutableStateOf("") }

    val homeState by homeViewModel.homeState.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()

    var homeData by remember { mutableStateOf(HomeData()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pullRefreshState = rememberPullToRefreshState()
    // 🔥 PERUBAHAN: Tambahkan Lifecycle Owner
    val lifecycleOwner = LocalLifecycleOwner.current

    // 🔥 PERUBAHAN: Gunakan DisposableEffect untuk Auto Reload saat ON_RESUME
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Dipanggil setiap kali layar tampil (awal buka atau kembali dari screen lain)
                homeViewModel.getHomeData()
                profileViewModel.loadUserProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // HAPUS atau KOMENTARI LaunchedEffect(Unit) yang lama karena sudah digantikan oleh kode di atas
    /*
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }
    */

    LaunchedEffect(homeState) {
        when (val result = homeState) {
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

            is Resource.Idle -> {
                isLoading = false
                errorMessage = null
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        state = pullRefreshState,
        onRefresh = {
            homeViewModel.getHomeData()
            profileViewModel.loadUserProfile()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FB)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            item {
                HeaderSection(firstName = userProfile.firstName,  onHistoryClick = onHistoryClick )
            }

            if (isLoading && homeData.folders.isEmpty() && homeData.decks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (errorMessage != null && homeData.folders.isEmpty() && homeData.decks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Gagal memuat data", color = Color.Red, fontWeight = FontWeight.Bold)
                        Text(errorMessage ?: "", fontSize = 12.sp, color = Color.Gray)
                        Button(onClick = { homeViewModel.getHomeData() }) { Text("Coba Lagi") }
                    }
                }
            } else {
                if (homeData.folders.isEmpty() && homeData.decks.isEmpty()) {
                    item { EmptyView() }
                } else {

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
                            val colorString =
                                folder.serverColor ?: fallbackColors[index % fallbackColors.size]

                            val bgColor = try {
                                Color(colorString.toColorInt())
                            } catch (e: Exception) {
                                Color(0xFFE8E9FE)
                            }

                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                FolderItemView(
                                    data = folder,
                                    bgColor = bgColor,
                                    onClick = { onFolderClick(folder) },
                                    onMoveClicked = { },
                                    onEditClick = { id, oldName, color ->
                                        val encodedName = Uri.encode(oldName)
                                        val safeColor = color.replace("#", "")
                                        onEditFolder(id, encodedName, safeColor)
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
                                    onClick = { onDeckClick(deck) },
                                    onMoveClicked = {
                                        onMoveDeck(deck.id)
                                    },
                                    onEditDeck = { deckId ->
                                        onEditDeck(deckId)
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
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                showDeleteDialog = false
                when (deleteType) {
                    "folder" -> folderViewModel.deleteFolder(deleteId)
                    "deck" -> deckViewModel.deleteDeck(deleteId)
                }
                homeViewModel.getHomeData()
                Toast.makeText(context, "Delete Success", Toast.LENGTH_SHORT).show()
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
fun HeaderSection(firstName: String?, onHistoryClick: () -> Unit) {
    val displayName = firstName?.let { name ->
        name.lowercase().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    } ?: "User"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepBlue)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        // --- HEADER ROW (NAMA & HISTORY) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hi, $displayName",
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

            // Tombol History
            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = Color.White
                )
            }
        }
        // -----------------------------------

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
fun FolderItemView(
    data: FolderItemData,
    bgColor: Color,
    onClick: () -> Unit,
    onMoveClicked: () -> Unit,
    onEditClick: (String, String, String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                        onClick = {
                            expanded = false; onEditClick(
                            data.id,
                            data.name,
                            bgColor.toHex()
                        )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray.copy(0.3f)
                    )
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 🔥 UBAH ICON: DateRange
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // 🔥 UBAH TEXT: Format Tanggal Update
                    Text(
                        text = formatHomeDeckDate(data.updatedAt),
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
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painterResource(R.drawable.move),
                                    "Move",
                                    tint = Color(0xFF0961F5)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Move", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = { expanded = false; onMoveClicked() }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFFF9800))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit", fontSize = 14.sp, color = TextBlack)
                            }
                        },
                        onClick = { expanded = false; onEditDeck(data.id) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
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