package com.mobile.memorise.ui.screen.signup.linksentverif

import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel

import com.mobile.memorise.R

@Composable
fun VerivicationLinkSentScreen(
    viewModel: VerificationStatusViewModel = hiltViewModel(),
    onContinue: () -> Unit,
) {
    val isVerified by viewModel.isVerified.collectAsState()

    LaunchedEffect(isVerified) {
        if (isVerified) {
            onContinue()
        }
    }

    VerificationLinkSentContent()
}

@Composable
private fun VerificationLinkSentContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3FA)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Image(
            painter = painterResource(id = R.drawable.memorisey),
            contentDescription = "Memorise Logo",
            modifier = Modifier.height(20.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "You're reset link has been sent!",
            fontSize = 18.sp,
            color = Color(0xFF2B2D42),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(56.dp))

        Image(
            painter = painterResource(id = R.drawable.sms),
            contentDescription = "SMS Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .heightIn(max = 220.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.weight(1f))

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
                text = "Please check your inbox to complete your reset password.",
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
