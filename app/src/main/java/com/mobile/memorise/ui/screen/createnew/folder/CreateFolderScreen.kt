package com.mobile.memorise.ui.screen.createnew.folder

import android.graphics.Color as AndroidColor
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.delay

// Definisi Warna Lokal
private val BgColor = Color.White
private val TextBlack = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF0C3DF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFolderScreen(
    navController: NavHostController,
    folderViewModel: FolderViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Collect State
    val apiState by folderViewModel.createFolderState.collectAsState()

    // State Form
    var folderName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<String?>("#E1FFBF") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var showSuccessPopup by remember { mutableStateOf(false) }

    val colorOptions = listOf("#E1FFBF", "#E8E9FE", "#FFF3CD", "#FFE4E1", "#E0F7FA")

    // Logic Validasi
    val isFormValid = folderName.isNotBlank() && selectedColor != null
    val isLoading = apiState is Resource.Loading

    // --- SIDE EFFECTS: Handle Success / Error ---
    LaunchedEffect(apiState) {
        when (val state = apiState) {
            is Resource.Success -> {
                // 1. Tampilkan Popup
                showSuccessPopup = true
                // 2. Tunggu 1.5 detik
                delay(1500)
                // 3. Reset State & Navigasi Balik
                folderViewModel.resetState() // Pastikan fungsi ini ada di VM (untuk reset createFolderState)
                onBackClick()
            }
            is Resource.Error -> {
                // Tampilkan pesan error bersih
                Toast.makeText(context, state.message ?: "Unknown error", Toast.LENGTH_LONG).show()
                // Jangan reset state loading agar user bisa edit lagi,
                // tapi jika VM mengharuskan reset agar status error hilang dari flow, lakukan di sini:
                // folderViewModel.resetState()
            }
            else -> {} // Idle or Loading
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
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
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
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

                // --- NAME INPUT ---
                Text("Folder Name", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        nameError = if (it.isBlank()) "Required" else null
                        // Opsional: Reset error VM jika user mengetik
                        if (apiState is Resource.Error) folderViewModel.resetState()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = nameError != null,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = TextBlack,
                        fontWeight = FontWeight.SemiBold
                    ),
                    placeholder = { Text("e.g. Work Projects", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        errorBorderColor = Color.Red,
                        cursorColor = PrimaryBlue,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                // Error Text Animation
                AnimatedVisibility(visible = nameError != null) {
                    Text(
                        text = nameError ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- DESCRIPTION INPUT ---
                Text("Description", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = TextBlack),
                    placeholder = { Text("Optional description...", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- COLOR PICKER ---
                Text("Folder Color", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextGray)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorOptions.forEach { hex ->
                        val isSelected = selectedColor == hex
                        val colorInt = try { Color(AndroidColor.parseColor(hex)) } catch (e: Exception) { Color.Gray }

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

                // --- BUTTON ---
                Button(
                    onClick = {
                        if (isFormValid) {
                            folderViewModel.createFolder(folderName.trim(), description.trim(), selectedColor!!)
                        }
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
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFB3D4FC),
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                            "Folder created successfully.",
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