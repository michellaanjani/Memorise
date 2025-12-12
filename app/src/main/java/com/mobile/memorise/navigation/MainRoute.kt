package com.mobile.memorise.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// MainRoute requires (route, title, icon)
sealed class MainRoute(val route: String, val title: String, val icon: ImageVector) {
    object Home : MainRoute("home", "Home", Icons.Filled.Home)
    object Create : MainRoute("create", "Create", Icons.Filled.Add)
    object Account : MainRoute("account", "Account", Icons.Filled.Person)
    object EditProfile : MainRoute("edit_profile", "Edit Profile", Icons.Filled.Person)
    object EditPassword : MainRoute("edit_password", "Edit Password", Icons.Filled.Person)

    // Rute Khusus (Tidak ada di Navbar)
    object DeckDetail : MainRoute(
        "deck_detail/{folderName}/{folderId}",
        "Deck Detail",
        Icons.Filled.Home
    ) {
        fun createRoute(folderName: String, folderId: String) = "deck_detail/$folderName/$folderId"
    }

    object Cards : MainRoute("cards/{deckId}/{deckName}", "Cards", Icons.Filled.Home) {
        fun createRoute(deckId: String, deckName: String) = "cards/$deckId/$deckName"
    }

    object Study : MainRoute("study/{deckId}/{deckName}", "Study", Icons.Filled.Home) {
        fun createRoute(deckId: String, deckName: String) = "study/$deckId/$deckName"
    }

    object Quiz : MainRoute("quiz/{deckId}/{deckName}", "Quiz", Icons.Filled.Home) {
        fun createRoute(deckId: String, deckName: String) = "quiz/$deckId/$deckName"
    }

    // Menggunakan versi Stashed (dengan deckId) agar sinkron dengan Database
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

    // Create Deck
    object CreateDeck : MainRoute(
        route = "create_deck?folderId={folderId}",
        title = "Create Deck",
        icon = Icons.Filled.Add
    ) {
        fun createDeckWithFolder(id: String) = "create_deck?folderId=$id"
        fun createDeckNoFolder() = "create_deck"
    }

    // Move Deck
    object MoveDeck : MainRoute(
        "move_deck/{deckId}",
        title = "Move Deck",
        icon = Icons.Filled.Edit
    ) {
        fun createRoute(deckId: String) = "move_deck/$deckId"
    }

    object EditFolder : MainRoute(
        route = "edit_folder/{idFolder}/{oldName}/{color}",
        title = "Edit Folder",
        icon = Icons.Filled.Edit
    ) {
        fun createRoute(idFolder: String, oldName: String, color: String): String {
            return "edit_folder/${Uri.encode(idFolder)}/${Uri.encode(oldName)}/${Uri.encode(color)}"
        }
    }

    object EditDeck : MainRoute(
        route = "edit_deck/{deckId}",
        title = "Edit Deck",
        icon = Icons.Filled.Edit
    ) {
        fun createRoute(deckId: String) = "edit_deck/$deckId"
    }

    object AddCard : MainRoute(
        route = "add_card/{deckId}/{deckName}",
        title = "Add Card",
        icon = Icons.Filled.Add
    ) {
        fun createRoute(deckId: String, deckName: String) = "add_card/$deckId/$deckName"
    }

    // Menggunakan versi Stashed (dengan deckId)
    object EditCard : MainRoute(
        route = "edit_card/{deckId}/{deckName}/{index}/{json}",
        title = "Edit Card",
        icon = Icons.Filled.Edit
    ) {
        fun createRoute(deckId: String, deckName: String, index: Int, json: String): String {
            // Pastikan JSON di-encode di sini agar aman
            return "edit_card/$deckId/$deckName/$index/${Uri.encode(json)}"
        }
    }

    // ==============================
    // AI GENERATION ROUTES (NEW)
    // Diambil dari Upstream (GitHub)
    // ==============================
    object AiDraft : MainRoute(
        route = "ai_draft/{deckName}/{cardsJson}",
        title = "AI Draft",
        icon = Icons.Filled.Home
    ) {
        fun createRoute(deckName: String, cardsJson: String): String {
            return "ai_draft/${Uri.encode(deckName)}/${Uri.encode(cardsJson)}"
        }
    }

    object AiCardDetail : MainRoute(
        route = "ai_card_detail/{index}/{cardsJson}",
        title = "AI Card Detail",
        icon = Icons.Filled.Home
    ) {
        fun createRoute(index: Int, cardsJson: String): String {
            return "ai_card_detail/$index/${Uri.encode(cardsJson)}"
        }
    }

    object AiEditCard : MainRoute(
        route = "ai_edit_card/{index}/{cardsJson}",
        title = "AI Edit Card",
        icon = Icons.Filled.Edit
    ) {
        fun createRoute(index: Int, cardsJson: String): String {
            return "ai_edit_card/$index/${Uri.encode(cardsJson)}"
        }
    }
}