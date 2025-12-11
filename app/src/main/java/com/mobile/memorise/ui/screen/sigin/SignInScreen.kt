package com.mobile.memorise.ui.screen.sigin

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R // Pastikan import R sesuai package Anda

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel() // Inject ViewModel di sini
) {
    val context = LocalContext.current

    // State UI lokal (hanya visual, data tetap di ViewModel)
    var passwordVisible by remember { mutableStateOf(false) }
    val deepBlue = Color(0xFF0C3DF4)

    // Efek untuk mendengarkan respon API (Sukses/Gagal)
    LaunchedEffect(key1 = true) {
        viewModel.authEvent.collect { event ->
            when(event) {
                is AuthEvent.Success -> {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    onSignInSuccess()
                }
                is AuthEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        // ========== LOGO ==========
        Image(
            painter = painterResource(id = R.drawable.memorisey),
            contentDescription = null,
            modifier = Modifier.height(30.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ========== HEADER ==========
        Text(
            text = "Log In",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1D1D1D),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {

            // ================= EMAIL =================
            Text("Your Email", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = viewModel.email, // Ambil dari ViewModel
                onValueChange = {
                    viewModel.email = it
                    viewModel.isError = false // Reset visual error saat mengetik
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                isError = viewModel.isError, // Visual error (merah) jika gagal
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ================= PASSWORD =================
            Text("Password", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = viewModel.password, // Ambil dari ViewModel
                onValueChange = {
                    viewModel.password = it
                    viewModel.isError = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                isError = viewModel.isError,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                trailingIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.mata),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { passwordVisible = !passwordVisible }
                    )
                }
            )

            // Pesan Error Text di bawah input (Opsional)
            if (viewModel.isError) {
                Text(
                    text = "Invalid email or password",
                    color = Color(0xFFFF6A00),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ========== FORGOT PASSWORD ==========
        Text(
            text = "Forgot password?",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onForgotPasswordClick() }
                .padding(bottom = 2.dp),
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ========== LOGIN BUTTON ==========
        Button(
            onClick = { viewModel.onSignInClick() },
            enabled = !viewModel.isLoading, // Matikan tombol saat loading
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = deepBlue,
                disabledContainerColor = deepBlue.copy(alpha = 0.7f)
            )
        ) {
            if (viewModel.isLoading) {
                // Tampilkan Loading Spinner
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Log In",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ========== SIGN UP ==========
        Row {
            Text("Don’t have an account? ", color = Color.Gray, fontSize = 13.sp)
            Text(
                text = "Sign up",
                color = deepBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onSignUpClick() }
            )
        }
    }
}