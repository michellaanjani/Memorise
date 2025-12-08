package com.mobile.memorise.ui.screen.createnew.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

@Composable
fun EditFolderScreen(
    oldName: String,
    initialColor: String,
    onBackClick: () -> Unit,
    folderViewModel: FolderViewModel
) {
    // Tidak mencari folder dari viewmodel lagi
    var folderName by remember { mutableStateOf(oldName) }
    var folderError by remember { mutableStateOf<String?>(null) }

    // Warna awal dari navController
    var selectedColor by remember { mutableStateOf(initialColor) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val colorOptions = listOf("#E1FFBF", "#E8E9FE", "#FFF3CD")

    val isChanged = folderName.trim() != oldName || selectedColor != initialColor

    val isFormValid = folderName.isNotBlank() && folderError == null && isChanged

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {

            // HEADER
            Spacer(modifier = Modifier.height(52.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() }
                )

                Text(
                    "Edit Folder",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(26.dp))

            // FORM CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF3F3FA))
                    .padding(26.dp)
            ) {

                // FOLDER NAME
                Row {
                    Text("Folder Name", fontWeight = FontWeight.SemiBold)
                    Text("*", color = Color(0xFFC53636))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        val trimmed = it.trim()

                        folderError =
                            if (trimmed.isNotEmpty() &&
                                trimmed.lowercase() != oldName.lowercase() &&
                                folderViewModel.folderList.any { f -> f.name.equals(trimmed, true) }
                            ) "Folder name already exists!"
                            else null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = folderError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor =
                            if (folderError != null) Color.Red else Color(0xFFDBE1F3),
                        focusedBorderColor =
                            if (folderError != null) Color.Red else Color(0xFF0961F5),
                        cursorColor = Color(0xFF0961F5)
                    )
                )

                if (folderError != null) {
                    Text(
                        folderError!!,
                        color = Color(0xFFFF6905),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // COLOR PICKER
                Row {
                    Text("Icon & Color", fontWeight = FontWeight.SemiBold)
                    Text("*", color = Color(0xFFC53636))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(android.graphics.Color.parseColor(selectedColor))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.folder),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 1.dp,
                                    color = if (selectedColor == hex) Color(0xFF0035A0) else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    folderViewModel.updateFolder(
                        oldName = oldName,
                        newName = folderName.trim(),
                        newColor = selectedColor
                    )

                    showSuccessPopup = true

                    coroutineScope.launch {
                        delay(1100L)
                        onBackClick()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(40.dp)
            ) {
                Text("Save Changes")
            }
        }

        // SUCCESS POPUP
        if (showSuccessPopup) {

            // Fade-in animation for background
            val alphaAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0f,
                animationSpec = tween(250)
            )

            // Scale animation for popup
            val scaleAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0.95f,
                animationSpec = tween(250)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f * alphaAnim)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(12.dp),
                    modifier = Modifier
                        .padding(32.dp)
                        .graphicsLayer {
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                        }
                ) {

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 28.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // SUCCESS ICON
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFFFFFFFF),
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Folder Updated!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C24)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Your changes have been saved successfully.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { showSuccessPopup = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

    }
}
