package com.mobile.memorise.ui.screen.onboarding

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R

@Composable
fun OnboardingLayout(
    currentPage: Int,
    totalPages: Int = 5,
    buttonText: String,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val deepBlue = Color(0xFF0C3DF4)
    val textGray = Color(0xFF6A6A6A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            // PERBAIKAN 1: Ganti statusBarsPadding() dengan systemBarsPadding()
            // atau tambahkan navigationBarsPadding() agar memperhitungkan tombol navigasi HP
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // ===================== 1. TOP BAR (FIXED) =====================
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

        Spacer(modifier = Modifier.height(20.dp))

        // ===================== 2. CONTENT AREA =====================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===================== 3. BOTTOM AREA =====================
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Page Indicator ---
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(totalPages) { index ->
                    val isActive = index == currentPage
                    val width = if (isActive) 22.dp else 5.dp
                    val color = if (isActive) deepBlue else Color(0xFFD9D9D9)

                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(width)
                            .background(color, RoundedCornerShape(10.dp))
                    )

                    if (index != totalPages - 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // --- Main Button ---
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = deepBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // PERBAIKAN 2: Opsional, tambah sedikit jarak visual selain system bar
            // Agar tombol tidak terlalu mepet meskipun sudah ada systemBarsPadding
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}