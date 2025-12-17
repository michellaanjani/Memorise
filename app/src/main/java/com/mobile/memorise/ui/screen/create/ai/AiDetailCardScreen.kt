package com.mobile.memorise.ui.screen.create.ai

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// IMPORT DOMAIN & COMPONENT
import com.mobile.memorise.domain.model.Card
import com.mobile.memorise.ui.component.DeleteConfirmDialog
import com.mobile.memorise.ui.screen.cards.DetailBottomBar
import com.mobile.memorise.ui.screen.cards.DetailTopBar
import com.mobile.memorise.ui.screen.cards.SideLabel
import com.mobile.memorise.util.Resource
import com.mobile.memorise.navigation.MainRoute

// --- COLORS ---
private val BgColor = Color(0xFFF8F9FB)
private val TextDark = Color(0xFF1A1C24)
private val TextGray = Color(0xFF7A7A7A) // Tambahan untuk popup
private val PrimaryBlue = Color(0xFF536DFE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDetailCardScreen(
    navController: NavController,
    initialIndex: Int,
    viewModel: AiViewModel = hiltViewModel()
) {
    // 1. Observe Data
    val draftState by viewModel.draftState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState() // 🔥 Observe Delete State
    val draftData = (draftState as? Resource.Success)?.data

    // Ambil list kartu terbaru & Deck ID
    val cardList = remember(draftData) { draftData?.cards ?: emptyList() }
    val currentDeckId = draftData?.deck?.id

    // Jika list kosong setelah delete (dan sukses load), kembali
    LaunchedEffect(cardList.size, draftState) {
        if (draftState is Resource.Success && cardList.isEmpty()) {
            navController.popBackStack()
        }
    }

    // Safety check loading/empty (hanya jika belum ada data sama sekali)
    if (draftState is Resource.Loading || (draftState is Resource.Success && cardList.isEmpty())) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    // 2. Pager State
    val validInitialIndex = initialIndex.coerceIn(0, (cardList.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(
        initialPage = validInitialIndex,
        pageCount = { cardList.size }
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 3. States
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteSuccessPopup by remember { mutableStateOf(false) } // 🔥 Popup State

    val currentCard = cardList.getOrNull(pagerState.currentPage)

    // 🔥 4. Handle Delete Result Logic
    LaunchedEffect(deleteState) {
        if (deleteState is Resource.Success) {
            showDeleteSuccessPopup = true
            delay(1500)
            showDeleteSuccessPopup = false
            viewModel.resetDeleteState()
        }
        if (deleteState is Resource.Error) {
            snackbarHostState.showSnackbar(deleteState.message ?: "Failed to delete")
            viewModel.resetDeleteState()
        }
    }

    // WRAP SCAFFOLD IN BOX UNTUK POPUP OVERLAY
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BgColor,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                DetailTopBar(
                    currentIndex = pagerState.currentPage,
                    totalCards = cardList.size,
                    onClose = { navController.popBackStack() },
                    onEditClick = {
                        currentCard?.let { card ->
                            navController.navigate(MainRoute.AiEditCard.createRoute(card.id))
                        }
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
                    pageSpacing = 16.dp,
                    verticalAlignment = Alignment.Top
                ) { pageIndex ->
                    if (pageIndex < cardList.size) {
                        AiCardContentView(cardList[pageIndex])
                    }
                }
            }
        }

        // --- DELETE DIALOG ---
        if (showDeleteDialog && currentCard != null && currentDeckId != null) {
            DeleteConfirmDialog(
                onCancel = { showDeleteDialog = false },
                onDelete = {
                    viewModel.deleteDraftCard(currentDeckId, currentCard.id)
                    showDeleteDialog = false
                }
            )
        }

        // 🔥 --- DELETE SUCCESS POPUP (ANIMASI) --- 🔥
        if (showDeleteSuccessPopup) {
            val alphaAnim by animateFloatAsState(if (showDeleteSuccessPopup) 1f else 0f, tween(250), label = "alpha")
            val scaleAnim by animateFloatAsState(if (showDeleteSuccessPopup) 1f else 0.95f, tween(250), label = "scale")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .padding(32.dp)
                        .graphicsLayer { scaleX = scaleAnim; scaleY = scaleAnim }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Success!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Card deleted successfully.",
                            fontSize = 14.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiCardContentView(card: Card) {
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
                text = card.front,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            HorizontalDivider(
                thickness = 4.dp,
                color = BgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
            )

            Spacer(Modifier.height(32.dp))

            SideLabel(text = "Back Side")
            Spacer(Modifier.height(24.dp))

            Text(
                text = card.back,
                fontSize = 16.sp,
                color = TextDark.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}