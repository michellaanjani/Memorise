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
    object DeckDetail : MainRoute("deck_detail/{folderName}", "Deck Detail", Icons.Filled.Home) {
        fun createRoute(folderName: String) = "deck_detail/$folderName"
    }

    object Cards : MainRoute("cards/{deckName}", "Cards", Icons.Filled.Home) {
        fun createRoute(deckName: String) = "cards/$deckName"
    }

    // --- TAMBAHAN BARU ---
    object Study : MainRoute("study/{deckName}/{cardList}", "Study", Icons.Filled.Home) {
        // Fungsi ini dipakai saat tombol Study diklik
        fun createRoute(deckName: String, cardListJson: String) = "study/$deckName/$cardListJson"
    }

    object Quiz : MainRoute("quiz/{deckName}/{cardList}", "Quiz", Icons.Filled.Home) {
        fun createRoute(deckName: String, cardListJson: String) = "quiz/$deckName/$cardListJson"
    }

    object CardDetail : MainRoute("detail_card/{deckName}/{cardList}/{index}", "Detail", Icons.Filled.Home) {
        fun createRoute(deckName: String, encodedJson: String, index: Int): String {
            return "detail_card/$deckName/$encodedJson/$index"
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
        route = "create_deck",
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
        route = "edit_deck/{oldName}",
        title = "Edit Deck",
        icon = Icons.Default.Edit
    ) {
        fun createRoute(oldName: String) = "edit_deck/$oldName"
    }

    object AddCard : MainRoute(
        route = "add_card/{deckName}",
        title = "Add Card",
        icon = Icons.Default.Add
    ) {
        fun createRoute(deckName: String) = "add_card/$deckName"
    }

    object EditCard : MainRoute(
        route = "edit_card/{deckName}/{index}/{json}",
        title = "Edit Card",
        icon = Icons.Default.Edit
    ) {
        fun createRoute(deckName: String, index: Int, json: String): String {
            val encodedJson = Uri.encode(json)
            return "edit_card/$deckName/$index/$encodedJson"
        }
    }

    // ==============================
// AI GENERATION ROUTES (FINAL FIX)
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






