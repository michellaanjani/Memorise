package com.mobile.memorise.ui.screen.createnew.folder

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

    fun updateFolder(oldName: String, newName: String, newColor: String) {
        val idx = folderList.indexOfFirst { it.name == oldName }
        if (idx >= 0) {
            // replace with updated FolderItem
            folderList[idx] = FolderItem(name = newName, color = newColor)
        }
    }
}
