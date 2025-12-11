package com.mobile.memorise.ui.screen.createnew.folder

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.R
import kotlinx.coroutines.delay
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

// Warna Bantuan agar lebih konsisten
val TextBlack = Color(0xFF111827) // Hitam Pekat untuk Judul/Input
val TextGray = Color(0xFF6B7280)  // Abu-abu untuk Label
val PrimaryBlue = Color(0xFF0961F5)
val BgColor = Color(0xFFFFFFFF)   // Background Putih Bersih

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFolderScreen(
    folderId: String,
    oldName: String,
    initialColor: String,
    onBackClick: () -> Unit,
    folderViewModel: FolderViewModel
) {
    val context = LocalContext.current
    val editState = folderViewModel.editFolderState

    // State Form
    var folderName by remember { mutableStateOf(oldName) }

    // Pastikan warna ada pagarnya
    val safeInitialColor = if (initialColor.startsWith("#")) initialColor else "#$initialColor"
    var selectedColor by remember { mutableStateOf(safeInitialColor) }

    var showSuccessPopup by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val colorOptions = listOf("#E1FFBF", "#E8E9FE", "#FFF3CD", "#FFE4E1", "#E0F7FA")

    // Logic Validasi
    val isChanged = folderName.trim() != oldName || selectedColor != safeInitialColor
    val isFormValid = folderName.isNotBlank() && localError == null && isChanged
    val isLoading = editState is EditFolderState.Loading

    LaunchedEffect(editState) {
        when (editState) {
            is EditFolderState.Success -> {
                showSuccessPopup = true
                delay(1500)
                folderViewModel.resetEditState()
                onBackClick()
            }
            is EditFolderState.Error -> {
                if (editState.field == null) {
                    Toast.makeText(context, editState.message, Toast.LENGTH_LONG).show()
                    folderViewModel.resetEditState()
                }
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Scaffold(
            containerColor = BgColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Edit Folder",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextBlack // Pastikan Hitam
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = TextBlack
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- 1. INPUT NAME SECTION ---
                Text(
                    text = "Folder Name",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                val displayError = if (editState is EditFolderState.Error && editState.field == "name") {
                    editState.message
                } else localError
                val isError = displayError != null

                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        if (editState is EditFolderState.Error) folderViewModel.resetEditState()
                        localError = if (it.trim().isEmpty()) "Name cannot be empty" else null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = isError,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = TextBlack, // Teks Input Hitam Pekat
                        fontWeight = FontWeight.SemiBold
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        errorBorderColor = Color.Red,
                        cursorColor = PrimaryBlue,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    ),
                    placeholder = { Text("Enter folder name", color = Color.LightGray) }
                )

                if (isError) {
                    Text(
                        text = displayError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 2. COLOR PICKER SECTION (Redesigned) ---
                Text(
                    text = "Folder Color",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // Ratakan lingkaran
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorOptions.forEach { hex ->
                        val isSelected = selectedColor == hex
                        val colorInt = Color(android.graphics.Color.parseColor(hex))

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(colorInt)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedColor = hex }
                                .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // PERBAIKAN DI SINI:
                            // Gunakan animasi explisit (scaleIn + fadeIn) agar tidak error scope
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

                // --- 3. SAVE BUTTON (Bottom) ---
                Button(
                    onClick = {
                        folderViewModel.updateFolder(folderId, folderName.trim(), selectedColor)
                    },
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
                        disabledContainerColor = Color(0xFFE5E7EB), // Abu-abu saat disabled
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF9CA3AF)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // --- SUCCESS POPUP (Sama seperti sebelumnya) ---
        if (showSuccessPopup) {
            val alphaAnim by animateFloatAsState(targetValue = if (showSuccessPopup) 1f else 0f, animationSpec = tween(250))
            val scaleAnim by animateFloatAsState(targetValue = if (showSuccessPopup) 1f else 0.95f, animationSpec = tween(250))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * alphaAnim)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .padding(32.dp)
                        .graphicsLayer { scaleX = scaleAnim; scaleY = scaleAnim }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFFDCFCE7)), // Hijau Muda Halus
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF166534), modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Success!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Folder updated successfully.", fontSize = 14.sp, color = TextGray, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}