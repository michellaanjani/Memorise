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
    val deepBlue = Color(0xFF0C3DF4)
    val textGray = Color(0xFF6A6A6A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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

        // ===================== MAIN ILLUSTRATION =====================
        Image(
            painter = painterResource(id = R.drawable.illustration),
            contentDescription = null,
            modifier = Modifier
                .height(220.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ===================== TITLE =====================
        Text(
            text = "Scan and generate\ninstantly",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ===================== SUBTEXT =====================
        Text(
            text = "Turn your notes or documents into smart\nflashcards with just one scan.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = textGray,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(95.dp))

        // ===================== PAGE INDICATOR =====================
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Active indicator
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(5.dp)
                    .background(deepBlue, RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(6.dp))

            repeat(4) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color(0xFFD9D9D9), RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

// ===================== BUTTON =====================
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = deepBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                "Let’s Go",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
