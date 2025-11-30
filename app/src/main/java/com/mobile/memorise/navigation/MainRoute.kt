package com.mobile.memorise.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

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
}