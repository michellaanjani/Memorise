package com.mobile.memorise.ui.screen.create.ai

import com.mobile.memorise.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val BgColor = Color(0xFFF5F5F7)
private val HeaderBlue = Color(0xFF0961F5)
private val CardWhite = Color.White
private val TextDark = Color(0xFF1A1C24)
private val TextGray = Color(0xFF7A7A7A)
private val PrimaryBlue = HeaderBlue

@Serializable
data class AiDraftCard(
    val frontSide: String,
    val backSide: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGeneratedDraftScreen(
    navController: NavController,
    deckName: String,
    cardsJson: String,
    onBackClick: () -> Unit,
    onSaveClick: (String, List<AiDraftCard>) -> Unit,
    onCardClick: (Int) -> Unit,
    onEditCardClick: (Int) -> Unit
) {
    var deckNameState by remember { mutableStateOf(deckName) }
    var cards by remember { mutableStateOf(Json.decodeFromString<List<AiDraftCard>>(cardsJson)) }

    var selectedIndex by remember { mutableStateOf(-1) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var showSuccessPopup by remember { mutableStateOf(false) }

    // FIX untuk delay popup
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = BgColor,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardWhite,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(46.dp)
                ) {
                    TextButton(
                        onClick = {
                            onSaveClick(deckNameState, cards)
                            showSuccessPopup = true

                            scope.launch {
                                kotlinx.coroutines.delay(3500)
                                showSuccessPopup = false
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlue)
                    ) {
                        Text("Save", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ================= HEADER (CENTER TITLE + LOGO) =================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                        .background(HeaderBlue)
                        .padding(bottom = 20.dp)
                ) {

                    // TOP BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "AI Generation",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.Transparent
                        )
                    }

                    // LOGO FIXED
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logm),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(90.dp) // bebas ubah
                        )
                    }
                }
            }



            // ============ DECK NAME INPUT ============
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Deck Name",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Text(
                            text = " *",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = deckNameState,
                        onValueChange = { deckNameState = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = PrimaryBlue,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        placeholder = {
                            Text("Enter deck name", color = TextGray)
                        }
                    )
                }
            }

            // ============ SECTION TITLE + COUNT ============
            item {
                Text(
                    text = "Cards generated — ${cards.size} cards",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // ================= CARD LIST =================
            itemsIndexed(cards) { index, card ->
                MinimalAppleCardItem(
                    card = card,
                    onClick = { onCardClick(index) },
                    onEditClick = { onEditCardClick(index) },
                    onDeleteClick = {
                        selectedIndex = index
                        showDeleteDialog = true
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    // ================= SUCCESS POPUP =================
    if (showSuccessPopup) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 60.dp)
                    .background(Color(0xFF7CFF8A), RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "Your card is ready!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }

    // ================= DELETE DIALOG =================
    if (showDeleteDialog && selectedIndex in cards.indices) {
        DeleteConfirmDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                cards = cards.toMutableList().apply { removeAt(selectedIndex) }
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun MinimalAppleCardItem(
    card: AiDraftCard,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardWhite,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.frontSide,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = card.backSide,
                    fontSize = 13.sp,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}
