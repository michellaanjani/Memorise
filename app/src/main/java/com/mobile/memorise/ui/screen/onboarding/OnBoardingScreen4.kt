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

@Composable
fun OnboardingScreen4(
    onNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val deepBlue = Color(0xFF0C3DF4)
    val textDark = Color(0xFF202020)
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
                modifier = Modifier.height(22.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Skip",
                fontSize = 15.sp,
                color = textGray,
                modifier = Modifier.clickable { onSkip() }
            )
        }

        Spacer(modifier = Modifier.height(55.dp))

        // ===================== TITLE =====================
        Text(
            text = "With Memorise, every review\nboosts both instant recall and\nlong-term retention.",
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,   // lebih bold daripada ExtraBold
            color = textDark,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(45.dp))

        // ===================== MAIN IMAGE =====================
        Image(
            painter = painterResource(id = R.drawable.finalp),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.92f),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(120.dp))

        // ===================== PAGE INDICATOR (PAGE 4 ACTIVE) =====================
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            DotSmall()
            Spacer(modifier = Modifier.width(6.dp))
            DotSmall()
            Spacer(modifier = Modifier.width(6.dp))
            DotSmall()
            Spacer(modifier = Modifier.width(6.dp))
            DotActive()
            Spacer(modifier = Modifier.width(6.dp))
            DotSmall()
        }

        Spacer(modifier = Modifier.height(40.dp))

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
                text = "Let’s Start",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
