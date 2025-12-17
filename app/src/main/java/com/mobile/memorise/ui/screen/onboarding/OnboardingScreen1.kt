package com.mobile.memorise.ui.screen.onboarding
import com.mobile.memorise.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen1(
    onNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    OnboardingLayout(
        currentPage = 0,
        buttonText = "Let’s Go",
        onNext = onNext,
        onSkip = onSkip
    ) {
        // === Konten Khusus Screen 1 ===

        Image(
            painter = painterResource(id = R.drawable.illustration),
            contentDescription = null,
            modifier = Modifier
                .height(220.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Scan and generate\ninstantly",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Turn your notes or documents into smart\nflashcards with just one scan.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color(0xFF6A6A6A),
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}