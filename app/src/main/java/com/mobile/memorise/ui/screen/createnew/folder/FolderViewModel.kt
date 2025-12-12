package com.mobile.memorise.ui.screen.createnew.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.model.Folder
import com.mobile.memorise.domain.repository.ContentRepository
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {

    // ==========================================
    // STATE DECLARATION
    // ==========================================

    private val _createFolderState = MutableStateFlow<Resource<Folder>?>(null)
    val createFolderState: StateFlow<Resource<Folder>?> = _createFolderState.asStateFlow()

    private val _editFolderState = MutableStateFlow<Resource<Folder>?>(null)
    val editFolderState: StateFlow<Resource<Folder>?> = _editFolderState.asStateFlow()

    private val _deleteFolderState = MutableStateFlow<Resource<Boolean>?>(null)
    val deleteFolderState: StateFlow<Resource<Boolean>?> = _deleteFolderState.asStateFlow()

    // ==========================================
    // ACTIONS
    // ==========================================

    fun createFolder(name: String, description: String, color: String) {
        viewModelScope.launch {
            _createFolderState.value = Resource.Loading()
            val result = repository.createFolder(name, description, color)

            result.onSuccess { folder ->
                _createFolderState.value = Resource.Success(folder)
            }.onFailure { error ->
                _createFolderState.value = Resource.Error(error.message ?: "Gagal membuat folder")
            }
        }
    }

    fun updateFolder(folderId: String, name: String, color: String) {
        viewModelScope.launch {
            _editFolderState.value = Resource.Loading()
            // Kirim string kosong untuk deskripsi karena tidak diedit di UI ini
            val result = repository.updateFolder(folderId, name, "", color)

            result.onSuccess { folder ->
                _editFolderState.value = Resource.Success(folder)
            }.onFailure { error ->
                _editFolderState.value = Resource.Error(error.message ?: "Gagal update folder")
            }
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            _deleteFolderState.value = Resource.Loading()
            repository.deleteFolder(folderId)
                .onSuccess {
                    _deleteFolderState.value = Resource.Success(true)
                }
                .onFailure { error ->
                    _deleteFolderState.value = Resource.Error(error.message ?: "Gagal menghapus folder")
                }
        }
    }

    // ==========================================
    // RESET STATES
    // ==========================================

    // PERBAIKAN: Menambahkan fungsi ini agar sesuai dengan UI CreateFolderScreen
    fun resetState() {
        _createFolderState.value = null
    }

    fun resetEditState() {
        _editFolderState.value = null
    }

    fun resetDeleteState() {
        _deleteFolderState.value = null
    }
}