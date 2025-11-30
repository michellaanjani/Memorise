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
    val deepBlue = Color(0xFF0C3DF4)
    val textDark = Color(0xFF1A1A1A)
    val textGray = Color(0xFF6A6A6A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(42.dp))

        // ===================== TOP BAR =====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.memorisey),
                contentDescription = null,
                modifier = Modifier
                    .height(22.dp)
                    .padding(start = 2.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Skip",
                fontSize = 14.sp,
                color = textGray,
                modifier = Modifier
                    .padding(end = 2.dp)
                    .clickable { onSkip() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ===================== TITLE =====================
        Text(
            text = "Instantly turn your study\nmaterials into flashcard",
            textAlign = TextAlign.Center,
            fontSize = 20.sp,                      // ← lebih kecil
            fontWeight = FontWeight.ExtraBold,     // ← lebih tebal
            color = textDark,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ===================== BIG IMAGE =====================
        Image(
            painter = painterResource(id = R.drawable.materi),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Fit    // ⬅️ tidak akan crop
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ===================== FEATURES LIST =====================
        Column(
            modifier = Modifier
                .wrapContentWidth()                     // width mengikuti isi
                .align(Alignment.CenterHorizontally),   // block berada di tengah
            horizontalAlignment = Alignment.Start        // teks rata kiri
        ) {
            FeatureBullet(text = "Extract images")
            Spacer(modifier = Modifier.height(8.dp))
            FeatureBullet(text = "Highlight important words")
            Spacer(modifier = Modifier.height(8.dp))
            FeatureBullet(text = "Use fill-in-the-blank")
        }


        Spacer(modifier = Modifier.height(28.dp))

        // ===================== PAGE INDICATOR (PAGE 2) =====================
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 0 → INACTIVE DOT
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(Color(0xFFD9D9D9), RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(6.dp))

            // 1 → ACTIVE (PANJANG)
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(5.dp)
                    .background(deepBlue, RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(6.dp))

            // 2 → INACTIVE DOT
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(Color(0xFFD9D9D9), RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(6.dp))

            // 3 → INACTIVE DOT
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(Color(0xFFD9D9D9), RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(6.dp))

            // 4 → INACTIVE DOT
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(Color(0xFFD9D9D9), RoundedCornerShape(10.dp))
            )
        }


        Spacer(modifier = Modifier.height(26.dp))

        // ===================== BUTTON =====================
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = deepBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Generate FlashCard",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun FeatureBullet(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.cek),  // centang biru
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = Color(0xFF1A1A1A))
    }
}

@Composable
fun Dot(active: Boolean) {
    Box(
        modifier = Modifier
            .size(if (active) 12.dp else 6.dp)
            .background(
                if (active) Color(0xFF0C3DF4) else Color(0xFFD9D9D9),
                RoundedCornerShape(50)
            )
    )
}
