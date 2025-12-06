package com.mobile.memorise.ui.screen.createnew

import androidx.compose.foundation.Image
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
import androidx.navigation.NavHostController
import com.mobile.memorise.R

@Composable
fun CreateFolderScreen(
    navController: NavHostController,
    folderViewModel: FolderViewModel,
    onBackClick: () -> Unit
) {

    var folderName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var folderError by remember { mutableStateOf<String?>(null) }

    val colorOptions = listOf("#E1FFBF", "#E8E9FE", "#FFF3CD")

    val isFormValid =
        folderName.isNotBlank() &&
                selectedColor != null &&
                folderError == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {

        Spacer(modifier = Modifier.height(52.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() }
            )

            Text(
                "Add New Folder",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(26.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            ImageFolderCard(
                drawableId = R.drawable.memorize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
            )
            ImageFolderCard(
                drawableId = R.drawable.learn,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF3F3FA))
                .padding(26.dp)
        ) {

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
                            folderViewModel.folderList.any { f ->
                                f.name.equals(trimmed, ignoreCase = true)
                            }
                        ) {
                            "Folder name already exists!"
                        } else null
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

            Row {
                Text("Icon & color", fontWeight = FontWeight.SemiBold)
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
                        .background(
                            Color(
                                android.graphics.Color.parseColor(
                                    selectedColor ?: "#C4C4C4"
                                )
                            )
                        ),
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
                                color = if (selectedColor == hex) Color(0xFF0035A0)
                                else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                folderViewModel.addFolder(folderName.trim(), selectedColor!!)
                navController.popBackStack()
            },
            enabled = isFormValid,
            modifier = Modifier
                .width(200.dp)
                .height(50.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor =
                    if (isFormValid) Color(0xFF0961F5) else Color(0xFFB9C4FF)
            )
        ) {
            Text(
                "Add New Folder",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ImageFolderCard(
    drawableId: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}
