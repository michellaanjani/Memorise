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
    object Idle : CreateDeckState()
    object Loading : CreateDeckState()
    object Success : CreateDeckState()
    data class Error(val message: String) : CreateDeckState()
}

sealed class EditDeckState {
    object Idle : EditDeckState()
    object Loading : EditDeckState()
    object Success : EditDeckState()
    data class Error(val message: String) : EditDeckState()
}

sealed class MoveDeckState {
    object Idle : MoveDeckState()
    object Loading : MoveDeckState()
    object Success : MoveDeckState()
    data class Error(val message: String) : MoveDeckState()
}

// --- Card States (Baru Ditambahkan) ---
sealed class CardOperationState {
    object Idle : CardOperationState()
    object Loading : CardOperationState()
    object Success : CardOperationState()
    data class Error(val message: String) : CardOperationState()
}

@HiltViewModel
class DeckViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {

    // --- DATA HOLDERS ---
    private val _decks = mutableStateListOf<Deck>()
    val decks: List<Deck> get() = _decks

    private val _folders = mutableStateListOf<Folder>()
    val folders: List<Folder> get() = _folders

    // List kartu untuk ditampilkan di layar Detail/Edit
    private val _cards = mutableStateListOf<Card>()
    val cards: List<Card> get() = _cards

    // --- UI STATES (Deck) ---
    var createDeckState by mutableStateOf<CreateDeckState>(CreateDeckState.Idle)
        private set

    var editDeckState by mutableStateOf<EditDeckState>(EditDeckState.Idle)
        private set

    var moveDeckState by mutableStateOf<MoveDeckState>(MoveDeckState.Idle)
        private set

    // --- UI STATES (Card - Baru Ditambahkan) ---
    var cardOperationState by mutableStateOf<CardOperationState>(CardOperationState.Idle)
        private set

    var areCardsLoading by mutableStateOf(false)
        private set

    private var currentFolderId: String? = null

    // ==========================================================
    // STATE MANAGEMENT
    // ==========================================================

    fun resetState() {
        createDeckState = CreateDeckState.Idle
        editDeckState = EditDeckState.Idle
        moveDeckState = MoveDeckState.Idle
        cardOperationState = CardOperationState.Idle
    }

    fun onEditSuccessHandled() {
        editDeckState = EditDeckState.Idle
    }

    fun resetEditStateOnly() {
        editDeckState = EditDeckState.Idle
    }

    fun resetCardState() {
        cardOperationState = CardOperationState.Idle
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
                    _decks.add(0, newDeck)
                    createDeckState = CreateDeckState.Success
                }
                .onFailure { e ->
                    createDeckState = CreateDeckState.Error(e.message ?: "Failed to create deck")
                }
        }
    }

    fun updateDeck(deckId: String, name: String, description: String, folderId: String?) {
        viewModelScope.launch {
            editDeckState = EditDeckState.Loading
            repository.updateDeck(deckId, name, description, folderId)
                .onSuccess { updatedDeck ->
                    val index = _decks.indexOfFirst { it.id == deckId }
                    if (index != -1) {
                        _decks[index] = updatedDeck
                    } else {
                        reloadCurrentFolder()
                    }
                    editDeckState = EditDeckState.Success
                }
                .onFailure { e ->
                    editDeckState = EditDeckState.Error(e.message ?: "Failed to update deck")
                }
        }
    }

    fun moveDeck(deckId: String, targetFolderId: String?) {
        viewModelScope.launch {
            moveDeckState = MoveDeckState.Loading
            repository.moveDeck(deckId, targetFolderId)
                .onSuccess {
                    moveDeckState = MoveDeckState.Success
                    if (targetFolderId != currentFolderId) {
                        _decks.removeAll { it.id == deckId }
                    } else {
                        reloadCurrentFolder()
                    }
                }
                .onFailure { e ->
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
        }
    }

    fun getDeckById(deckId: String): Deck? {
        return _decks.find { it.id == deckId }
    }

    // ==========================================================
    // CARD ACTIONS (PENGGANTI DeckRemoteViewModel)
    // ==========================================================

    fun loadCards(deckId: String) {
        viewModelScope.launch {
            areCardsLoading = true
            repository.getCardsByDeckId(deckId)
                .onSuccess { cardList ->
                    _cards.clear()
                    _cards.addAll(cardList)
                    areCardsLoading = false
                }
                .onFailure {
                    areCardsLoading = false
                    // Handle error if needed, e.g. show snackbar
                }
        }
    }

    fun createCard(deckId: String, front: String, back: String) {
        viewModelScope.launch {
            cardOperationState = CardOperationState.Loading
            repository.createCard(deckId, front, back)
                .onSuccess { newCard ->
                    _cards.add(newCard) // Langsung update list UI
                    cardOperationState = CardOperationState.Success
                }
                .onFailure { e ->
                    cardOperationState = CardOperationState.Error(e.message ?: "Failed to create card")
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
                }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            // Opsional: set loading state jika ingin menunjukkan progress delete
            repository.deleteCard(cardId)
                .onSuccess {
                    _cards.removeAll { it.id == cardId }
                }
                .onFailure { e ->
                    // Handle error, maybe show toast
                }
        }
    }
}