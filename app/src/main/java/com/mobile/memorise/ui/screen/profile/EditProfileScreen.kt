package com.mobile.memorise.ui.screen.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.mobile.memorise.R
import com.mobile.memorise.navigation.MainRoute

@Composable
fun EditProfileScreen(
    navController: NavHostController
) {

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB))
            .padding(24.dp)
    ) {

        /** HEADER **/
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Edit Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C24)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        /** AVATAR **/
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
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { pickImageLauncher.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2EBFF))
                            .clickable { pickImageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(70.dp),
                            tint = Color(0xFF3D5CFF)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = 6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3D5CFF))
                        .clickable { pickImageLauncher.launch("image/*") }
                        .padding(7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Change Photo",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        /** FORM **/

        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            placeholder = { Text("First Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE7ECF5),
                focusedBorderColor = Color(0xFF3D5CFF),
                cursorColor = Color(0xFF3D5CFF)
            )
        )

        // Last Name
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            placeholder = { Text("Last Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE7ECF5),
                focusedBorderColor = Color(0xFF3D5CFF),
                cursorColor = Color(0xFF3D5CFF)
            )
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
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
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE7ECF5),
                focusedBorderColor = Color(0xFF3D5CFF),
                cursorColor = Color(0xFF3D5CFF)
            )
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val isFormFilled = firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank()

            Button(
                onClick = { /* TODO: Update profile */ },
                enabled = isFormFilled,
                modifier = Modifier
                    .widthIn(min = 140.dp) // <= ukuran minimum biar elegan
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormFilled) Color(0xFF3D5CFF) else Color(0xFFBFC8F8),
                    disabledContainerColor = Color(0xFFBFC8F8)
                )
            ) {
                Text(
                    text = "Update",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        /** PASSWORD LABEL **/
        Text(
            text = "Password",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C2F3A),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        /** BUTTON UPDATE PASSWORD — warna beda, lebih rounded **/
        Button(
            onClick = { navController.navigate(MainRoute.EditPassword.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF99A3D),
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "Update Password",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
        }

    }
}
