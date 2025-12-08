package com.mobile.memorise.ui.screen.createnew.folder

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class FolderItem(
    val name: String,
    val color: String
)

class FolderViewModel : ViewModel() {

    var folderList = mutableStateListOf<FolderItem>()
        private set

    fun addFolder(name: String, color: String) {
        folderList.add(FolderItem(name, color))
    }

    fun updateFolder(oldName: String, newName: String, newColor: String) {
        val idx = folderList.indexOfFirst { it.name == oldName }
        if (idx >= 0) {
            folderList[idx] = FolderItem(newName, newColor)
        }
    }

    // ✔ FIXED — delete pakai `name`, sesuai HomeScreen
    fun deleteFolder(name: String) {
        folderList.removeAll { it.name == name }
        println("Deleted folder: $name")
    }
}
