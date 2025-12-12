package com.mobile.memorise.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector
import android.net.Uri


// MainRoute requires (route, title, icon)
sealed class MainRoute(val route: String, val title: String, val icon: ImageVector) {
    object Home : MainRoute("home", "Home", Icons.Filled.Home)
    object Create : MainRoute("create", "Create", Icons.Filled.Add) // Tombol Tambah di tengah
    object Account : MainRoute("account", "Account", Icons.Filled.Person)
    object EditProfile : MainRoute("edit_profile", "Edit Profile", Icons.Filled.Person)
    object EditPassword : MainRoute("edit_password", "Edit Password", Icons.Filled.Person)

    // Rute Khusus (Tidak ada di Navbar)
    object DeckDetail : MainRoute("deck_detail/{folderId}/{folderName}", "Deck Detail", Icons.Filled.Home) {
        fun createRoute(folderId: String, folderName: String) = "deck_detail/$folderId/$folderName"
    }

    object Cards : MainRoute("cards/{deckId}/{deckName}", "Cards", Icons.Filled.Home) {
        fun createRoute(deckId: String, deckName: String) = "cards/$deckId/$deckName"
    }

    // --- TAMBAHAN BARU ---
    object Study : MainRoute("study/{deckId}/{deckName}", "Study", Icons.Filled.Home) {
        fun createRoute(deckId: String, deckName: String) = "study/$deckId/$deckName"
    }

    object Quiz : MainRoute("quiz/{deckId}/{deckName}", "Quiz", Icons.Filled.Home) {
        fun createRoute(deckId: String, deckName: String) = "quiz/$deckId/$deckName"
    }

    object CardDetail : MainRoute("detail_card/{deckId}/{deckName}/{cardList}/{index}", "Detail", Icons.Filled.Home) {
        fun createRoute(deckId: String, deckName: String, encodedJson: String, index: Int): String {
            return "detail_card/$deckId/$deckName/$encodedJson/$index"
        }
    }



    object AiGeneration : MainRoute(
        route = "ai_generation",
        title = "AI Generation",
        icon = Icons.Filled.Home
    )

    object CameraScreen : MainRoute(
        route = "camera_screen",
        title = "Camera Screen",
        icon = Icons.Filled.Home
    )

    // Create folder
    object CreateFolder : MainRoute(
        route = "create_folder",
        title = "Create Folder",
        icon = Icons.Filled.Folder
    )

    object CreateDeck : MainRoute(
        route = "create_deck?folderId={folderId}&folderName={folderName}",
        title = "Create Deck",
        icon = Icons.Filled.Add
    )

    object EditFolder : MainRoute(
        route = "edit_folder/{oldName}/{color}",
        title = "Edit Folder",
        icon = Icons.Filled.Edit
    ) {
        fun createRoute(oldName: String, color: String): String {
            return "edit_folder/${Uri.encode(oldName)}/${Uri.encode(color)}"
        }
    }

    object EditDeck : MainRoute(
        route = "edit_deck/{deckId}/{deckName}",
        title = "Edit Deck",
        icon = Icons.Default.Edit
    ) {
        fun createRoute(deckId: String, deckName: String) = "edit_deck/$deckId/$deckName"
    }

    object AddCard : MainRoute(
        route = "add_card/{deckId}/{deckName}",
        title = "Add Card",
        icon = Icons.Default.Add
    ) {
        fun createRoute(deckId: String, deckName: String) = "add_card/$deckId/$deckName"
    }

    object EditCard : MainRoute(
        route = "edit_card/{deckId}/{deckName}/{index}/{json}",
        title = "Edit Card",
        icon = Icons.Default.Edit
    ) {
        fun createRoute(deckId: String, deckName: String, index: Int, json: String): String {
            val encodedJson = Uri.encode(json)
            return "edit_card/$deckId/$deckName/$index/$encodedJson"
        }
    }
}






