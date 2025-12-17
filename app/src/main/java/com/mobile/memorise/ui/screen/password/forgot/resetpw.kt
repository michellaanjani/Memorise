package com.mobile.memorise.ui.screen.password.forgot

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R

@Composable
fun ResetPwScreen(
    onBackClick: () -> Unit = {},
    // 🔥 PERBAIKAN 1: Callback menerima String email
    onEmailSent: (String) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            Toast.makeText(context, "Reset link sent to your email!", Toast.LENGTH_SHORT).show()
            // 🔥 PERBAIKAN 2: Kirim email ke screen selanjutnya via navigasi
            onEmailSent(state.email)
            viewModel.onEvent(ForgotPasswordEvent.ResetState)
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
        }
    }

    // Tombol aktif jika email tidak kosong & tidak sedang loading
    val buttonEnabled = state.email.isNotBlank() && !state.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3FA)), // Background utama abu-abu muda
        horizontalAlignment = Alignment.Start
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        // --- HEADER (Back + Logo) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() },
                tint = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.memorisey),
                contentDescription = null,
                modifier = Modifier.height(20.dp)
            )
        }

        // --- TITLE ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Reset password",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111111)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Please enter your email, and we’ll send you\n" +
                        "a message to reset your password",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- WHITE CONTAINER ---
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Email",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                // --- INPUT EMAIL ---
                OutlinedTextField(
                    value = state.email,
                    onValueChange = {
                        viewModel.onEvent(ForgotPasswordEvent.EmailChanged(it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = state.error != null,
                    enabled = !state.isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF111111),
                        unfocusedTextColor = Color(0xFF111111),
                        cursorColor = Color(0xFF0C3DF4),
                        focusedBorderColor = Color(0xFF0C3DF4),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                if (state.error != null) {
                    Text(
                        text = state.error ?: "An error occurred",
                        color = Color(0xFFFF6A00),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- BUTTON ---
                Button(
                    onClick = {
                        viewModel.onEvent(ForgotPasswordEvent.Submit)
                    },
                    enabled = buttonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    // 🔥 PERBAIKAN 3: Warna tombol sesuai permintaan (Abu Tua & Putih saat disabled) 🔥
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black, // Atau Color(0xFF0C3DF4) sesuai tema aktif kamu
                        contentColor = Color.White,

                        // Saat tombol MATI (Disabled)
                        disabledContainerColor = Color.Gray, // Abu Tua
                        disabledContentColor = Color.White       // Tulisan Tetap Putih
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Send reset link",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}