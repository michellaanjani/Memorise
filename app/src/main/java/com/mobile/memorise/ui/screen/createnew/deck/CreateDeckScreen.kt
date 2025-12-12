package com.mobile.memorise.ui.screen.createnew.deck

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.memorise.ui.viewmodel.DeckRemoteViewModel
import com.mobile.memorise.util.Resource

@Composable
fun CreateDeckScreen(
    navController: NavHostController,
    folderId: String? = null,
    onBackClick: () -> Unit,
    deckRemoteViewModel: DeckRemoteViewModel = hiltViewModel()
) {

    var deckName by remember { mutableStateOf("") }
    var deckError by remember { mutableStateOf<String?>(null) }
    val mutationState by deckRemoteViewModel.deckMutationState.collectAsState()

    LaunchedEffect(mutationState) {
        if (mutationState is Resource.Success) {
            navController.popBackStack()
        }
    }

    val isFormValid =
        deckName.isNotBlank() &&
                deckError == null &&
                mutationState !is Resource.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {

        Spacer(modifier = Modifier.height(52.dp))

        // TOP BAR — sama persis Create Folder
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
                "Add New Deck",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(26.dp))

        // BANNER IMAGES — tetap sama
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            ImageDeckCard(
                drawableId = R.drawable.memorize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
            )
            ImageDeckCard(
                drawableId = R.drawable.learn,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FORM CONTAINER — sama persis, hanya ganti text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF3F3FA))
                .padding(26.dp)
        ) {

            Row {
                Text("Deck Name", fontWeight = FontWeight.SemiBold)
                Text("*", color = Color(0xFFC53636))
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = deckName,
                onValueChange = {
                    deckName = it
                    deckError = null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = deckError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor =
                        if (deckError != null) Color.Red else Color(0xFFDBE1F3),
                    focusedBorderColor =
                        if (deckError != null) Color.Red else Color(0xFF0961F5),
                    cursorColor = Color(0xFF0961F5)
                )
            )

            if (deckError != null) {
                Text(
                    deckError!!,
                    color = Color(0xFFFF6905),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // BUTTON — sama persis, hanya ganti text
        Button(
            onClick = {
                deckRemoteViewModel.createDeck(
                    name = deckName.trim(),
                    description = null,
                    folderId = folderId
                )
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
                "Add New Deck",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ImageDeckCard(
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
