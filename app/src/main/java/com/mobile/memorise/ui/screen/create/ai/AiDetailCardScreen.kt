package com.mobile.memorise.ui.screen.create.ai

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Reuse helpers from cards package
import com.mobile.memorise.ui.screen.cards.DetailBottomBar
import com.mobile.memorise.ui.screen.cards.DetailTopBar
import com.mobile.memorise.ui.screen.cards.SideLabel

// Make sure AiDraftCard is visible in this package or import it if located elsewhere
// @kotlinx.serialization.Serializable
// data class AiDraftCard(val frontSide: String, val backSide: String)

private val BgColor = Color(0xFFF8F9FB)
private val TextDark = Color(0xFF1A1C24)

@Composable
fun AiDetailCardScreen(
    jsonCards: String,
    initialIndex: Int,
    onClose: () -> Unit,
    onEditAiCard: (Int, String) -> Unit,
    onReturnUpdatedList: (List<AiDraftCard>) -> Unit
) {
    // decode + initial list
    val decoded = Uri.decode(jsonCards)
    val initialList = remember { Json.decodeFromString<List<AiDraftCard>>(decoded) }
    val cardList = remember { mutableStateListOf<AiDraftCard>().apply { addAll(initialList) } }

    // safety for empty
    if (cardList.isEmpty()) {
        LaunchedEffect(Unit) { onReturnUpdatedList(emptyList()) }
        return
    }

    var currentIndex by remember { mutableStateOf(initialIndex.coerceIn(0, cardList.lastIndex)) }

    // Use the same pager API as your project: rememberPagerState with pageCount lambda
    val pagerState = rememberPagerState(
        initialPage = currentIndex,
        pageCount = { cardList.size }
    )

    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            DetailTopBar(
                currentIndex = pagerState.currentPage,
                totalCards = cardList.size,
                onClose = onClose,
                onEditClick = { index ->
                    val json = Json.encodeToString(cardList.toList())
                    val encoded = Uri.encode(json)
                    onEditAiCard(index, encoded)
                },
                onDeleteClick = {
                    showDeleteDialog = true
                }
            )
        },
        bottomBar = {
            DetailBottomBar(
                currentIndex = pagerState.currentPage,
                totalCards = cardList.size,
                onPrevClick = {
                    scope.launch {
                        val prev = (pagerState.currentPage - 1).coerceAtLeast(0)
                        pagerState.animateScrollToPage(prev)
                    }
                },
                onNextClick = {
                    scope.launch {
                        val next = (pagerState.currentPage + 1).coerceAtMost(cardList.lastIndex)
                        pagerState.animateScrollToPage(next)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 16.dp
            ) { pageIndex ->
                // update currentIndex for TopBar label
                currentIndex = pageIndex
                val currentCard = cardList[pageIndex]
                AiCardContentView(currentCard)
            }
        }
    }

    if (showDeleteDialog) {
        com.mobile.memorise.ui.component.DeleteConfirmDialog(
            onCancel = { showDeleteDialog = false },
            onDelete = {
                val removeAt = pagerState.currentPage
                if (removeAt in cardList.indices) {
                    cardList.removeAt(removeAt)
                }
                if (cardList.isEmpty()) {
                    onReturnUpdatedList(emptyList())
                } else {
                    onReturnUpdatedList(cardList.toList())
                }
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun AiCardContentView(card: AiDraftCard) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SideLabel(text = "Front Side")
            Spacer(Modifier.height(24.dp))

            Text(
                text = card.frontSide,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Divider(thickness = 4.dp, color = BgColor, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp)))

            Spacer(Modifier.height(32.dp))

            SideLabel(text = "Back Side")
            Spacer(Modifier.height(24.dp))

            Text(
                text = card.backSide,
                fontSize = 16.sp,
                color = TextDark.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
