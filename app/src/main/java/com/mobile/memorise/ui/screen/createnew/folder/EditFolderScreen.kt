package com.mobile.memorise.ui.screen.createnew.folder

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.delay

// --- Definisi Warna ---
private val TextBlack = Color(0xFF111827)
private val TextGray = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF0961F5)
private val BgColor = Color(0xFFFFFFFF)

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

    // Mengambil state edit dari ViewModel
    val editState by folderViewModel.editFolderState.collectAsState()

    // --- 1. State Form ---
    var folderName by remember { mutableStateOf(oldName) }

    // Format warna aman (tambah # jika belum ada)
    val safeInitialColor = if (initialColor.startsWith("#")) initialColor else "#$initialColor"
    var selectedColor by remember { mutableStateOf(safeInitialColor) }

    var showSuccessPopup by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val colorOptions = listOf("#E1FFBF", "#E8E9FE", "#FFF3CD", "#FFE4E1", "#E0F7FA")

    // --- 2. Logic Validasi ---
    val isChanged = folderName.trim() != oldName || selectedColor != safeInitialColor
    val isFormValid = folderName.isNotBlank() && localError == null && isChanged

    // Cek Loading
    val isLoading = editState is Resource.Loading

    // --- 3. Side Effects (API Response) ---
    LaunchedEffect(editState) {
        when (val state = editState) {
            is Resource.Success -> {
                // 1. Munculkan Popup
                showSuccessPopup = true
                // 2. Tunggu
                delay(1500)
                // 3. Reset State & Balik
                folderViewModel.resetEditState()
                onBackClick()
            }
            is Resource.Error -> {
                // Tampilkan pesan error bersih
                Toast.makeText(context, state.message ?: "An error occurred", Toast.LENGTH_LONG).show()
                // Reset state agar tidak stuck di error, tapi jangan keluar layar
                folderViewModel.resetEditState()
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
                            color = TextBlack
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

                // --- INPUT NAME SECTION ---
                Text(
                    text = "Folder Name",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                val isError = localError != null

                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        // Validasi lokal saat mengetik
                        localError = if (it.trim().isEmpty()) "Name cannot be empty" else null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = isError,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = TextBlack,
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

                // Error Message Animation
                AnimatedVisibility(visible = isError) {
                    Text(
                        text = localError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- COLOR PICKER SECTION ---
                Text(
                    text = "Folder Color",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorOptions.forEach { hex ->
                        val isSelected = selectedColor == hex
                        val colorInt = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            Color.Gray
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(colorInt)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedColor = hex }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PrimaryBlue else Color.Black.copy(0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
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

                // --- SAVE BUTTON ---
                Button(
                    onClick = {
                        // Memanggil fungsi update di ViewModel
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
                        disabledContainerColor = Color(0xFFE5E7EB),
                        contentColor = Color.White,
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
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // --- SUCCESS POPUP OVERLAY ---
        if (showSuccessPopup) {
            val alphaAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0f,
                animationSpec = tween(250), label = "alpha"
            )
            val scaleAnim by animateFloatAsState(
                targetValue = if (showSuccessPopup) 1f else 0.95f,
                animationSpec = tween(250), label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * alphaAnim))
                    // Block clicks di belakang popup
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
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Folder updated successfully.",
                            fontSize = 14.sp,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}