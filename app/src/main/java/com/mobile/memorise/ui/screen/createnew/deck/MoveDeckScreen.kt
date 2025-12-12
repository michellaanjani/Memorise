package com.mobile.memorise.ui.screen.createnew.deck

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

// --- Warna Desain ---
private val PrimaryBlue = Color(0xFF0961F5)
private val TextBlack = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val BgColor = Color(0xFFF9FAFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveDeckScreen(
    deckId: String,
    onBackClick: () -> Unit,
    viewModel: DeckViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val moveState = viewModel.moveDeckState
    val folders = viewModel.folders

    // State pilihan folder: null artinya "Home / Unassigned"
    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFolders()
    }

    LaunchedEffect(moveState) {
        when (moveState) {
            is MoveDeckState.Success -> {
                showSuccessPopup = true
                delay(1500)
                viewModel.resetState()
                onBackClick()
            }
            is MoveDeckState.Error -> {
                Toast.makeText(context, moveState.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Move Deck", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextBlack)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextBlack)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor)
            )
        },
        bottomBar = {
            ContainerBottomBar(
                isLoading = moveState is MoveDeckState.Loading,
                onConfirm = {
                    // PERBAIKAN DISINI:
                    // Jika selectedFolderId adalah null (Home), kirim string kosong ""
                    // Backend menolak null, jadi kita kirim "" sebagai penanda "Tanpa Folder"
                    viewModel.moveDeck(deckId, selectedFolderId ?: "")
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Header Section
            Text(
                text = "Where do you want to move this deck?",
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 12.dp),
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- ITEM 1: HOME ---
                item {
                    SelectionItem(
                        name = "Home (Unassigned)",
                        icon = Icons.Rounded.Home,
                        iconColor = PrimaryBlue,
                        isSelected = selectedFolderId == null,
                        onClick = { selectedFolderId = null }
                    )
                }

                // --- ITEM 2: FOLDERS ---
                items(folders) { folder ->
                    // Parse warna hex dari server, fallback ke Orange jika error
                    val folderColor = try {
                        Color(android.graphics.Color.parseColor(folder.color))
                    } catch (e: Exception) {
                        Color(0xFFFFB74D)
                    }

                    SelectionItem(
                        name = folder.name,
                        icon = Icons.Rounded.Folder,
                        iconColor = folderColor,
                        isSelected = selectedFolderId == folder.id,
                        onClick = { selectedFolderId = folder.id }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showSuccessPopup) {
        SuccessOverlay()
    }
}

@Composable
private fun SelectionItem(
    name: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animasi Border Color
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryBlue else Color.Transparent,
        animationSpec = tween(300), label = "border"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container (Lingkaran dengan background transparan warna icon)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor, // Warna Icon Solid
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = TextBlack,
                modifier = Modifier.weight(1f)
            )

            // Selection Indicator (Checkmark Modern)
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Unselected",
                    tint = Color.LightGray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun ContainerBottomBar(
    isLoading: Boolean,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Button(
            onClick = onConfirm,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(
                    elevation = if (isLoading) 0.dp else 6.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = PrimaryBlue.copy(alpha = 0.4f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                disabledContainerColor = Color(0xFFE5E7EB),
                contentColor = Color.White,
                disabledContentColor = Color(0xFF9CA3AF)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    "Move Here",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SuccessOverlay() {
    val scaleAnim by animateFloatAsState(targetValue = 1f, animationSpec = tween(300), label = "scale")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // Background lebih gelap sedikit biar popup pop-out
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(32.dp)
                .graphicsLayer {
                    scaleX = scaleAnim
                    scaleY = scaleAnim
                }
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF166534),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Success!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Deck moved successfully.",
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp
                )
            }
        }
    }
}