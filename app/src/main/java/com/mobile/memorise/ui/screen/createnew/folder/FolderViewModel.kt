package com.mobile.memorise.ui.screen.createnew.folder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mobile.memorise.domain.model.CreateFolderRequest // Pastikan import ini benar
import com.mobile.memorise.domain.model.UpdateFolderRequest // Pastikan import ini benar
import com.mobile.memorise.domain.model.ErrorResponse
import com.mobile.memorise.domain.model.FolderData
import com.mobile.memorise.data.remote.api.FolderApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- STATES ---

sealed class CreateFolderState {
    object Idle : CreateFolderState()
    object Loading : CreateFolderState()
    object Success : CreateFolderState()
    data class Error(val message: String, val field: String? = null) : CreateFolderState()
}

sealed class EditFolderState {
    object Idle : EditFolderState()
    object Loading : EditFolderState()
    object Success : EditFolderState()
    data class Error(val message: String, val field: String? = null) : EditFolderState()
}

// --- VIEW MODEL ---

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val apiService: FolderApi
) : ViewModel() {

    // State untuk Create Folder
    var createFolderState by mutableStateOf<CreateFolderState>(CreateFolderState.Idle)
        private set

    // State untuk Edit Folder
    var editFolderState by mutableStateOf<EditFolderState>(EditFolderState.Idle)
        private set

    // List folder lokal (gunakan mutableStateListOf agar UI otomatis update jika list berubah)
    var folderList = mutableStateListOf<FolderData>()
        private set

    // --- CREATE FOLDER (POST) ---
    fun addFolder(name: String, color: String) {
        viewModelScope.launch {
            createFolderState = CreateFolderState.Loading

            try {
                val request = CreateFolderRequest(
                    name = name,
                    description = "Created via App", // Default description
                    color = color
                )

                val response = apiService.createFolder(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val newFolder = response.body()!!.data
                    if (newFolder != null) {
                        folderList.add(0, newFolder) // Tambah ke paling atas list lokal
                    }
                    createFolderState = CreateFolderState.Success
                } else {
                    handleCreateError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                createFolderState = CreateFolderState.Error("Connection error: ${e.message}")
            }
        }
    }

    // --- UPDATE FOLDER (PATCH) ---
    fun updateFolder(id: String, name: String, color: String) {
        viewModelScope.launch {
            editFolderState = EditFolderState.Loading

            try {
                val request = UpdateFolderRequest(name = name, color = color)
                val response = apiService.updateFolder(id, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedFolder = response.body()!!.data

                    // Update list lokal jika data ditemukan
                    if (updatedFolder != null) {
                        val index = folderList.indexOfFirst { it.id == id }
                        if (index != -1) {
                            folderList[index] = updatedFolder
                        }
                    }
                    editFolderState = EditFolderState.Success
                } else {
                    handleEditError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                editFolderState = EditFolderState.Error("Connection error: ${e.message}")
            }
        }
    }

    // --- ERROR HANDLERS ---

    private fun handleCreateError(errorBody: String?) {
        try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            createFolderState = if (errorResponse.message.contains("conflict", ignoreCase = true) ||
                errorResponse.message.contains("exists", ignoreCase = true)) {
                CreateFolderState.Error("Folder name already exists", field = "name")
            } else {
                CreateFolderState.Error(errorResponse.message)
            }
        } catch (e: Exception) {
            createFolderState = CreateFolderState.Error("An unknown error occurred")
        }
    }

    private fun handleEditError(errorBody: String?) {
        try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            editFolderState = if (errorResponse.message.contains("conflict", ignoreCase = true) ||
                errorResponse.message.contains("exists", ignoreCase = true)) {
                EditFolderState.Error("Folder name already exists", field = "name")
            } else {
                EditFolderState.Error(errorResponse.message)
            }
        } catch (e: Exception) {
            editFolderState = EditFolderState.Error("An unknown error occurred")
        }
    }

    // --- RESET STATES ---

    fun resetState() {
        createFolderState = CreateFolderState.Idle
    }

    fun resetEditState() {
        editFolderState = EditFolderState.Idle
    }

    fun deleteFolder(id: String) {
        viewModelScope.launch {
            try {
                // Kita tidak perlu state loading khusus di sini
                // karena UI akan refresh data via HomeViewModel setelah hapus
                val response = apiService.deleteFolder(id)
                if (!response.isSuccessful) {
                    // Opsional: Handle error delete (misal Toast)
                }
            } catch (e: Exception) {
                // Handle connection error
            }
        }
    }
}