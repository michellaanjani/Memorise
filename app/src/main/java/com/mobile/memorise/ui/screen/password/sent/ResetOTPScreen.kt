package com.mobile.memorise.ui.screen.password.sent

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
import kotlinx.coroutines.delay

@Composable
fun ResetOtpScreen(
    email: String,
    onBack: () -> Unit,
    onVerified: (String) -> Unit, // Mengirim OTP ke screen selanjutnya
    viewModel: ResetOtpViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val timer by viewModel.timerState.collectAsState()

    // State Lokal
    var otp by remember { mutableStateOf(List(5) { "" }) }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Animasi Shake
    var shouldShake by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }

    // Focus Management
    val focusRequesters = remember { List(5) { FocusRequester() } }

    // UX: Otomatis fokus ke kolom pertama saat layar dibuka
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    suspend fun triggerShake() {
        shake.animateTo(12f, tween(60))
        shake.animateTo(0f, tween(60))
        shake.animateTo(-12f, tween(60))
        shake.animateTo(0f, tween(60))
    }

    // Efek Shake jika Error
    LaunchedEffect(shouldShake) {
        if (shouldShake) {
            triggerShake()
            shouldShake = false
        }
    }

    // Timer Countdown
    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            viewModel.decrementTimer()
        }
    }

    // Observer UI State (Resend OTP)
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ResetOtpUiState.Loading -> isLoading = true
            is ResetOtpUiState.ResendSuccess -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is ResetOtpUiState.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> isLoading = false
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

        // --- HEADER ---
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

        // --- ICON ---
        Image(
            painter = painterResource(R.drawable.otpe),
            contentDescription = "OTP Icon",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- TITLE ---
        Text(
            text = "Check Your Email",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Please enter the code we have sent to your email",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )

        // Menampilkan Email User
        Text(
            text = email,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- OTP INPUT FIELDS ---
        Row(
            modifier = Modifier.graphicsLayer { translationX = shake.value },
            horizontalArrangement = Arrangement.Center
        ) {
            otp.forEachIndexed { index, value ->
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isError) Color(0xFFFFEEEE) else Color.White)
                        .border(
                            width = 2.dp,
                            color = when {
                                isError -> Color.Red
                                value.isNotEmpty() -> Color(0xFF3B82F6) // Active Blue
                                else -> Color(0xFFD1D5DB) // Inactive Gray
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = { newInput ->
                            if (isLoading) return@BasicTextField

                            val filtered = newInput.filter { it.isDigit() }
                            val updated = otp.toMutableList()

                            // Handle Backspace (Clear & Move Back)
                            if (filtered.isEmpty() && value.isNotEmpty()) {
                                updated[index] = ""
                                otp = updated
                                if (index > 0) focusRequesters[index - 1].requestFocus()
                                return@BasicTextField
                            }

                            // Handle Input (Set & Move Next)
                            if (filtered.length == 1) {
                                updated[index] = filtered
                                otp = updated
                                isError = false
                                if (index < 4) focusRequesters[index + 1].requestFocus()
                            }
                        },
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

        // --- RESEND OTP ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Text("Don’t receive OTP?", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))

                val isResendClickable = timer == 0 && !isLoading

                Text(
                    text = "Resend",
                    color = if (isResendClickable) Color(0xFF3B82F6) else Color.Gray,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(enabled = isResendClickable) {
                        viewModel.resendOtp(email)
                    }
                )
            }

            Text(
                text = if (timer > 0) String.format("%02d:%02d", timer / 60, timer % 60) else "",
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- SUBMIT BUTTON ---
        val isButtonEnabled = otp.all { it.isNotEmpty() } && !isLoading

        Button(
            onClick = {
                val code = otp.joinToString("")
                // Logic verifikasi dilakukan di layar selanjutnya (NewPasswordScreen)
                onVerified(code)
            },
            enabled = isButtonEnabled,
            modifier = Modifier
                .width(160.dp) // Sesuai permintaan ukuran fixed
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                // State Aktif
                containerColor = Color(0xFF0961F5),
                contentColor = Color.White,

                // State Tidak Aktif (Abu Tua, Tulisan Putih)
                disabledContainerColor = Color(0xFFB3D4FC),
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "DONE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}