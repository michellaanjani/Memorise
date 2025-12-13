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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
import com.mobile.memorise.domain.model.quiz.QuizAnswerDetail
import com.mobile.memorise.domain.model.quiz.QuizQuestion
import com.mobile.memorise.domain.model.quiz.QuizSubmitData
import com.mobile.memorise.domain.model.quiz.QuizSubmitRequest
import com.mobile.memorise.ui.viewmodel.QuizViewModel
import com.mobile.memorise.util.Resource

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
    deckName: String, // Bisa digunakan untuk judul jika mau
    onBackClick: () -> Unit,
    quizViewModel: QuizViewModel = hiltViewModel()
) {
    // Collect State dari ViewModel
    val startState by quizViewModel.startState.collectAsState()
    val submitState by quizViewModel.submitState.collectAsState()

    // State Lokal Quiz
    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf(listOf<QuizAnswerDetail>()) }

    // 1. Mulai Quiz saat layar dibuka
    LaunchedEffect(deckId) {
        quizViewModel.resetStartState()
        quizViewModel.resetSubmitState()
        quizViewModel.startQuiz(deckId)
    }

    // Ambil data pertanyaan jika sukses
    val questions = (startState as? Resource.Success)?.data?.questions ?: emptyList()
    val totalQuestions = (startState as? Resource.Success)?.data?.totalQuestions ?: questions.size

    // --- MAIN CONTENT SWITCHER ---
    when (startState) {
        is Resource.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        }
        is Resource.Error -> {
            val msg = (startState as Resource.Error).message ?: "Gagal memulai kuis."
            ErrorView(
                message = msg,
                onRetry = { quizViewModel.startQuiz(deckId) },
                onBack = onBackClick
            )
        }
        is Resource.Success -> {
            // Cek apakah sudah Submit (Selesai)
            if (submitState is Resource.Success) {
                val result = (submitState as Resource.Success).data!!
                QuizResultContent(
                    totalQuestions = result.totalQuestions,
                    correctAnswers = result.correctAnswers,
                    score = result.score,
                    onBackClick = onBackClick
                )
            } else if (submitState is Resource.Error) {
                val msg = (submitState as Resource.Error).message ?: "Gagal mengirim jawaban."
                ErrorView(
                    message = msg,
                    onRetry = {
                        quizViewModel.submitQuiz(
                            QuizSubmitRequest(deckId, totalQuestions, correctCount, answers)
                        )
                    },
                    onBack = onBackClick
                )
            } else {
                // Tampilkan Pertanyaan Aktif
                if (questions.isEmpty()) {
                    ErrorView(
                        message = "Tidak ada pertanyaan tersedia untuk Deck ini.",
                        onRetry = onBackClick,
                        onBack = onBackClick
                    )
                } else {
                    QuizQuestionContent(
                        question = questions[currentIndex],
                        currentNumber = currentIndex + 1,
                        totalNumber = totalQuestions,
                        onBackClick = onBackClick,
                        onAnswered = { detail ->
                            // Simpan jawaban (hindari duplikat cardId)
                            answers = answers.filterNot { it.cardId == detail.cardId } + detail
                            if (detail.isCorrect) correctCount++
                        },
                        onNextClick = {
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                            } else {
                                // Jika soal terakhir selesai, Submit ke API
                                quizViewModel.submitQuiz(
                                    QuizSubmitRequest(deckId, totalQuestions, correctCount, answers)
                                )
                            }
                        },
                        isLastQuestion = currentIndex == questions.size - 1,
                        submitState = submitState
                    )
                }
            }
        }
        else -> {} // Idle State
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionContent(
    question: QuizQuestion,
    currentNumber: Int,
    totalNumber: Int,
    onBackClick: () -> Unit,
    onAnswered: (QuizAnswerDetail) -> Unit,
    onNextClick: () -> Unit,
    isLastQuestion: Boolean,
    submitState: Resource<QuizSubmitData>
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showExplain by remember { mutableStateOf(false) }

    // Ambil maksimal 4 opsi
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
            // Tombol Next/Submit Muncul setelah menjawab
            if (isAnswered) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = {
                            // Reset state lokal untuk pertanyaan berikutnya
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
                            containerColor = if (isLastQuestion) CorrectGreen else BluePrimary
                        ),
                        enabled = submitState !is Resource.Loading,
                    ) {
                        if (submitState is Resource.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isLastQuestion) "Selesai & Lihat Hasil" else "Pertanyaan Selanjutnya",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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

            // Indikator Nomor Soal
            Text(
                text = "Soal $currentNumber dari $totalNumber",
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Teks Pertanyaan
            Text(
                text = question.question,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Loop Opsi Jawaban
            options.forEach { option ->
                val isCorrectOption = option == question.correctAnswer
                val isSelected = option == selectedAnswer

                // Tentukan Warna Border & Background
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
                            Icon(
                                Icons.Default.Close,
                                null,
                                tint = WrongRed,
                                modifier = Modifier
                                    .border(1.dp, WrongRed, CircleShape)
                                    .padding(2.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                } else if (isSelected) {
                    // State terpilih tapi belum dikonfirmasi (opsional, disini langsung jawab)
                    borderColor = BluePrimary
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

                            // Callback ke Parent
                            onAnswered(
                                QuizAnswerDetail(
                                    cardId = question.cardId,
                                    isCorrect = isCorrectOption,
                                    userAnswer = option,
                                    correctAnswer = question.correctAnswer,
                                    explanation = question.explanation
                                )
                            )

                            // Mainkan Efek Suara
                            val soundRes = if (isCorrectOption) R.raw.correct else R.raw.wrong
                            val mp = MediaPlayer.create(context, soundRes)
                            mp.setOnCompletionListener { it.release() }
                            mp.start()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
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

            // Tombol Penjelasan (Hanya muncul jika sudah dijawab & ada penjelasan)
            if (isAnswered) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showExplain = !showExplain },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E7FF),
                        contentColor = TextDark
                    )
                ) {
                    Text(if (showExplain) "Sembunyikan Penjelasan" else "Lihat Penjelasan", fontWeight = FontWeight.SemiBold)
                }

                if (showExplain) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Penjelasan:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BluePrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = question.explanation ?: "Tidak ada penjelasan tambahan.",
                                color = TextDark,
                                fontSize = 14.sp
                            )
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
    score: Int,
    onBackClick: () -> Unit
) {
    val targetPercentage = score.toFloat()
    val wrongAnswers = totalQuestions - correctAnswers

    // Animasi Progress Bar
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = targetPercentage,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // KONTEN TENGAH (Weight 1f agar mengisi ruang)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(250.dp)
                ) {
                    // Track Belakang (Abu-abu muda)
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFF3F6FF),
                        strokeWidth = 20.dp,
                        trackColor = Color(0xFFF3F6FF),
                        strokeCap = StrokeCap.Round,
                    )

                    // Progress Depan (Biru) - Animasi
                    CircularProgressIndicator(
                        progress = { animatedProgress.value / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = BluePrimary,
                        strokeWidth = 20.dp,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round,
                    )

                    // Teks Persentase di Tengah
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${animatedProgress.value.toInt()}%",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Score",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Detail Jawaban
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$correctAnswers", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CorrectGreen)
                        Text("Benar", color = TextGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$wrongAnswers", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = WrongRed)
                        Text("Salah", color = TextGray)
                    }
                }
            }

            // TOMBOL KEMBALI (Di Bawah)
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
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