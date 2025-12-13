package com.mobile.memorise.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MainRoute(val route: String, val title: String, val icon: ImageVector) {

    // ============================================================
    // 1. MAIN MENU & PROFILE
    // ============================================================
    object Home : MainRoute("home", "Home", Icons.Filled.Home)
    object Create : MainRoute("create", "Create", Icons.Filled.Add)
    object Account : MainRoute("account", "Account", Icons.Filled.Person)
    object EditProfile : MainRoute("edit_profile", "Edit Profile", Icons.Filled.ManageAccounts)
    object EditPassword : MainRoute("edit_password", "Edit Password", Icons.Filled.Lock)

    // ============================================================
    // 2. CONSUMPTION ROUTES (Detail, Study, Quiz)
    // ============================================================

    // Masuk ke dalam folder
    object DeckDetail : MainRoute("deck_detail/{folderName}/{folderId}", "Deck Detail", Icons.Filled.FolderOpen) {
        fun createRoute(folderName: String, folderId: String) =
            "deck_detail/${Uri.encode(folderName)}/$folderId"
    }

    // List Kartu dalam Deck
    object Cards : MainRoute("cards/{deckId}/{deckName}", "Cards", Icons.Filled.Style) {
        fun createRoute(deckId: String, deckName: String) =
            "cards/$deckId/${Uri.encode(deckName)}"
    }

    // Mode Belajar
    object Study : MainRoute("study/{deckId}/{deckName}", "Study", Icons.Filled.School) {
        fun createRoute(deckId: String, deckName: String) =
            "study/$deckId/${Uri.encode(deckName)}"
    }

    // Mode Kuis
    object Quiz : MainRoute("quiz/{deckId}/{deckName}", "Quiz", Icons.Filled.Quiz) {
        fun createRoute(deckId: String, deckName: String) =
            "quiz/$deckId/${Uri.encode(deckName)}"
    }

    // Detail Kartu (Swipe View)
    object CardDetail : MainRoute("detail_card/{deckId}/{deckName}/{cardList}/{index}", "Detail", Icons.Filled.ViewCarousel) {
        // Penting: Encode JSON di sini agar navigasi tidak crash
        fun createRoute(deckId: String, deckName: String, jsonList: String, index: Int) =
            "detail_card/$deckId/${Uri.encode(deckName)}/${Uri.encode(jsonList)}/$index"
    }

    // ============================================================
    // 3. MANAGEMENT ROUTES (Create, Edit, Move)
    // ============================================================

    object CreateFolder : MainRoute("create_folder", "Create Folder", Icons.Filled.CreateNewFolder)

    object CreateDeck : MainRoute("create_deck?folderId={folderId}", "Create Deck", Icons.Filled.AddBox) {
        fun createDeckWithFolder(id: String) = "create_deck?folderId=$id"
        fun createDeckNoFolder() = "create_deck"
    }

    object MoveDeck : MainRoute("move_deck/{deckId}", "Move Deck", Icons.Filled.DriveFileMove) {
        fun createRoute(deckId: String) = "move_deck/$deckId"
    }

    object EditFolder : MainRoute("edit_folder/{idFolder}/{oldName}/{color}", "Edit Folder", Icons.Filled.Edit) {
        // Encode color karena '#' adalah karakter spesial di URL
        fun createRoute(idFolder: String, oldName: String, color: String) =
            "edit_folder/$idFolder/${Uri.encode(oldName)}/${Uri.encode(color)}"
    }

    object EditDeck : MainRoute("edit_deck/{deckId}", "Edit Deck", Icons.Filled.Edit) {
        fun createRoute(deckId: String) = "edit_deck/$deckId"
    }

    object AddCard : MainRoute("add_card/{deckId}/{deckName}", "Add Card", Icons.Filled.PostAdd) {
        fun createRoute(deckId: String, deckName: String) =
            "add_card/$deckId/${Uri.encode(deckName)}"
    }

    object EditCard : MainRoute("edit_card/{deckId}/{deckName}/{index}/{json}", "Edit Card", Icons.Filled.EditNote) {
        // Encode JSON di sini
        fun createRoute(deckId: String, deckName: String, index: Int, json: String) =
            "edit_card/$deckId/${Uri.encode(deckName)}/$index/${Uri.encode(json)}"
    }

    // ============================================================
    // 4. AI FEATURE ROUTES
    // ============================================================

    object AiGeneration : MainRoute("ai_generation", "AI Generation", Icons.Filled.AutoAwesome)

    object CameraScreen : MainRoute("camera_screen", "Camera", Icons.Filled.CameraAlt)

    // Draft sekarang menggunakan deckId (Data diambil via API/ViewModel)
    object AiDraft : MainRoute("ai_draft_screen/{deckId}", "AI Draft", Icons.Filled.ListAlt) {
        fun createRoute(deckId: String) = "ai_draft_screen/$deckId"
    }

    // Detail menggunakan index (Data diambil dari Shared ViewModel AI)
    object AiCardDetail : MainRoute("ai_card_detail/{index}", "AI Card Detail", Icons.Filled.Info) {
        fun createRoute(index: Int) = "ai_card_detail/$index"
    }

    // Edit menggunakan cardId untuk update spesifik
    object AiEditCard : MainRoute("ai_edit_card/{cardId}", "AI Edit Card", Icons.Filled.Edit) {
        fun createRoute(cardId: String) = "ai_edit_card/$cardId"
    }
}