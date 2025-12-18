package com.mobile.memorise.ui.screen.password.newpassword

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NewPasswordScreen(
    otpToken: String,
    onSuccessReset: () -> Unit,
    viewModel: NewPasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Validasi sederhana
    val isValid = password.length >= 8

    LaunchedEffect(state) {
        when (val s = state) {
            is NewPasswordState.Loading -> isLoading = true
            is NewPasswordState.Success -> {
                isLoading = false
                Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                onSuccessReset()
            }
            is NewPasswordState.Error -> {
                isLoading = false
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
            }
            else -> isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FC))
            .systemBarsPadding() // Menggunakan systemBarsPadding agar aman
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(20.dp))

        // ==== HEADER ====
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Reset password",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1E21),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(40.dp))

        // ==== LABEL ====
        Text(
            "New Password (min 8 characters)",
            fontSize = 13.sp,
            color = Color(0xFF6B6B6B),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(6.dp))

        // ==== INPUT FIELD ====
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            isError = password.isNotEmpty() && !isValid,
            enabled = !isLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = description,
                        tint = Color(0xFF6B6B6B)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                // Warna teks Normal (Saat mengetik & diam)
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

                // PENTING: Warna teks saat Error (kurang dari 8 karakter)
                errorTextColor = Color.Black,

                // Warna Border
                focusedBorderColor = Color(0xFF0A57F2),
                unfocusedBorderColor = Color(0xFFD9D9D9),
                errorBorderColor = Color.Red,

                // Warna Background
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White, // Pastikan bg tetap putih saat error

                // Opsional: Warna Kursor
                cursorColor = Color.Black,
                errorCursorColor = Color.Red
            )
        )

        if (password.isNotEmpty() && !isValid) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Password must be at least 8 characters.",
                fontSize = 11.sp,
                color = Color(0xFFE53935)
            )
        }

        // 👇 PERBAIKAN POSISI BUTTON (Diganti dari weight(1f) ke height tetap)
        Spacer(Modifier.height(60.dp))

        // ==== BUTTON ====
        val isButtonEnabled = isValid && !isLoading

        Button(
            onClick = {
                viewModel.resetPassword(otpToken, password)
            },
            enabled = isButtonEnabled,
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0A57F2),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFB3D4FC),
                disabledContentColor = Color.White
            ),
            modifier = Modifier
                .padding(bottom = 20.dp)
                .height(54.dp)
                .width(200.dp)
                .shadow(
                    elevation = if (isButtonEnabled) 8.dp else 0.dp,
                    shape = RoundedCornerShape(40.dp),
                    clip = false,
                    ambientColor = Color(0x400A57F2),
                    spotColor = Color(0x400A57F2)
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Update Password",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}