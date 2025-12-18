package com.mobile.memorise.ui.screen.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
import com.mobile.memorise.domain.model.QuizHistoryDetail
import com.mobile.memorise.domain.model.QuizResult

// Definisi Warna dari Desain
val BrandPurple = Color(0xFF0C3DF4)
val SuccessGreen = Color(0xFF2ECC71)
val SuccessBg = Color(0xFFE8F8F0)
val ErrorRed = Color(0xFFE74C3C)
val ErrorBg = Color(0xFFFDEDEC)
val OrangeScore = Color(0xFFFF9F43)
val TextDark = Color(0xFF2D3436)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailScreen(
    onBackClick: () -> Unit,
    onRetakeClick: (String, String) -> Unit,
    viewModel: QuizHistoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.memorisey),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .height(20.dp)
                            .padding(end = 16.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )

                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FE)
                )
            )
        },
        containerColor = Color(0xFFF8F9FE)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()).fillMaxSize()) {
            when (val state = uiState) {
                is QuizDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BrandPurple
                    )
                }
                is QuizDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = ErrorRed)
                        Button(onClick = { /* viewModel.retry() */ }) {
                            Text("Retry")
                        }
                    }
                }
                is QuizDetailUiState.Success -> {
                    QuizDetailContent(
                        result = state.result,
                        onRetakeClick = {
                            onRetakeClick(state.result.deckId, state.result.deckName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizDetailContent(
    result: QuizResult,
    onRetakeClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp), // Space agar tombol tidak menutupi konten terakhir
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Header Card (Score)
            item {
                HeaderScoreCard(result)
            }

            // 2. Title Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Review Answers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextDark
                    )
                    Text(
                        text = "Question 1 of ${result.totalQuestions}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // 3. List Pertanyaan & Jawaban
            itemsIndexed(result.details) { index, detail ->
                QuestionReviewItem(index + 1, detail)
            }
        }

        // 4. Floating Bottom Button
        Button(
            onClick = onRetakeClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retake Quiz", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun HeaderScoreCard(result: QuizResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Deck", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = result.deckName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TOTAL", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${result.totalQuestions} Questions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CORRECT", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${result.correctAnswers} Correct", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = SuccessGreen)
                    }
                }
            }

            // Circular Progress
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF0F0F0),
                    strokeWidth = 8.dp,
                )
                CircularProgressIndicator(
                    progress = { result.score / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = OrangeScore,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.score}%", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OrangeScore)
                    Text("Score", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun QuestionReviewItem(number: Int, detail: QuizHistoryDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Soal
            Text(
                text = detail.question,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // JAWABAN USER
            val isUserCorrect = detail.isCorrect

            // Kotak Jawaban User (Hijau jika benar, Merah jika salah)
            ReviewAnswerBox(
                label = "YOUR ANSWER",
                text = detail.userAnswer,
                isCorrectStyle = isUserCorrect,
                icon = if (isUserCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                isErrorState = !isUserCorrect
            )

            // JAWABAN BENAR (Hanya muncul jika user salah)
            if (!isUserCorrect) {
                Spacer(modifier = Modifier.height(12.dp))
                ReviewAnswerBox(
                    label = "CORRECT ANSWER",
                    text = detail.correctAnswer,
                    isCorrectStyle = true,
                    icon = Icons.Default.CheckCircle,
                    isErrorState = false
                )
            }

            // PERBAIKAN:
            // 1. Tambahkan tanda seru (!) untuk mengecek jika TIDAK kosong
            // 2. Masukkan Spacer ke dalam blok if agar padding tidak double jika kosong
            if (!detail.explanation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BrandPurple)) {
                                append("Explanation: ")
                            }
                            append(detail.explanation)
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF555555),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewAnswerBox(
    label: String,
    text: String,
    isCorrectStyle: Boolean, // True = Hijau, False = Merah
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isErrorState: Boolean // Khusus menandakan kotak merah
) {
    val borderColor = if (isErrorState) ErrorRed else SuccessGreen
    val bgColor = if (isErrorState) ErrorBg else SuccessBg
    val labelColor = if (isErrorState) ErrorRed else SuccessGreen

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = labelColor)
            Icon(imageVector = icon, contentDescription = null, tint = borderColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, fontSize = 14.sp, color = TextDark, lineHeight = 20.sp)
    }
}