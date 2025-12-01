package com.mobile.memorise.ui.screen.cards

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
// import com.mobile.memorise.ui.screen.cards.CardItemData // Pastikan ini di-import sesuai package project Anda

// --- COLORS ---
private val TextDark = Color(0xFF1A1C24)
private val TextGray = Color(0xFF757575)
private val BluePrimary = Color(0xFF4285F4)
private val OrangeButton = Color(0xFFFF9800)

// Warna Status Jawaban
private val CorrectGreen = Color(0xFF00C853)
private val CorrectBg = Color(0xFFE8F5E9)
private val WrongRed = Color(0xFFD32F2F)
private val WrongBg = Color(0xFFFFEBEE)
private val BorderDefault = Color(0xFFE0E0E0)
private val BgDefault = Color(0xFFF5F6F8)

@Composable
fun QuizScreen(
    deckName: String,
    cardList: List<CardItemData>,
    onBackClick: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isQuizFinished by remember { mutableStateOf(false) }

    if (isQuizFinished) {
        QuizResultContent(
            totalQuestions = cardList.size,
            correctAnswers = correctCount,
            onBackClick = onBackClick
        )
    } else {
        QuizQuestionContent(
            currentCard = cardList[currentIndex],
            currentNumber = currentIndex + 1,
            totalNumber = cardList.size,
            allCards = cardList,
            onBackClick = onBackClick,
            onAnswerSelected = { isCorrect ->
                if (isCorrect) correctCount++
            },
            onNextClick = {
                if (currentIndex < cardList.size - 1) {
                    currentIndex++
                } else {
                    isQuizFinished = true
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionContent(
    currentCard: CardItemData,
    currentNumber: Int,
    totalNumber: Int,
    allCards: List<CardItemData>,
    onBackClick: () -> Unit,
    onAnswerSelected: (Boolean) -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    val isLastQuestion = currentNumber == totalNumber

    val options = remember(currentCard) {
        val correctAnswer = currentCard.back
        val wrongAnswers = allCards
            .filter { it.id != currentCard.id }
            .map { it.back }
            .shuffled()
            .take(2)
        (wrongAnswers + correctAnswer).shuffled()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.memorisey),
                        contentDescription = "Memorise Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.height(28.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (isAnswered) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(24.dp)
                ){
                    Button(
                        onClick = {
                            if (isLastQuestion) {
                                try {
                                    val mediaPlayer = MediaPlayer.create(context, R.raw.done)
                                    mediaPlayer.start()
                                    mediaPlayer.setOnCompletionListener { it.release() }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            selectedAnswer = null
                            isAnswered = false
                            onNextClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastQuestion) CorrectGreen else BluePrimary
                        )
                    ){
                        Text(
                            text = if (isLastQuestion) "Selesai" else "Next Question",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Soal $currentNumber dari $totalNumber",
                color = TextGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = currentCard.front,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(40.dp))

            options.forEach { option ->
                val isCorrectOption = option == currentCard.back
                val isSelected = option == selectedAnswer

                var borderColor = BorderDefault
                var bgColor = BgDefault
                var icon: @Composable (() -> Unit)? = null

                if (isAnswered) {
                    if (isCorrectOption) {
                        borderColor = CorrectGreen
                        bgColor = CorrectBg
                        icon = { Icon(Icons.Default.CheckCircle, null, tint = CorrectGreen) }
                    } else if (isSelected) {
                        borderColor = WrongRed
                        bgColor = WrongBg
                        icon = {
                            Icon(Icons.Default.Close, null, tint = WrongRed, modifier = Modifier.border(1.dp, WrongRed, CircleShape).padding(2.dp).size(16.dp))
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected || (isAnswered && isCorrectOption)) borderColor else BorderDefault),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected || (isAnswered && isCorrectOption)) bgColor else Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(enabled = !isAnswered) {
                            selectedAnswer = option
                            isAnswered = true
                            onAnswerSelected(isCorrectOption)

                            if (isCorrectOption) {
                                val mp = MediaPlayer.create(context, R.raw.correct)
                                mp.start()
                                mp.setOnCompletionListener { it.release() }
                            } else {
                                val mp = MediaPlayer.create(context, R.raw.wrong)
                                mp.start()
                                mp.setOnCompletionListener { it.release() }
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            color = TextDark,
                            modifier = Modifier.weight(1f)
                        )
                        if (isAnswered && icon != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            icon()
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultContent(
    totalQuestions: Int,
    correctAnswers: Int,
    onBackClick: () -> Unit
) {
    val targetPercentage = if (totalQuestions > 0) {
        ((correctAnswers.toFloat() / totalQuestions.toFloat()) * 100)
    } else 0f
    val wrongAnswers = totalQuestions - correctAnswers

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = targetPercentage,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.memorisey),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.height(28.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        // MAIN COLUMN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. KONTEN TENGAH (Animasi & Teks Hasil)
            // Gunakan weight(1f) di sini agar mengambil semua ruang sisa antara TopBar dan Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // Kunci: Konten vertikal di tengah
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(250.dp)
                ) {
                    // Track Belakang
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFF3F6FF),
                        strokeWidth = 20.dp,
                        trackColor = Color(0xFFF3F6FF),
                        strokeCap = StrokeCap.Round,
                    )

                    // Progress Depan
                    CircularProgressIndicator(
                        progress = { animatedProgress.value / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = BluePrimary,
                        strokeWidth = 20.dp,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round,
                    )

                    // Teks Angka
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${animatedProgress.value.toInt()}%",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Jawaban Benar",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "$correctAnswers Benar, $wrongAnswers Salah",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            // 2. TOMBOL (Di bagian bawah)
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp), // Tombol tetap di bawah dan tingginya fix
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeButton)
            ) {
                Text(
                    text = "Kembali ke Detail Deck",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}