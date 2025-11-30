package com.mobile.memorise.ui.screen.landing

import com.mobile.memorise.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LandingScreen(
    onNavigate: () -> Unit = {}
) {
    val blue = Color(0xFF0961F5)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(blue)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            Spacer(modifier = Modifier.height(70.dp))

            // ===== LOGO =====
            Image(
                painter = painterResource(id = R.drawable.logm),
                contentDescription = null,
                modifier = Modifier
                    .width(230.dp)
                    .height(82.dp)
            )

            // ===== SUBTITLE — sangat dekat =====
            Text(
                text = "Turn notes into flashcards.\nLearn smarter with AI.",
                fontSize = 17.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ===== ILUSTRASI =====
            Image(
                painter = painterResource(id = R.drawable.landis),
                contentDescription = null,
                modifier = Modifier
                    .width(300.dp)
                    .height(290.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ===== MODERN BUTTON =====
            Button(
                onClick = onNavigate,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .height(46.dp)
                    .width(230.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 14.dp
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    color = blue,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
