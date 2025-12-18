package com.mobile.memorise.ui.screen.cards

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
// 1. FIX IMPORT DOMAIN MODEL
import com.mobile.memorise.domain.model.QuizAnswerInput
import com.mobile.memorise.domain.model.QuizQuestion
import com.mobile.memorise.domain.model.QuizResult
import com.mobile.memorise.ui.screen.cards.QuizViewModel
import com.mobile.memorise.util.Resource

// --- COLORS PALETTE ---
private val PrimaryBlue = Color(0xFF4285F4)
private val LightBlueBg = Color(0xFFF3F7FF)
private val WarningOrange = Color(0xFFFF9800)

private val TextGray = Color(0xFF6B7280)
private val SurfaceWhite = Color.White

@Composable
fun QuizScreen(
    deckId: String,
    deckName: String, // (Optional: parameter ini tidak terpakai di kode bawah, tapi oke dibiarkan)
    onBackClick: () -> Unit,
    quizViewModel: QuizViewModel = hiltViewModel()
) {
    val startState by quizViewModel.startState.collectAsState()
    val submitState by quizViewModel.submitState.collectAsState()

    // Snackbar State
    val snackbarHostState = remember { SnackbarHostState() }

    // State Lokal
    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }

    // 2. FIX TIPE DATA STATE JAWABAN (Gunakan QuizAnswerInput)
    var answers by remember { mutableStateOf(listOf<QuizAnswerInput>()) }

    // Efek Error Submit
    LaunchedEffect(submitState) {
        if (submitState is Resource.Error) {
            val msg = (submitState as Resource.Error).message ?: "Gagal mengirim jawaban."
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(deckId) {
        quizViewModel.resetStartState()
        quizViewModel.resetSubmitState()
        quizViewModel.startQuiz(deckId)
    }

    // Ambil data questions dari startState (Tipe: QuizSession)
    val questions = (startState as? Resource.Success)?.data?.questions ?: emptyList()
    val totalQuestions = (startState as? Resource.Success)?.data?.totalQuestions ?: questions.size

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBlueBg
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(Modifier.padding(paddingValues).fillMaxSize()) {
                when (startState) {
                    is Resource.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = PrimaryBlue)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Menyiapkan Kuis...", color = TextGray)
                            }
                        }
                    }
                    is Resource.Error -> {
                        val msg = (startState as Resource.Error).message ?: "Terjadi kesalahan."
                        ErrorStateView(message = msg, onRetry = { quizViewModel.startQuiz(deckId) }, onBack = onBackClick)
                    }
                    is Resource.Success -> {
                        // Cek apakah sudah submit sukses -> Tampilkan Result
                        if (submitState is Resource.Success) {
                            val result = (submitState as Resource.Success).data!!
                            QuizResultContent(
                                totalQuestions = result.totalQuestions,
                                correctAnswers = result.correctAnswers,
                                score = result.score,
                                onBackClick = onBackClick
                            )
                        } else if (questions.isNotEmpty()) {
                            // Tampilkan Soal
                            QuizQuestionContent(
                                question = questions[currentIndex],
                                currentIndex = currentIndex,
                                totalQuestions = totalQuestions,
                                onBackClick = onBackClick,
                                submitState = submitState, // Resource<QuizResult>
                                onAnswered = { input, isCorrect ->
                                    // 3. UPDATE LOGIC: Filter list lama & tambah jawaban baru
                                    answers = answers.filterNot { it.cardId == input.cardId } + input
                                    if (isCorrect) correctCount++
                                },
                                onNextClick = {
                                    if (currentIndex < questions.size - 1) {
                                        currentIndex++
                                    } else {
                                        // 4. FIX SUBMIT PARAMETERS (4 Parameter terpisah)
                                        quizViewModel.submitQuiz(
                                            deckId = deckId,
                                            totalQuestions = totalQuestions,
                                            correctAnswers = correctCount,
                                            answers = answers
                                        )
                                    }
                                }
                            )
                        } else {
                            ErrorStateView("Tidak ada kartu dalam Deck ini.", onRetry = onBackClick, onBack = onBackClick)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionContent(
    question: QuizQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    onBackClick: () -> Unit,
    submitState: Resource<QuizResult>, // 5. FIX TIPE PARAMETER
    onAnswered: (QuizAnswerInput, Boolean) -> Unit, // Callback: InputModel & Status Benar/Salah
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedAnswer by remember(question) { mutableStateOf<String?>(null) }
    var isAnswered by remember(question) { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = (currentIndex + 1) / totalQuestions.toFloat(),
        animationSpec = tween(500), label = "progress"
    )

    Scaffold(
        containerColor = LightBlueBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(shadowElevation = 4.dp, color = SurfaceWhite) {
                Column {
                    TopAppBar(
                        title = { Text("Quiz Time", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 18.sp) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = PrimaryBlue,
                        trackColor = Color(0xFFE0E0E0),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        },
        bottomBar = {
            if (isAnswered) {
                Surface(
                    shadowElevation = 16.dp, color = SurfaceWhite,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))) {
                        Button(
                            onClick = onNextClick,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (currentIndex == totalQuestions - 1) SuccessGreen else PrimaryBlue),
                            enabled = submitState !is Resource.Loading
                        ) {
                            if (submitState is Resource.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(text = if (currentIndex == totalQuestions - 1) "Finish Quiz" else "Next Question", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SurfaceWhite)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().verticalScroll(scrollState).padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Question ${currentIndex + 1} of $totalQuestions", style = MaterialTheme.typography.labelLarge, color = TextGray)
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = question,
                transitionSpec = { fadeIn(tween(300)) + slideInHorizontally { it } togetherWith fadeOut(tween(300)) + slideOutHorizontally { -it } },
                label = "q"
            ) { target ->
                Text(text = target.question, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark, textAlign = TextAlign.Center, lineHeight = 30.sp, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val options = remember(question) { (question.options ?: emptyList()).take(4) }
                options.forEach { option ->
                    QuizOptionItem(
                        optionText = option,
                        isSelected = selectedAnswer == option,
                        isAnswered = isAnswered,
                        isCorrectAnswer = option == question.correctAnswer,
                        onClick = {
                            if (!isAnswered) {
                                selectedAnswer = option
                                isAnswered = true
                                val isCorrect = option == question.correctAnswer

                                try {
                                    val soundRes = if (isCorrect) R.raw.correct else R.raw.wrong
                                    MediaPlayer.create(context, soundRes).apply { setOnCompletionListener { release() }; start() }
                                } catch (e: Exception) { e.printStackTrace() }

                                // 6. CONSTRUCT QUIZ ANSWER INPUT
                                val answerInput = QuizAnswerInput(
                                    cardId = question.cardId,
                                    userAnswer = option,
                                    correctAnswer = question.correctAnswer
                                )
                                onAnswered(answerInput, isCorrect)
                            }
                        }
                    )
                }
            }

            AnimatedVisibility(visible = isAnswered && !question.explanation.isNullOrBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), border = BorderStroke(1.dp, Color(0xFFFFC107)), modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Lightbulb, null, tint = Color(0xFFFFA000)); Spacer(Modifier.width(8.dp)); Text("Explanation", fontWeight = FontWeight.Bold, color = Color(0xFFFFA000)) }
                        Spacer(Modifier.height(4.dp)); Text(question.explanation ?: "", fontSize = 14.sp, color = TextDark)
                    }
                }
            }
            if (isAnswered) Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- BAGIAN KE BAWAH INI (UI COMPONENTS) TIDAK PERLU DIUBAH DARI VERSI SEBELUMNYA ---
// (QuizOptionItem, QuizResultContent, AnimatedCircularProgress, StatCardModern, ErrorStateView)
// Saya sertakan agar file lengkap bisa langsung dicopy-paste.

@Composable
fun QuizOptionItem(optionText: String, isSelected: Boolean, isAnswered: Boolean, isCorrectAnswer: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(targetValue = when { isAnswered && isCorrectAnswer -> SuccessGreen; isAnswered && isSelected && !isCorrectAnswer -> ErrorRed; isSelected -> PrimaryBlue; else -> Color(0xFFE5E7EB) }, label = "border")
    val backgroundColor by animateColorAsState(targetValue = when { isAnswered && isCorrectAnswer -> Color(0xFFDCFCE7); isAnswered && isSelected && !isCorrectAnswer -> Color(0xFFFEE2E2); isSelected -> Color(0xFFEFF6FF); else -> SurfaceWhite }, label = "bg")
    val icon = when { isAnswered && isCorrectAnswer -> Icons.Default.Check; isAnswered && isSelected && !isCorrectAnswer -> Icons.Default.Close; else -> null }
    Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(2.dp, borderColor), colors = CardDefaults.cardColors(containerColor = backgroundColor), modifier = Modifier.fillMaxWidth().clickable(enabled = !isAnswered, onClick = onClick).shadow(elevation = if (isSelected) 4.dp else 0.dp, shape = RoundedCornerShape(16.dp), spotColor = borderColor)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = optionText, fontSize = 16.sp, color = TextDark, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.weight(1f))
            if (icon != null) { Spacer(modifier = Modifier.width(8.dp)); Icon(imageVector = icon, contentDescription = null, tint = if (icon == Icons.Default.Check) SuccessGreen else ErrorRed, modifier = Modifier.size(24.dp).background(color = if (icon == Icons.Default.Check) SuccessGreen.copy(0.1f) else ErrorRed.copy(0.1f), shape = CircleShape).padding(4.dp)) }
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
    val context = LocalContext.current
    val wrongAnswers = totalQuestions - correctAnswers
    val (resultTitle, resultMessage, resultColor) = remember(score) {
        when {
            score >= 80 -> Triple("Excellent!", "Kamu luar biasa!", SuccessGreen)
            score >= 60 -> Triple("Good Job!", "Pertahankan semangatmu!", PrimaryBlue)
            score >= 40 -> Triple("Not Bad", "Coba lagi biar makin jago!", WarningOrange)
            else -> Triple("Keep Learning", "Jangan menyerah, ayo belajar lagi!", ErrorRed)
        }
    }
    val animatedScore = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        try {
            val mp = MediaPlayer.create(context, R.raw.done)
            mp.start()
            mp.setOnCompletionListener { it.release() }
        } catch (e: Exception) { e.printStackTrace() }
        animatedScore.animateTo(targetValue = score.toFloat(), animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing))
    }

    Scaffold(containerColor = SurfaceWhite, contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Quiz Result", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(32.dp))
            Box(contentAlignment = Alignment.Center) {
                AnimatedCircularProgress(percentage = animatedScore.value / 100f, color = resultColor, size = 220.dp, strokeWidth = 18.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${animatedScore.value.toInt()}%", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    Text("Score", fontSize = 14.sp, color = TextGray)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(resultTitle, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = resultColor)
            Text(resultMessage, fontSize = 16.sp, color = TextGray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCardModern(label = "Correct", value = correctAnswers.toString(), color = SuccessGreen, icon = Icons.Rounded.CheckCircle, modifier = Modifier.weight(1f))
                StatCardModern(label = "Wrong", value = wrongAnswers.toString(), color = ErrorRed, icon = Icons.Rounded.Cancel, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = LightBlueBg), shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total Questions", color = TextGray, fontSize = 14.sp)
                    Text("$totalQuestions", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("Back to Deck", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SurfaceWhite)
            }
        }
    }
}

@Composable
fun AnimatedCircularProgress(percentage: Float, color: Color, size: Dp, strokeWidth: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        drawCircle(color = Color(0xFFF3F4F6), style = Stroke(width = strokeWidth.toPx()))
        drawArc(color = color, startAngle = -90f, sweepAngle = 360 * percentage, useCenter = false, style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun StatCardModern(label: String, value: String, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(label, fontSize = 12.sp, color = TextGray)
            }
        }
    }
}

@Composable
fun ErrorStateView(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Close, null, tint = ErrorRed, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Oops!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(message, textAlign = TextAlign.Center, color = TextGray)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(PrimaryBlue)) { Text("Try Again") }
        }
    }
}