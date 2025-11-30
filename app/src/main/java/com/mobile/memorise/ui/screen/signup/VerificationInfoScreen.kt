package com.mobile.memorise.ui.screen.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

@Composable
fun VerificationInfoScreen(
    onContinue: () -> Unit,
    autoNavigate: Boolean = true   // 🔧 bisa dimatikan nanti kalau mau
) {
    // ⏳ AUTO NAVIGATE setelah beberapa detik
    LaunchedEffect(Unit) {
        if (autoNavigate) {
            delay(5000)   // 5  detik
            onContinue()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3FA)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(64.dp))

        // --- LOGO ---
        Image(
            painter = painterResource(id = R.drawable.memorisey),
            contentDescription = "Memorise Logo",
            modifier = Modifier
                .height(20.dp), // diperkecil dari 24dp
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- SUBTITLE ---
        Text(
            text = "You're almost there!",
            fontSize = 18.sp,
            color = Color(0xFF2B2D42),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(56.dp))

        // --- ILUSTRASI SMS ---
        Image(
            painter = painterResource(id = R.drawable.sms),
            contentDescription = "SMS Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .heightIn(max = 220.dp), // diperkecil dari 280dp
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- CARD BAWAH ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Please check your inbox to complete your registration.",
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(26.dp))
            }
        }
    }

