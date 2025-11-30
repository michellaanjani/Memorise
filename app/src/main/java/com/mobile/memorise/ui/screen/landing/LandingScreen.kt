package com.mobile.memorise.ui.screen.landing
import com.mobile.memorise.R
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LandingScreen(
    onNavigate: () -> Unit = {}
) {
    val deepBlue = Color(0xFF0C3DF4)
    val textGray = Color(0xFF6A6A6A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(55.dp))

        // LOGO
        Image(
            painter = painterResource(id = R.drawable.memorisey),
            contentDescription = null,
            modifier = Modifier
                .width(220.dp)
                .height(85.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(4.dp))

        // SUBTITLE
        Text(
            text = "Turn notes into flashcards.\nLearn smarter with AI.",
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            color = textGray,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // LEARN MORE
        Text(
            text = "learn more",
            color = deepBlue,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ========= FEATURE CARDS ==========
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            // ROW 1
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FeatureCard(
                    icon = R.drawable.mage,
                    title = "Smart Scan",
                    desc = "turn your notes into flashcards in seconds",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    icon = R.drawable.search,
                    title = "AI Cards",
                    desc = "auto-generated questions to boost your memory",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ROW 2 – big icon
            FeatureCard(
                icon = R.drawable.progess,
                title = "Quick Review",
                desc = "practice anytime,\nanywhere with ease",
                modifier = Modifier.fillMaxWidth(),
                largeIcon = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // BUTTON (lebih naik)
        Button(
            onClick = onNavigate,
            colors = ButtonDefaults.buttonColors(containerColor = deepBlue),
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 42.dp, vertical = 12.dp) // ukuran ideal
        ) {
            Text(
                "Get Started",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }


        Spacer(modifier = Modifier.height(38.dp))
    }
}

@Composable
fun FeatureCard(
    icon: Int,
    title: String,
    desc: String,
    modifier: Modifier = Modifier,
    largeIcon: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(
            if (largeIcon) 165.dp else 150.dp   // lebih tinggi untuk progress card
        )
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {

            // ICON
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(
                    if (largeIcon) 70.dp else 38.dp // progress lebih besar
                ),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = desc,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xFF8A8A8A)
            )
        }
    }
}
