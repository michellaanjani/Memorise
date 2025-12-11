package com.mobile.memorise.ui.screen.createnew.folder

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.core.graphics.toColorInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderScreen(
    navController: NavHostController,
    folderViewModel: FolderViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val apiState = folderViewModel.createFolderState

    var folderName by remember { mutableStateOf("") }
    // Default pilih warna pertama agar user tidak bingung
    var selectedColor by remember { mutableStateOf<String?>("#E1FFBF") }
    var localError by remember { mutableStateOf<String?>(null) }

    val colorOptions = listOf("#E1FFBF", "#E8E9FE", "#FFF3CD", "#FFE4E1", "#E0F7FA")

    // --- Logic API ---
    LaunchedEffect(apiState) {
        when (apiState) {
            is CreateFolderState.Success -> {
                Toast.makeText(context, "✨ Folder created successfully!", Toast.LENGTH_SHORT).show()
                folderViewModel.resetState()
                navController.popBackStack()
            }
            is CreateFolderState.Error -> {
                if (apiState.field == null) {
                    Toast.makeText(context, apiState.message, Toast.LENGTH_LONG).show()
                    folderViewModel.resetState()
                }
            }
            else -> {}
        }
    }

    val isFormValid = folderName.isNotBlank() && selectedColor != null && localError == null
    val isLoading = apiState is CreateFolderState.Loading

    Scaffold(
        containerColor = BgColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "New Folder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextBlack
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp),
                            tint = TextBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgColor
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Spacer(modifier = Modifier.height(32.dp))

            // --- FORM INPUT NAME ---
            Text(
                text = "Folder Name",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            val displayError = if (apiState is CreateFolderState.Error && apiState.field == "name") {
                apiState.message
            } else {
                localError
            }
            val isError = displayError != null

            OutlinedTextField(
                value = folderName,
                onValueChange = {
                    folderName = it
                    if (apiState is CreateFolderState.Error) folderViewModel.resetState()
                    localError = if (it.trim().isEmpty()) "Name is required" else null
                },
                placeholder = { Text("e.g. Biology 101", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = isError,
                // PERBAIKAN PENTING: Warna Teks Hitam
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextBlack
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    errorBorderColor = Color.Red,
                    cursorColor = PrimaryBlue,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFFAFAFA)
                )
            )

            // Error Message Animation
            AnimatedVisibility(
                visible = isError,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Text(
                    text = displayError ?: "",
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- COLOR PICKER (Redesigned) ---
            Text(
                text = "Folder Color",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextGray
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Ratakan spasi
                verticalAlignment = Alignment.CenterVertically
            ) {
                colorOptions.forEach { hex ->
                    val isSelected = selectedColor == hex
                    val colorInt = Color(hex.toColorInt())

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(colorInt)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedColor = hex }
                            .border(1.dp, Color.Black.copy(0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Animasi Centang (Fix RowScope Error dengan enter/exit explisit)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSelected,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.Black.copy(0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- SUBMIT BUTTON ---
            Button(
                onClick = { folderViewModel.addFolder(folderName.trim(), selectedColor!!) },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = if (isFormValid) 4.dp else 0.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = PrimaryBlue.copy(0.4f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    // Warna saat tombol mati (disabled) lebih terlihat
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF9CA3AF)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        "Create Folder",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

