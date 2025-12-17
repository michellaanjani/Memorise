package com.mobile.memorise.ui.screen.onboarding
import com.mobile.memorise.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen2(
    onNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    OnboardingLayout(
        currentPage = 1,
        buttonText = "Generate FlashCard", // Text tombol berubah di sini
        onNext = onNext,
        onSkip = onSkip
    ) {
        // === Konten Khusus Screen 2 ===

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
            painter = painterResource(id = R.drawable.materi),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Features List
        Column(
            modifier = Modifier.wrapContentWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            FeatureBullet(text = "Extract images")
            Spacer(modifier = Modifier.height(8.dp))
            FeatureBullet(text = "Highlight important words")
            Spacer(modifier = Modifier.height(8.dp))
            FeatureBullet(text = "Use fill-in-the-blank")
        }
    }
}

// Helper Composable (bisa ditaruh di file utils atau di bawah)
@Composable
fun FeatureBullet(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.cek),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = Color(0xFF1A1A1A))
    }
}