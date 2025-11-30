package com.mobile.memorise.ui.screen.onboarding
import com.mobile.memorise.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.ui.component.DotActive
import com.mobile.memorise.ui.component.DotSmall

@Composable
fun OnboardingScreen5(
    onSignUp: () -> Unit = {},
    onLogin: () -> Unit = {}
) {
    val deepBlue = Color(0xFF0C3DF4)
    val deepBlueBorder = Color(0xFF0C3DF4)

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
                modifier = Modifier.height(22.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(65.dp))

        // ===================== BLUE CARD IMAGE =====================
        Image(
            painter = painterResource(id = R.drawable.offer),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(85.dp))

        // ===================== PAGE INDICATOR (PAGE 5 ACTIVE) =====================
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
            DotSmall()
            Spacer(modifier = Modifier.width(6.dp))
            DotActive() // page 5 aktfi
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ===================== BUTTON LOGIN (OUTLINE) =====================
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // SIGN UP BUTTON (filled)
            Button(
                onClick = onSignUp,
                colors = ButtonDefaults.buttonColors(containerColor = deepBlue),
                modifier = Modifier
                    .width(150.dp)            // ⬅️ persis seperti Figma (tidak full width)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Sign up",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // LOG IN BUTTON (outline)
            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .width(150.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, deepBlueBorder)
            ) {
                Text(
                    text = "Log in",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = deepBlue
                )
            }
        }
    }
}
