package com.mobile.memorise.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.mobile.memorise.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank

@Composable
fun UpdatePasswordScreen(
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()

    val backendPassword = "passwordBE"

    // Text state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // Password visibility toggles
    var currentPassVisible by remember { mutableStateOf(false) }
    var newPassVisible by remember { mutableStateOf(false) }

    // Error state for current password from BE
    var wrongPasswordError by remember { mutableStateOf(false) }

    // Popup success
    var showSuccessPopup by remember { mutableStateOf(false) }

    val isFormFilled = currentPassword.isNotBlank() && newPassword.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB))
            .statusBarsPadding()
            .padding(24.dp)
    ) {

        /** TOP BAR **/
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {

            // Back Button (kiri)
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterStart)
                    .clickable { navController.popBackStack() }
            )

            // Title (center)
            Text(
                text = "Update Password",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C24),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))


        /** CURRENT PASSWORD **/
        Text("Current Password", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(3.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = {
                currentPassword = it
                wrongPasswordError = false
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (currentPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.mata),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { currentPassVisible = !currentPassVisible }
                )
            },
            isError = wrongPasswordError
        )

        if (wrongPasswordError) {
            Text(
                text = "The password you entered is invalid!",
                color = Color(0xFFFF6A00),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(Modifier.height(16.dp))


        /** NEW PASSWORD **/
        Text("Password (min 8 characters)", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(3.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (newPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.mata),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { newPassVisible = !newPassVisible }
                )
            },
            isError = newPassword.length in 1..7
        )

        if (newPassword.length in 1..7) {
            Text(
                text = "Password must be at least 8 characters",
                color = Color.Red,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(Modifier.height(38.dp))


        /** UPDATE BUTTON **/
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = {
                    scope.launch {

                        // SIMULASI BACKEND VALIDATION
                        if (currentPassword != backendPassword) {
                            wrongPasswordError = true
                            return@launch
                        }

                        // SUCCESS POPUP
                        showSuccessPopup = true

                        // RESET FORM LANGSUNG
                        currentPassword = ""
                        newPassword = ""
                        wrongPasswordError = false

                        // Tahan popup 5 detik
                        delay(5000)

                        showSuccessPopup = false
                    }
                },
                enabled = isFormFilled && newPassword.length >= 8,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (isFormFilled && newPassword.length >= 8) Color(0xFF0961F5)
                        else Color(0xFFB9C4FF),
                    disabledContainerColor = Color(0xFFB9C4FF)
                ),
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp)
            ) {
                Text(
                    "Update Password",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }

    /** SUCCESS POPUP **/
    if (showSuccessPopup) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF79FF79))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Password Updated!",
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
