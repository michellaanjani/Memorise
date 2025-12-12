package com.mobile.memorise.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.data.remote.api.DeckApi
import com.mobile.memorise.data.remote.api.CardApi
import com.mobile.memorise.data.remote.api.FolderApi
import com.mobile.memorise.domain.model.card.CardData
import com.mobile.memorise.domain.model.card.CardRequest
import com.mobile.memorise.domain.model.deck.CreateDeckRequest
import com.mobile.memorise.domain.model.deck.DeckData
import com.mobile.memorise.domain.model.deck.MoveDeckRequest
import com.mobile.memorise.domain.model.deck.UpdateDeckRequest
import com.mobile.memorise.domain.model.FolderData
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckRemoteViewModel @Inject constructor(
    private val deckApi: DeckApi,
    private val cardApi: CardApi,
    private val folderApi: FolderApi
) : ViewModel() {

    // Deck list state (used for folder view and unassigned view)
    private val _deckListState = MutableStateFlow<Resource<List<DeckData>>>(Resource.Loading())
    val deckListState: StateFlow<Resource<List<DeckData>>> = _deckListState

    // Single deck state (create/update/move/delete)
    private val _deckMutationState = MutableStateFlow<Resource<DeckData?>>(Resource.Idle())
    val deckMutationState: StateFlow<Resource<DeckData?>> = _deckMutationState

    // Cards state for a deck
    private val _cardsState = MutableStateFlow<Resource<List<CardData>>>(Resource.Loading())
    val cardsState: StateFlow<Resource<List<CardData>>> = _cardsState

    private val _cardMutationState = MutableStateFlow<Resource<CardData?>>(Resource.Idle())
    val cardMutationState: StateFlow<Resource<CardData?>> = _cardMutationState

    private val _foldersState = MutableStateFlow<Resource<List<FolderData>>>(Resource.Loading())
    val foldersState: StateFlow<Resource<List<FolderData>>> = _foldersState

    fun resetMutationState() {
        _deckMutationState.value = Resource.Idle()
        _cardMutationState.value = Resource.Idle()
    }

    fun loadFolders() {
        viewModelScope.launch {
            _foldersState.value = Resource.Loading()
            try {
                val response = folderApi.getFolders()
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _foldersState.value = Resource.Success(body.data ?: emptyList())
                } else {
                    _foldersState.value = Resource.Error(body?.message ?: "Failed to load folders")
                }
            } catch (e: Exception) {
                _foldersState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    // ---------- Deck operations ----------
    fun loadDecks(folderId: String? = null, unassigned: Boolean? = null) {
        viewModelScope.launch {
            _deckListState.value = Resource.Loading()
            try {
                val response = deckApi.getDecks(folderId = folderId, unassigned = unassigned)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _deckListState.value = Resource.Success(body.data)
                } else {
                    _deckListState.value = Resource.Error(body?.message ?: "Failed to load decks")
                }
            } catch (e: Exception) {
                _deckListState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun createDeck(name: String, description: String?, folderId: String?) {
        viewModelScope.launch {
            _deckMutationState.value = Resource.Loading()
            try {
                val response = deckApi.createDeck(
                    CreateDeckRequest(name = name, description = description, folderId = folderId)
                )
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _deckMutationState.value = Resource.Success(body.data)
                    // refresh list for relevant scope
                    loadDecks(folderId = folderId, unassigned = if (folderId == null) true else null)
                } else {
                    _deckMutationState.value = Resource.Error(body?.message ?: "Failed to create deck")
                }
            } catch (e: Exception) {
                _deckMutationState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun updateDeck(id: String, name: String?, description: String?) {
        viewModelScope.launch {
            _deckMutationState.value = Resource.Loading()
            try {
                val response = deckApi.updateDeck(id, UpdateDeckRequest(name = name, description = description))
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _deckMutationState.value = Resource.Success(body.data)
                } else {
                    _deckMutationState.value = Resource.Error(body?.message ?: "Failed to update deck")
                }
            } catch (e: Exception) {
                _deckMutationState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun moveDeck(id: String, folderId: String?) {
        viewModelScope.launch {
            _deckMutationState.value = Resource.Loading()
            try {
                val response = deckApi.moveDeck(id, MoveDeckRequest(folderId = folderId))
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _deckMutationState.value = Resource.Success(body.data)
                } else {
                    _deckMutationState.value = Resource.Error(body?.message ?: "Failed to move deck")
                }
            } catch (e: Exception) {
                _deckMutationState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun deleteDeck(id: String, folderId: String? = null) {
        viewModelScope.launch {
            _deckMutationState.value = Resource.Loading()
            try {
                val response = deckApi.deleteDeck(id)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _deckMutationState.value = Resource.Success(null)
                    loadDecks(folderId = folderId, unassigned = if (folderId == null) true else null)
                } else {
                    _deckMutationState.value = Resource.Error(body?.message ?: "Failed to delete deck")
                }
            } catch (e: Exception) {
                _deckMutationState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    // ---------- Card operations ----------
    fun loadCards(deckId: String) {
        viewModelScope.launch {
            _cardsState.value = Resource.Loading()
            try {
                val response = cardApi.getCardsByDeck(deckId)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _cardsState.value = Resource.Success(body.data)
                } else {
                    _cardsState.value = Resource.Error(body?.message ?: "Failed to load cards")
                }
            } catch (e: Exception) {
                _cardsState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun createCard(deckId: String, front: String, back: String, notes: String? = null, tags: List<String>? = null) {
        viewModelScope.launch {
            _cardMutationState.value = Resource.Loading()
            try {
                val response = cardApi.createCard(CardRequest(deckId = deckId, front = front, back = back, notes = notes, tags = tags))
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _cardMutationState.value = Resource.Success(body.data)
                    loadCards(deckId)
                } else {
                    _cardMutationState.value = Resource.Error(body?.message ?: "Failed to create card")
                }
            } catch (e: Exception) {
                _cardMutationState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun updateCard(id: String, deckId: String, front: String, back: String, notes: String? = null, tags: List<String>? = null) {
        viewModelScope.launch {
            _cardMutationState.value = Resource.Loading()
            try {
                val response = cardApi.updateCard(id, CardRequest(deckId = deckId, front = front, back = back, notes = notes, tags = tags))
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _cardMutationState.value = Resource.Success(body.data)
                    loadCards(deckId)
                } else {
                    _cardMutationState.value = Resource.Error(body?.message ?: "Failed to update card")
                }
            } catch (e: Exception) {
                _cardMutationState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun deleteCard(id: String, deckId: String) {
        viewModelScope.launch {
            _cardMutationState.value = Resource.Loading()
            try {
                val response = cardApi.deleteCard(id)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    _cardMutationState.value = Resource.Success(null)
                    loadCards(deckId)
                } else {
                    _cardMutationState.value = Resource.Error(body?.message ?: "Failed to delete card")
                }
            } catch (e: Exception) {
                _cardMutationState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }
}

