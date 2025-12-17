package com.mobile.memorise.ui.screen.profile

// Import Icon Material Design
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.R
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.delay

@Composable
fun UpdatePasswordScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // Text state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // Password visibility toggles
    var currentPassVisible by remember { mutableStateOf(false) }
    var newPassVisible by remember { mutableStateOf(false) }

    // Error state for current password from BE
    var wrongPasswordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Popup success
    var showSuccessPopup by remember { mutableStateOf(false) }

    val changePasswordState by viewModel.changePasswordState.collectAsState()

    val isFormFilled = currentPassword.isNotBlank() && newPassword.isNotBlank()

    // Handle change password result
    LaunchedEffect(changePasswordState) {
        when (val state = changePasswordState) {
            is Resource.Success -> {
                // 1. Tampilkan Popup
                showSuccessPopup = true

                // 2. Reset form (opsional, agar bersih jika user kembali lagi nanti)
                currentPassword = ""
                newPassword = ""
                wrongPasswordError = false
                errorMessage = null

                // 3. Tunggu 2 detik agar user membaca pesan sukses
                delay(2000)

                // 4. Sembunyikan popup
                showSuccessPopup = false

                // 5. Kembali ke halaman sebelumnya (Profile)
                navController.popBackStack()
            }
            is Resource.Error -> {
                wrongPasswordError = true
                errorMessage = state.message
            }
            else -> {}
        }
    }

    // Root BOX agar popup bisa overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB))
    ) {
        // --- CONTENT LAYER ---
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                visualTransformation = if (currentPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                // Toggle Icon
                trailingIcon = {
                    val icon = if (currentPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = icon,
                        contentDescription = if (currentPassVisible) "Hide password" else "Show password",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { currentPassVisible = !currentPassVisible }
                    )
                },
                isError = wrongPasswordError,
                // Warna text tetap hitam saat error
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorTextColor = Color.Black,
                    cursorColor = Color.Black,
                    errorCursorColor = Color.Black
                )
            )

            if (wrongPasswordError) {
                Text(
                    text = errorMessage ?: "The password you entered is invalid!",
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
                // Toggle Icon
                trailingIcon = {
                    val icon = if (newPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(
                        imageVector = icon,
                        contentDescription = if (newPassVisible) "Hide password" else "Show password",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { newPassVisible = !newPassVisible }
                    )
                },
                isError = newPassword.length in 1..7,
                // Warna text tetap hitam saat error (panjang < 8)
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorTextColor = Color.Black,
                    cursorColor = Color.Black,
                    errorCursorColor = Color.Black
                )
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
                        viewModel.changePassword(currentPassword, newPassword)
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

        // --- POPUP SUCCESS LAYER ---
        if (showSuccessPopup) {
            val alphaAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0f,
                animationSpec = tween(250),
                label = "alpha"
            )
            val scaleAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0.95f,
                animationSpec = tween(250),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                    .clickable(enabled = false) {}, // Prevent click through
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .padding(32.dp)
                        .graphicsLayer {
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                            alpha = alphaAnim
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Success!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C24)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Password updated successfully.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}