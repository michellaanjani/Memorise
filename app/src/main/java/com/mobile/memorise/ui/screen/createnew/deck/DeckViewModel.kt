package com.mobile.memorise.ui.screen.createnew.deck

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.model.Card
import com.mobile.memorise.domain.model.Deck
import com.mobile.memorise.domain.model.Folder
import com.mobile.memorise.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ==========================================================
// STATE DEFINITIONS
// ==========================================================

// --- Deck States ---
sealed class CreateDeckState {
    data object Idle : CreateDeckState()
    data object Loading : CreateDeckState()
    data object Success : CreateDeckState()
    data class Error(val message: String) : CreateDeckState()
}

sealed class EditDeckState {
    data object Idle : EditDeckState()
    data object Loading : EditDeckState()
    data object Success : EditDeckState()
    data class Error(val message: String) : EditDeckState()
}

sealed class MoveDeckState {
    data object Idle : MoveDeckState()
    data object Loading : MoveDeckState()
    data object Success : MoveDeckState()
    data class Error(val message: String) : MoveDeckState()
}

// --- Card States ---
sealed class CardOperationState {
    data object Idle : CardOperationState()
    data object Loading : CardOperationState()
    data object Success : CardOperationState()
    data class Error(val message: String) : CardOperationState()
}

@HiltViewModel
class DeckViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {

    // ==========================================================
    // DATA HOLDERS
    // ==========================================================

    private val _decks = mutableStateListOf<Deck>()
    val decks: List<Deck> get() = _decks

    private val _folders = mutableStateListOf<Folder>()
    val folders: List<Folder> get() = _folders

    // List kartu untuk ditampilkan di layar Detail (CardsScreen)
    private val _cards = mutableStateListOf<Card>()
    val cards: List<Card> get() = _cards

    // ==========================================================
    // UI STATES
    // ==========================================================

    // State untuk Create Deck
    var createDeckState by mutableStateOf<CreateDeckState>(CreateDeckState.Idle)
        private set

    // State untuk Edit Deck
    var editDeckState by mutableStateOf<EditDeckState>(EditDeckState.Idle)
        private set

    // State untuk Move Deck
    var moveDeckState by mutableStateOf<MoveDeckState>(MoveDeckState.Idle)
        private set

    // State untuk Operasi Card (Create/Update/Delete)
    var cardOperationState by mutableStateOf<CardOperationState>(CardOperationState.Idle)
        private set

    // Loading khusus saat fetch cards
    var areCardsLoading by mutableStateOf(false)
        private set

    // Pesan error umum (Snackbar)
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // ==========================================================
    // PERBAIKAN DI SINI (Dibuat Public State)
    // ==========================================================
    // Agar EditDeckScreen bisa mengambil ID ini jika data dari Deck null
    var currentFolderId by mutableStateOf<String?>(null)
        private set

    // ==========================================================
    // HELPER FUNCTIONS
    // ==========================================================

    /**
     * Mengambil data deck dari list lokal berdasarkan ID.
     * Digunakan di EditDeckScreen untuk pre-fill data.
     */
    fun getDeckById(id: String): Deck? {
        return _decks.find { it.id == id }
    }

    /**
     * Reset state Edit setelah sukses atau navigasi
     */
    fun onEditSuccessHandled() {
        editDeckState = EditDeckState.Idle
    }

    /**
     * Reset state Error saat user mulai mengetik ulang
     */
    fun resetEditStateOnly() {
        if (editDeckState is EditDeckState.Error) {
            editDeckState = EditDeckState.Idle
        }
    }

    /**
     * Reset semua state utama (Deck & Folder Ops)
     */
    fun resetState() {
        createDeckState = CreateDeckState.Idle
        editDeckState = EditDeckState.Idle
        moveDeckState = MoveDeckState.Idle
        cardOperationState = CardOperationState.Idle
        errorMessage = null
    }

    /**
     * Reset khusus state Card (Diminta untuk tidak dihapus)
     */
    fun resetCardState() {
        cardOperationState = CardOperationState.Idle
        errorMessage = null
    }

    fun clearError() {
        errorMessage = null
    }

    // ==========================================================
    // DECK & FOLDER ACTIONS
    // ==========================================================

    fun loadDecks(folderId: String?) {
        currentFolderId = folderId
        viewModelScope.launch {
            repository.getDecks(folderId)
                .onSuccess { deckList ->
                    _decks.clear()
                    _decks.addAll(deckList)
                }
                .onFailure {
                    errorMessage = "Gagal memuat decks: ${it.message}"
                }
        }
    }

    private fun reloadCurrentFolder() {
        loadDecks(currentFolderId)
    }

    fun loadFolders() {
        viewModelScope.launch {
            repository.getAllFolders()
                .onSuccess { folderList ->
                    _folders.clear()
                    _folders.addAll(folderList)
                }
        }
    }

