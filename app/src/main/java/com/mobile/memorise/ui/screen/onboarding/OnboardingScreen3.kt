package com.mobile.memorise.ui.screen.onboarding
import com.mobile.memorise.R
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.ui.component.DotActive
import com.mobile.memorise.ui.component.DotSmall

// IMPORT DARI FILE DOT

@Composable
fun OnboardingScreen3(
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

        // ===================== MAIN IMAGE (resul.png) =====================
        Image(
            painter = painterResource(id = R.drawable.resul),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.85f),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ===================== PAGE INDICATOR =====================
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            DotSmall()
            Spacer(modifier = Modifier.width(6.dp))
            DotSmall()
            Spacer(modifier = Modifier.width(6.dp))
            DotActive()
            Spacer(modifier = Modifier.width(6.dp))
            DotSmall()
            Spacer(modifier = Modifier.width(6.dp))
            DotSmall()
        }

        Spacer(modifier = Modifier.height(36.dp))

        // ===================== BUTTON =====================
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = deepBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
