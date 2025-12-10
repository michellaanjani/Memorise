package com.mobile.memorise.ui.screen.signup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var otp by remember { mutableStateOf(List(5) { "" }) }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    var shouldShake by remember { mutableStateOf(false) }

    var resendEnabled by remember { mutableStateOf(true) }
    var timer by remember { mutableStateOf(0) }

    val shake = remember { Animatable(0f) }

    // Focus requesters untuk perpindahan fokus
    val focusRequesters = remember { List(5) { FocusRequester() } }

    suspend fun triggerShake() {
        shake.animateTo(12f, tween(60))
        shake.animateTo(0f, tween(60))
    }

    // Timer
    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer -= 1
            if (timer == 0) resendEnabled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(R.drawable.otpe),
            contentDescription = "OTP Icon",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Check Your Email",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Please enter the code we have sent to your",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // OTP BOXES
        Row(
            modifier = Modifier.graphicsLayer { translationX = shake.value },
            horizontalArrangement = Arrangement.Center
        ) {
            otp.forEachIndexed { index, value ->

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isError -> Color(0xFFFFEEEE)
                                isSuccess -> Color(0xFFD7F7D7)
                                else -> Color.White
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = when {
                                isError -> Color.Red
                                isSuccess -> Color(0xFF28A745)
                                value.isNotEmpty() -> Color(0xFF3B82F6)
                                else -> Color(0xFFD1D5DB)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    BasicTextField(
                        value = value,
                        onValueChange = { newInput ->
                            val filtered = newInput.filter { it.isDigit() }
                            val updated = otp.toMutableList()

                            // Jika user menghapus angka → pindah ke kolom sebelumnya
                            if (filtered.isEmpty() && value.isNotEmpty()) {
                                updated[index] = ""
                                otp = updated

                                if (index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                                return@BasicTextField
                            }

                            // Jika user input angka → pindah ke kolom berikutnya
                            if (filtered.length == 1) {
                                updated[index] = filtered
                                otp = updated

                                if (index < 4) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier
                            .width(52.dp)
                            .height(52.dp)
                            .focusRequester(focusRequesters[index])
                            .wrapContentSize(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row {
                Text("Don’t receive OTP?", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Resend",
                    color = if (resendEnabled) Color(0xFF3B82F6) else Color.Gray,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(enabled = resendEnabled) {
                        resendEnabled = false
                        timer = 120
                    }
                )
            }

            Text(
                text = if (timer > 0)
                    String.format("%02d:%02d", timer / 60, timer % 60)
                else "",
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                val code = otp.joinToString("")
                if (code != "12345") {
                    isError = true
                    isSuccess = false
                    otp = List(5) { "" }
                    shouldShake = true
                    focusRequesters[0].requestFocus()
                } else {
                    isError = false
                    isSuccess = true
                    onVerified()
                }
            },
            enabled = otp.all { it.isNotEmpty() },
            modifier = Modifier
                .width(160.dp)
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                disabledContainerColor = Color(0x33000000)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                "DONE",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }

    LaunchedEffect(shouldShake) {
        if (shouldShake) {
            triggerShake()
            shouldShake = false
        }
    }
}
