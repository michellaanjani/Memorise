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
fun OnboardingScreen3(
    onNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    // Menggunakan Wrapper Layout
    OnboardingLayout(
        currentPage = 2, // Halaman ke-3 (index 2)
        buttonText = "Continue",
        onNext = onNext,
        onSkip = onSkip
    ) {
        // === KONTEN KHUSUS SCREEN 3 ===

        Text(
            text = "Instantly turn your study\nmaterials into flashcard",
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        Image(
            painter = painterResource(id = R.drawable.resul),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.85f),
            contentScale = ContentScale.Fit
        )
    }
}