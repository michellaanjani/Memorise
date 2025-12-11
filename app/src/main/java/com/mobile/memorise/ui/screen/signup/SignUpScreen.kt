package com.mobile.memorise.ui.screen.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R

@Composable
fun SignUpScreen(
    onLoginClick: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var passwordVisible by remember { mutableStateOf(false) }
    val deepBlue = Color(0xFF0C3DF4)

    LaunchedEffect(key1 = true) {
        viewModel.signUpEvent.collect { event ->
            when(event) {
                is SignUpEvent.Success -> {
                    Toast.makeText(context, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                    onSignUpSuccess()
                }
                is SignUpEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        // PERBAIKAN 1: Paksa background menjadi Putih agar teks hitam terlihat jelas
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Menghapus statusBarsPadding() jika background status bar jadi aneh,
                    // tapi jika transparan biarkan saja.
                    // .statusBarsPadding()
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.Start
            ) {

                Spacer(modifier = Modifier.height(28.dp))

                Image(
                    painter = painterResource(id = R.drawable.memorisey),
                    contentDescription = null,
                    modifier = Modifier
                        .height(26.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Sign Up",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF121212) // Warna Teks Hitam Pekat
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Enter your details below to create account",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    MinimalInput(
                        label = "Email",
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it }
                    )
                    Spacer(modifier = Modifier.height(13.dp))
                    MinimalInput(
                        label = "First Name",
                        value = viewModel.firstName,
                        onValueChange = { viewModel.firstName = it }
                    )
                    Spacer(modifier = Modifier.height(13.dp))
                    MinimalInput(
                        label = "Last Name",
                        value = viewModel.lastName,
                        onValueChange = { viewModel.lastName = it }
                    )
                    Spacer(modifier = Modifier.height(13.dp))
                    MinimalInput(
                        label = "Password (min 8 characters)",
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onToggleVisibility = { passwordVisible = !passwordVisible },
                        isError = viewModel.password.isNotEmpty() && viewModel.password.length < 8
                    )

                    if (viewModel.password.isNotEmpty() && viewModel.password.length < 8) {
                        Text(
                            text = "Password must be at least 8 characters",
                            color = Color(0xFFFF6A00),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = viewModel.termsAccepted,
                        onCheckedChange = { viewModel.termsAccepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = deepBlue,
                            uncheckedColor = Color.Gray,
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "By creating an account you agree with our Terms & Conditions.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.onSignUpClick() },
                    enabled = !viewModel.isLoading && viewModel.termsAccepted && viewModel.password.length >= 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = deepBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    )
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create account",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Already have an account?",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Log in",
                        color = deepBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onLoginClick() }
                            .padding(bottom = 2.dp),
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    isError: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray // Label warna abu-abu
        )

        Spacer(modifier = Modifier.height(3.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),

            // Memastikan teks input berwarna Hitam
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = Color.Black
            ),

            singleLine = true,
            isError = isError,
            visualTransformation = if (isPassword) {
                if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },

            trailingIcon = {
                if (isPassword) {
                    Image(
                        painter = painterResource(id = R.drawable.mata),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onToggleVisibility() }
                    )
                }
            },

            colors = OutlinedTextFieldDefaults.colors(
                // PERBAIKAN 2: Pastikan warna teks jelas
                focusedBorderColor = Color(0xFF0C3DF4),
                unfocusedBorderColor = Color(0xFFD0D7E2),
                cursorColor = Color(0xFF0C3DF4),

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

                // Pastikan container transparan (mengikuti warna Scaffold Putih)
                // atau set focusedContainerColor = Color.White jika mau eksplisit
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}