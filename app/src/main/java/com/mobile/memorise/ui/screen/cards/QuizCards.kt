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
    deckId: String,
    deckName: String,
    onBackClick: () -> Unit,
    quizViewModel: com.mobile.memorise.ui.viewmodel.QuizViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val startState by quizViewModel.startState.collectAsState()
    val submitState by quizViewModel.submitState.collectAsState()

    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf(listOf<com.mobile.memorise.domain.model.quiz.QuizAnswerDetail>()) }

    LaunchedEffect(deckId) {
        quizViewModel.resetStartState()
        quizViewModel.resetSubmitState()
        quizViewModel.startQuiz(deckId)
    }

    val questions = (startState as? com.mobile.memorise.util.Resource.Success)?.data?.questions ?: emptyList()
    val totalQuestions = (startState as? com.mobile.memorise.util.Resource.Success)?.data?.totalQuestions ?: questions.size

    when (startState) {
        is com.mobile.memorise.util.Resource.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        is com.mobile.memorise.util.Resource.Error -> {
            val msg = (startState as com.mobile.memorise.util.Resource.Error).message ?: "Failed to start quiz"
            ErrorView(message = msg, onRetry = { quizViewModel.startQuiz(deckId) }, onBack = onBackClick)
        }
        is com.mobile.memorise.util.Resource.Success -> {
            if (submitState is com.mobile.memorise.util.Resource.Success) {
                val result = (submitState as com.mobile.memorise.util.Resource.Success).data!!
                QuizResultContent(
                    totalQuestions = result.totalQuestions,
                    correctAnswers = result.correctAnswers,
                    score = result.score,
                    onBackClick = onBackClick
                )
            } else if (submitState is com.mobile.memorise.util.Resource.Error) {
                val msg = (submitState as com.mobile.memorise.util.Resource.Error).message ?: "Failed to submit quiz"
                ErrorView(message = msg, onRetry = {
                    quizViewModel.submitQuiz(
                        com.mobile.memorise.domain.model.quiz.QuizSubmitRequest(
                            deckId = deckId,
                            totalQuestions = totalQuestions,
                            correctAnswers = correctCount,
                            details = answers
                        )
                    )
                }, onBack = onBackClick)
            } else {
                if (questions.isEmpty()) {
                    ErrorView(message = "No questions available", onRetry = onBackClick, onBack = onBackClick)
                } else {
                    QuizQuestionContent(
                        question = questions[currentIndex],
                        currentNumber = currentIndex + 1,
                        totalNumber = totalQuestions,
                        onBackClick = onBackClick,
                        onAnswered = { detail ->
                            answers = answers.filterNot { it.cardId == detail.cardId } + detail
                            if (detail.isCorrect) correctCount++
                        },
                        onNextClick = {
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                            } else {
                                // submit
                                quizViewModel.submitQuiz(
                                    com.mobile.memorise.domain.model.quiz.QuizSubmitRequest(
                                        deckId = deckId,
                                        totalQuestions = totalQuestions,
                                        correctAnswers = correctCount,
                                        details = answers
                                    )
                                )
                            }
                        },
                        isLastQuestion = currentIndex == questions.size - 1,
                        submitState = submitState
                    )
                }
            }
        }
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionContent(
    question: com.mobile.memorise.domain.model.quiz.QuizQuestion,
    currentNumber: Int,
    totalNumber: Int,
    onBackClick: () -> Unit,
    onAnswered: (com.mobile.memorise.domain.model.quiz.QuizAnswerDetail) -> Unit,
    onNextClick: () -> Unit,
    isLastQuestion: Boolean,
    submitState: com.mobile.memorise.util.Resource<com.mobile.memorise.domain.model.quiz.QuizSubmitData>
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showExplain by remember { mutableStateOf(false) }

    val options = remember(question) { question.options.take(4) }

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
                            selectedAnswer = null
                            isAnswered = false
                            showExplain = false
                            onNextClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastQuestion) CorrectGreen else BluePrimary,
                            disabledContainerColor = Color(0xFFB9C4FF)
                        ),
                        enabled = submitState !is com.mobile.memorise.util.Resource.Loading,
                    ){
                        Text(
                            text = if (isLastQuestion) "Submit" else "Next Question",
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
                text = question.question,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(40.dp))

            options.forEach { option ->
                val isCorrectOption = option == question.correctAnswer
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
                            onAnswered(
                                com.mobile.memorise.domain.model.quiz.QuizAnswerDetail(
                                    cardId = question.cardId,
                                    isCorrect = isCorrectOption,
                                    userAnswer = option,
                                    correctAnswer = question.correctAnswer,
                                    explanation = question.explanation
                                )
                            )

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
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showExplain = !showExplain },
                enabled = isAnswered,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAnswered) Color(0xFFE0E7FF) else Color(0xFFE5E7EB),
                    contentColor = TextDark
                )
            ) {
                Text("Explain", fontWeight = FontWeight.SemiBold)
            }
            if (showExplain && question.explanation != null) {
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = question.explanation,
                        color = TextDark,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
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
    score: Int,
    onBackClick: () -> Unit
) {
    val targetPercentage = score.toFloat()
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