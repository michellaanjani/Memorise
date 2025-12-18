package com.mobile.memorise.ui.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.memorise.R
import com.mobile.memorise.navigation.MainRoute
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.delay

@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var showSuccessPopup by remember { mutableStateOf(false) }

    // --- LOGIC PERUBAHAN DI SINI ---
    // Menangani timer popup dan auto navigation back
    LaunchedEffect(showSuccessPopup) {
        if (showSuccessPopup) {
            delay(2000) // Tunggu 2 detik agar user membaca "Success"
            showSuccessPopup = false
            navController.popBackStack() // Otomatis kembali ke layar sebelumnya
        }
    }

    val userState by viewModel.userProfile.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    // Load profile when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Update local state when userState changes
    var firstName by remember(userState.firstName) { mutableStateOf(userState.firstName) }
    var lastName by remember(userState.lastName) { mutableStateOf(userState.lastName) }
    var email by remember(userState.email) { mutableStateOf(userState.email) }
    var avatarUri by remember(userState.avatarUri) { mutableStateOf(userState.avatarUri?.let { Uri.parse(it) }) }

    // Show success popup when update is successful
    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            showSuccessPopup = true
            viewModel.loadUserProfile() // Reload to get updated data
            viewModel.resetUpdateState()
        }
        if (updateState is Resource.Error) {
            viewModel.resetUpdateState()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3FA))
            .statusBarsPadding()
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            /** HEADER */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.CenterStart)
                        .clickable { navController.popBackStack() }
                )

                Text(
                    text = "Edit Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1C24)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            /** PROFILE AVATAR */
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(115.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFE2E2E2), CircleShape)
                                .clickable { pickImageLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(115.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E2E2), CircleShape)
                                .clickable { pickImageLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = Color(0xFF0961F5),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .offset(8.dp, 8.dp)
                            .size(36.dp)
                            .shadow(4.dp, CircleShape, clip = false)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                            .clip(CircleShape)
                            .clickable { pickImageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera),
                            contentDescription = "Change Photo",
                            tint = Color(0xFF0961F5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            /** FORM */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {

                // FIRST NAME
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("First Name", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        // Text input warna hitam
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        unfocusedBorderColor = Color(0xFFE7ECF5),
                        focusedBorderColor = Color(0xFF0961F5),
                        cursorColor = Color(0xFF0961F5)
                    ),
                    singleLine = true
                )

                // LAST NAME
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Last Name", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        // Text input warna hitam
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        unfocusedBorderColor = Color(0xFFE7ECF5),
                        focusedBorderColor = Color(0xFF0961F5),
                        cursorColor = Color(0xFF0961F5)
                    ),
                    singleLine = true
                )

                // EMAIL (Read Only - Disabled)
                OutlinedTextField(
                    value = email,
                    onValueChange = { },
                    enabled = false, // Tidak bisa diedit
                    placeholder = { Text("Email") },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.mail),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        // Warna disabled tetap abu-abu sesuai default/request
                        unfocusedBorderColor = Color(0xFFE7ECF5),
                        focusedBorderColor = Color(0xFFE7ECF5),
                        disabledBorderColor = Color(0xFFE7ECF5),
                        disabledTextColor = Color(0xFF9E9E9E),
                        disabledPlaceholderColor = Color(0xFF9E9E9E),
                        disabledLeadingIconColor = Color(0xFF9E9E9E)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            /** UPDATE BUTTON */
            val isFormFilled = firstName.isNotBlank() && lastName.isNotBlank()
            val isChanged =
                firstName != userState.firstName ||
                        lastName != userState.lastName ||
                        avatarUri?.toString() != userState.avatarUri

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (isChanged) {
                            viewModel.updateProfile(
                                firstName, lastName, avatarUri?.toString()
                            )
                        }
                    },
                    enabled = isFormFilled && isChanged,
                    modifier = Modifier
                        .height(46.dp)
                        .widthIn(min = 150.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormFilled && isChanged) Color(0xFF0961F5) else Color(0xFFBFD4FF),
                        disabledContainerColor = Color(0xFFBFD4FF)
                    )
                ) {
                    Text(
                        text = "Update",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            /** UPDATE PASSWORD BUTTON */
            Button(
                onClick = { navController.navigate(MainRoute.EditPassword.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0961F5)
                ),
                border = BorderStroke(1.dp, Color(0xFF0961F5))
            ) {
                Text(
                    text = "Update Password",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                )
            }
        }

        /** SUCCESS POPUP */
        if (showSuccessPopup) {
            val alphaAnim by animateFloatAsState(
                if (showSuccessPopup) 1f else 0f,
                tween(250),
                label = "alpha"
            )
            val scaleAnim by animateFloatAsState(
                if (showSuccessPopup) 1f else 0.95f,
                tween(250),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                    .clickable(enabled = false) {},
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
                                Icons.Default.Check,
                                null,
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Success!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Profile Updated successfully.",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}