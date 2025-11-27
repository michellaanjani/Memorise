package com.mobile.memorise.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MainRote(val route: String, val title: String, val icon: ImageVector) {
    object Home : MainRote("home", "Home", Icons.Filled.Home)
    object Create : MainRote("create", "Create", Icons.Filled.Add) // Tombol Tambah di tengah
    object Account : MainRote("account", "Account", Icons.Filled.Person)

    // Rute Khusus (Tidak ada di Navbar)
    object DeckDetail : MainRote("deck_detail/{folderName}", "Deck Detail", Icons.Filled.Home) {
        fun createRoute(folderName: String) = "deck_detail/$folderName"
    }
}