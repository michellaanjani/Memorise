package com.mobile.memorise.ui.screen.createnew

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class FolderItem(
    val name: String,
    val color: String
)

class FolderViewModel : ViewModel() {

    // ⭐ List folder sementara (lokal tanpa backend)
    var folderList = mutableStateListOf<FolderItem>()
        private set

    // ⭐ Fungsi menambah folder baru
    fun addFolder(name: String, color: String) {
        folderList.add(
            FolderItem(
                name = name,
                color = color
            )
        )
    }
}