    fun createDeck(name: String, description: String, folderId: String?) {
        viewModelScope.launch {
            createDeckState = CreateDeckState.Loading
            repository.createDeck(name, description, folderId)
                .onSuccess { newDeck ->
                    _decks.add(0, newDeck) // Tambah ke atas list
                    createDeckState = CreateDeckState.Success
                }
                .onFailure { e ->
                    createDeckState = CreateDeckState.Error(e.message ?: "Failed to create deck")
                }
        }
    }

    /**
     * Update Deck.
     * Pastikan folderId diisi dengan ID folder lama agar deck tidak keluar dari folder.
     */
    fun updateDeck(deckId: String, name: String, description: String, folderId: String?) {
        viewModelScope.launch {
            editDeckState = EditDeckState.Loading

            // Memanggil Repository (Pastikan di RepositoryImpl sudah menggunakan api.updateDeck)
            repository.updateDeck(deckId, name, description, folderId)
                .onSuccess { updatedDeck ->
                    // 1. Update list lokal agar UI DeckScreen berubah (nama/updatedAt) langsung
                    val index = _decks.indexOfFirst { it.id == deckId }
                    if (index != -1) {
                        _decks[index] = updatedDeck
                    } else {
                        // Jika deck tidak ketemu (kasus jarang), reload semua
                        reloadCurrentFolder()
                    }
                    editDeckState = EditDeckState.Success
                }
                .onFailure { e ->
                    editDeckState = EditDeckState.Error(e.message ?: "Failed to update deck")
                }
        }
    }

    /**
     * Logic Move Deck yang sudah diperbaiki.
     * Memilih antara moveDeck (folder biasa) atau moveDeckToHome (root).
     */
    fun moveDeck(deckId: String, targetFolderId: String?) {
        viewModelScope.launch {
            moveDeckState = MoveDeckState.Loading

            // PILIH STRATEGI BERDASARKAN TARGET
            val result = if (targetFolderId == null) {
                // Jika target null, berarti pindah ke Home. Panggil fungsi khusus repository.
                repository.moveDeckToHome(deckId)
            } else {
                // Jika target ada ID-nya, pindah ke folder tersebut.
                repository.moveDeck(deckId, targetFolderId)
            }

            result.onSuccess {
                moveDeckState = MoveDeckState.Success
                // Logika UI:
                // Jika deck dipindahkan ke tempat yang BUKAN folder saat ini,
                // maka hapus deck itu dari tampilan list sekarang.
                if (targetFolderId != currentFolderId) {
                    _decks.removeAll { it.id == deckId }
                } else {
                    // Jika dipindah ke folder yang sama (edge case), refresh saja
                    reloadCurrentFolder()
                }
            }.onFailure { e ->
                moveDeckState = MoveDeckState.Error(e.message ?: "Failed to move deck")
            }
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
                .onSuccess {
                    _decks.removeAll { it.id == deckId }
                }
                .onFailure {
                    errorMessage = "Gagal menghapus deck: ${it.message}"
                }
        }
    }

    // ==========================================================
    // CARD ACTIONS
    // ==========================================================

    fun loadCards(deckId: String) {
        viewModelScope.launch {
            areCardsLoading = true
            errorMessage = null
            repository.getCardsByDeckId(deckId)
                .onSuccess { cardList ->
                    _cards.clear()
                    _cards.addAll(cardList)
                }
                .onFailure { e ->
                    errorMessage = "Gagal memuat kartu: ${e.message}"
                }
            areCardsLoading = false
        }
    }

    fun createCard(deckId: String, front: String, back: String) {
        viewModelScope.launch {
            cardOperationState = CardOperationState.Loading
            repository.createCard(deckId, front, back)
                .onSuccess { newCard ->
                    _cards.add(newCard) // Langsung update UI CardsScreen
                    cardOperationState = CardOperationState.Success
                }
                .onFailure { e ->
                    cardOperationState = CardOperationState.Error(e.message ?: "Failed to create card")
                    errorMessage = e.message
                }
        }
    }

    fun updateCard(cardId: String, front: String, back: String) {
        viewModelScope.launch {
            cardOperationState = CardOperationState.Loading
            repository.updateCard(cardId, front, back)
                .onSuccess { updatedCard ->
                    val index = _cards.indexOfFirst { it.id == cardId }
                    if (index != -1) {
                        _cards[index] = updatedCard
                    }
                    cardOperationState = CardOperationState.Success
                }
                .onFailure { e ->
                    cardOperationState = CardOperationState.Error(e.message ?: "Failed to update card")
                    errorMessage = e.message
                }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
                .onSuccess {
                    _cards.removeAll { it.id == cardId }
                }
                .onFailure { e ->
                    errorMessage = "Gagal menghapus kartu: ${e.message}"
                }
        }
    }
}