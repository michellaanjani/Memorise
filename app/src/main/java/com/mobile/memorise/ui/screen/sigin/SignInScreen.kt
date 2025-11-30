package com.mobile.memorise.ui.screen.sigin
import com.mobile.memorise.R
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Error states
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    val deepBlue = Color(0xFF0C3DF4)

    val correctEmail = "user01@gmail.com"
    val correctPassword = "01user"

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        // ========== HEADER SUPER BOLD ==========
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
                value = email,
                onValueChange = {
                    email = it
                    emailError = false
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                isError = emailError,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    color = Color.Black
                )
            )

            if (emailError) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6A00),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= PASSWORD =================
            Text("Password", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                isError = passwordError,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

            if (passwordError) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6A00),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ========== FORGOT PASSWORD (underline) ==========
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
            onClick = {
                val isEmailCorrect = email == correctEmail
                val isPasswordCorrect = password == correctPassword

                when {
                    isEmailCorrect && isPasswordCorrect -> {
                        onSignInSuccess()
                    }

                    !isEmailCorrect -> {
                        emailError = true
                        errorMessage = "Email not found!"
                    }

                    !isPasswordCorrect -> {
                        passwordError = true
                        errorMessage = "Password is incorrect!"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = deepBlue)
        ) {
            Text(
                text = "Log In",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ========== SIGN UP (underline) ==========
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


