package com.mobile.memorise.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.mobile.memorise.R
import com.mobile.memorise.navigation.MainRoute
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val user = viewModel.userProfile.collectAsState().value
    val hasPhoto = user.avatarUri != null

    // Load profile when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp) // ⭐ LEBIH PENDEK → text & logo otomatis naik
                .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4B89F3),
                            Color(0xFF3366FF)
                        )
                    )
                ))
         {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp), // ⭐ NAIKKAN
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Account",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Icon(
                    painter = painterResource(id = R.drawable.logm),
                    contentDescription = "Account Icon",
                    tint = Color.White,
                    modifier = Modifier.size(90.dp) // ⭐ sedikit lebih kecil, jadi lebih naik
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 110.dp), // ⭐ Foto naik sedikit
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // PROFILE PHOTO
            Box(contentAlignment = Alignment.BottomEnd) {

                if (hasPhoto) {
                    AsyncImage(
                        model = user.avatarUri,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(115.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFE2E2E2), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(115.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFE2E2E2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF0961F5),
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "${user.firstName} ${user.lastName}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C24)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // EDIT ACCOUNT CARD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .clickable { navController.navigate(MainRoute.EditProfile.route) }
                    .padding(vertical = 13.dp, horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Account",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1C24)
                )

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF1A1C24)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // LOGOUT BUTTON — ⭐ Dipendekkan
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .padding(horizontal = 80.dp) // ⭐ Lebih pendek
                    .height(48.dp)
                    .fillMaxWidth(), // tetap center tapi tidak sepanjang sebelumnya
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFCDD2),
                    contentColor = Color(0xFFD32F2F)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFF8A80))
            ) {
                Text(
                    text = "Log Out",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}