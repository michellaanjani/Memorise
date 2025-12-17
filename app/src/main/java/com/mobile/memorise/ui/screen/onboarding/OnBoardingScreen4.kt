package com.mobile.memorise.ui.screen.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R

@Composable
fun OnboardingScreen4(
    onNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    // Menggunakan Wrapper Layout
    OnboardingLayout(
        currentPage = 3, // Halaman ke-4 (index 3)
        buttonText = "Let’s Start",
        onNext = onNext,
        onSkip = onSkip
    ) {
        // === KONTEN KHUSUS SCREEN 4 ===

        Text(
            text = "With Memorise, every review\nboosts both instant recall and\nlong-term retention.",
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black, // Menggunakan Black sesuai request
            color = Color(0xFF202020),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(45.dp))

        Image(
            painter = painterResource(id = R.drawable.finalp),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.92f),
            contentScale = ContentScale.Fit
        )
    }
}