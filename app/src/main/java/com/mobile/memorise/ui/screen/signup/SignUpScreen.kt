package com.mobile.memorise.ui.screen.signup

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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

    // State untuk scroll
    val scrollState = rememberScrollState()

    var passwordVisible by remember { mutableStateOf(false) }
    val deepBlue = Color(0xFF0C3DF4)

    // PERBAIKAN LOGIC: Menggunakan .trim() agar spasi depan/belakang diabaikan saat validasi
    val isEmailError = remember(viewModel.email) {
        val trimmedEmail = viewModel.email.trim()
        // Error terjadi jika: Text asli tidak kosong (isNotBlank) TAPI format (setelah di-trim) salah
        viewModel.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
    }

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
                    .padding(horizontal = 22.dp)
                    .verticalScroll(scrollState),
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
                    color = Color(0xFF121212)
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
                        onValueChange = { viewModel.email = it },
                        isError = isEmailError
                    )

                    if (isEmailError) {
                        Text(
                            text = "Invalid email address format",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                        )
                    }

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

                // Update logika validasi form: gunakan isNotBlank() agar aman terhadap spasi saja
                val isFormValid = !viewModel.isLoading &&
                        viewModel.termsAccepted &&
                        viewModel.password.length >= 8 &&
                        !isEmailError &&
                        viewModel.email.isNotBlank()

                Button(
                    onClick = { viewModel.onSignUpClick() },
                    enabled = isFormValid,
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
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 20.dp)
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
            color = if(isError) MaterialTheme.colorScheme.error else Color.Gray
        )

        Spacer(modifier = Modifier.height(3.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),

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
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = onToggleVisibility) {
                        Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                    }
                }
            },

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0C3DF4),
                unfocusedBorderColor = Color(0xFFD0D7E2),
                cursorColor = Color(0xFF0C3DF4),

                errorBorderColor = MaterialTheme.colorScheme.error,
                errorCursorColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}