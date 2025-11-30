package com.mobile.memorise.ui.screen.password.newpassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R

@Composable
fun NewPasswordScreen(
    onSuccess: () -> Unit,
    onBackToResetPw: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isValid = password.length >= 8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FC))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(20.dp))

        // ==== HEADER ====
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Back button
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onBackToResetPw() }
            )

            // Title center
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
            "Password (min 8 characters)",
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
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.mata),
                    contentDescription = "",
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { passwordVisible = !passwordVisible }
                )
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color(0xFF0A57F2),
                unfocusedIndicatorColor = Color(0xFFD9D9D9),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
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

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onSuccess() },
            enabled = isValid,
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValid) Color(0xFF0A57F2) else Color(0xFF4D80FF),
                contentColor = Color.White
            ),
            modifier = Modifier
                .padding(bottom = 40.dp)
                .height(54.dp)
                .width(200.dp) // lebih proporsional
                .shadow(
                    elevation = if (isValid) 8.dp else 0.dp,
                    shape = RoundedCornerShape(40.dp),
                    clip = false,
                    ambientColor = Color(0x400A57F2), // soft blue glow
                    spotColor = Color(0x400A57F2)
                )
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
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

