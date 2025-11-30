package com.mobile.memorise.ui.screen.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import com.mobile.memorise.ui.screen.cards.CardItemData

// --- COLORS ---
private val TextDark = Color(0xFF1A1C24)
private val TextGray = Color(0xFF757575)
private val BluePrimary = Color(0xFF4285F4)
private val BgColor = Color(0xFFF8F9FB)
private val OrangeButton = Color(0xFFFF9800) // Warna tombol hasil

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
    // State Global Quiz
    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isQuizFinished by remember { mutableStateOf(false) }

    // Jika Quiz Selesai, Tampilkan Result Screen
    if (isQuizFinished) {
        QuizResultContent(
            totalQuestions = cardList.size,
            correctAnswers = correctCount,
            onBackClick = onBackClick
        )
    } else {
        // Tampilkan Pertanyaan
        QuizQuestionContent(
            currentCard = cardList[currentIndex],
            currentNumber = currentIndex + 1,
            totalNumber = cardList.size,
            // Ambil list kartu lain untuk dijadikan jawaban jebakan
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
    // State Lokal per Pertanyaan
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    // Generate Pilihan Jawaban (1 Benar + 2 Salah)
    // remember(currentCard) memastikan opsi di-generate ulang hanya saat kartu berubah
    val options = remember(currentCard) {
        // 1. Jawaban Benar
        val correctAnswer = currentCard.back

        // 2. Jawaban Jebakan (Ambil dari kartu lain secara acak)
        val wrongAnswers = allCards
            .filter { it.id != currentCard.id } // Jangan ambil kartu yang sama
            .map { it.back }
            .shuffled()
            .take(2) // Ambil 2 saja

        // 3. Gabung dan Acak posisi
        (wrongAnswers + correctAnswer).shuffled()
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // LOGO MEMORISE RESPONSIVE
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            // Tombol Next hanya muncul jika sudah dijawab
            if (isAnswered) {
                Button(
                    onClick = {
                        // Reset state lokal sebelum pindah
                        selectedAnswer = null
                        isAnswered = false
                        onNextClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Next Question", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Indikator Soal
            Text(
                text = "Soal $currentNumber dari $totalNumber",
                color = TextGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PERTANYAAN
            Text(
                text = currentCard.front,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // LIST OPSI JAWABAN
            options.forEach { option ->
                val isCorrectOption = option == currentCard.back
                val isSelected = option == selectedAnswer

                // Menentukan Warna & Icon Status
                var borderColor = BorderDefault
                var bgColor = BgDefault
                var icon: @Composable (() -> Unit)? = null

                if (isAnswered) {
                    if (isCorrectOption) {
                        // Jika ini jawaban benar -> Selalu Hijau (baik dipilih atau tidak, agar user tahu yg benar)
                        borderColor = CorrectGreen
                        bgColor = CorrectBg
                        icon = { Icon(Icons.Default.CheckCircle, null, tint = CorrectGreen) }
                    } else if (isSelected) {
                        // Jika ini jawaban salah yang dipilih user -> Merah
                        borderColor = WrongRed
                        bgColor = WrongBg
                        icon = {
                            // Icon Silang Merah dalam Lingkaran
                            Icon(Icons.Default.Close, null, tint = WrongRed, modifier = Modifier.border(1.dp, WrongRed, CircleShape).padding(2.dp).size(16.dp))
                        }
                    }
                }

                // Item Jawaban
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

                        // Tampilkan Icon jika sudah dijawab
                        if (isAnswered && icon != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            icon()
                        }
                    }
                }
            }
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
    // Menghitung Persentase (Int)
    val percentage = if (totalQuestions > 0) {
        ((correctAnswers.toFloat() / totalQuestions.toFloat()) * 100).toInt()
    } else 0

    val wrongAnswers = totalQuestions - correctAnswers

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- CIRCULAR PROGRESS BAR ---
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(250.dp)
            ) {
                // 1. Track (Lingkaran Abu Belakang)
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF3F6FF), // Biru sangat muda
                    strokeWidth = 20.dp,
                    trackColor = Color(0xFFF3F6FF),
                    strokeCap = StrokeCap.Round
                )

                // 2. Progress (Lingkaran Biru Depan)
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = BluePrimary,
                    strokeWidth = 20.dp,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )

                // 3. Text Tengah
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
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

            // Text Ringkasan
            Text(
                text = "$correctAnswers Benar, $wrongAnswers Salah",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.weight(1f)) // Dorong tombol ke bawah

            // Tombol Kembali
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp), // Lebih bulat
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