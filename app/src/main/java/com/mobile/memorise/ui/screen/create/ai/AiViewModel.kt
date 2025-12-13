package com.mobile.memorise.ui.screen.create.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.model.Card
import com.mobile.memorise.domain.model.Deck
import com.mobile.memorise.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// Model UI Khusus untuk menyimpan state Draft
data class DraftSession(
    val deck: Deck,
    val cards: List<Card>
)

@HiltViewModel
class AiViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {

    // --- STATES ---
    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _draftSession = MutableStateFlow<DraftSession?>(null)
    val draftSession = _draftSession.asStateFlow()

    // --- 1. GENERATE AI (Upload -> Generate) ---
    fun processFileAndGenerate(file: File, format: String, amount: Int) {
        viewModelScope.launch {
            _uiState.value = AiUiState.Loading("Mengunggah dokumen...")

            // Step 1: Upload
            val uploadResult = repository.uploadFile(file)

            uploadResult.onFailure {
                handleError(it)
                return@launch
            }

            val fileId = uploadResult.getOrNull()?.id ?: return@launch

            // Step 2: Generate
            _uiState.value = AiUiState.Loading("AI sedang menganalisa...")

            val generateResult = repository.generateFlashcards(
                prompt = format,
                fileId = fileId
                // amount parameter bisa ditambahkan ke prompt string jika repository belum support
            )

            generateResult.onSuccess { aiContent ->
                _uiState.value = AiUiState.SuccessGenerated(aiContent.deckId)
            }.onFailure {
                handleError(it)
            }
        }
    }

    // --- 2. LOAD DRAFT ---
    fun loadDraft(deckId: String) {
        viewModelScope.launch {
            // Jika data sudah ada dan sama, tidak perlu load ulang (opsional optimization)
            if (_draftSession.value?.deck?.id == deckId) return@launch

            _uiState.value = AiUiState.Loading("Menyiapkan draft...")

            val deckResult = repository.getAiDraft(deckId)
            val cardsResult = repository.getCardsByDeckId(deckId)

            if (deckResult.isSuccess && cardsResult.isSuccess) {
                val deck = deckResult.getOrNull()!!
                val cards = cardsResult.getOrNull()!!

                _draftSession.value = DraftSession(deck, cards)
                _uiState.value = AiUiState.Idle
            } else {
                val exception = deckResult.exceptionOrNull() ?: cardsResult.exceptionOrNull()
                handleError(exception ?: Exception("Gagal memuat draft"))
            }
        }
    }

    // --- 3. EDIT CARD ---
    fun updateCard(cardId: String, front: String, back: String) {
        val currentSession = _draftSession.value ?: return
        val deckId = currentSession.deck.id

        viewModelScope.launch {
            val result = repository.updateDraftCard(deckId, cardId, front, back)

            result.onSuccess { updatedCard ->
                // Update local list agar UI langsung berubah tanpa refresh
                val newCards = currentSession.cards.map {
                    if (it.id == cardId) updatedCard else it
                }
                _draftSession.value = currentSession.copy(cards = newCards)
            }.onFailure {
                handleError(it)
            }
        }
    }

    // --- 4. DELETE CARD ---
    fun deleteCard(cardId: String) {
        val currentSession = _draftSession.value ?: return
        val deckId = currentSession.deck.id

        viewModelScope.launch {
            val result = repository.deleteDraftCard(deckId, cardId)

            result.onSuccess {
                // Hapus dari list local
                val newCards = currentSession.cards.filter { it.id != cardId }
                _draftSession.value = currentSession.copy(cards = newCards)
            }.onFailure {
                handleError(it)
            }
        }
    }

    // --- 5. UPDATE LOCAL DECK NAME (Dari TextField UI) ---
    fun updateLocalDeckName(newName: String) {
        val currentSession = _draftSession.value ?: return
        val updatedDeck = currentSession.deck.copy(name = newName)
        _draftSession.value = currentSession.copy(deck = updatedDeck)
    }

    // --- 6. SAVE DECK ---
    fun saveDeck(onComplete: () -> Unit) {
        val currentSession = _draftSession.value ?: return
        val deckId = currentSession.deck.id

        // TODO: Jika API saveAiDraft mendukung rename deck, kirim nama baru di sini.
        // Jika tidak, pastikan ada endpoint updateDeck(deckId, name) yang dipanggil sebelumnya.

        viewModelScope.launch {
            _uiState.value = AiUiState.Loading("Menyimpan ke koleksi...")

            // destinationFolderId null = Root/Home
            repository.saveAiDraft(deckId, null)
                .onSuccess {
                    _uiState.value = AiUiState.SuccessSaved
                    onComplete()
                }
                .onFailure {
                    handleError(it)
                }
        }
    }

    fun resetState() {
        _uiState.value = AiUiState.Idle
    }

    // --- HELPER: ERROR MESSAGE HANDLER ---
    private fun handleError(throwable: Throwable) {
        val friendlyMsg = getFriendlyErrorMessage(throwable)
        _uiState.value = AiUiState.Error(friendlyMsg)
    }

    private fun getFriendlyErrorMessage(throwable: Throwable): String {
        val msg = throwable.message?.lowercase() ?: "unknown error"
        return when {
            msg.contains("timeout") -> "Koneksi lambat. Coba kurangi jumlah halaman atau coba lagi nanti."
            msg.contains("host") || msg.contains("connect") || msg.contains("network") -> "Gagal terhubung ke server. Periksa koneksi internet Anda."
            msg.contains("413") -> "Ukuran file terlalu besar untuk diproses."
            msg.contains("429") -> "Terlalu banyak permintaan. Mohon tunggu sebentar."
            msg.contains("json") || msg.contains("serialize") -> "Terjadi kesalahan pemrosesan data. Coba lagi."
            else -> "Terjadi kesalahan: ${throwable.message}" // Fallback message
        }
    }
}

// --- UI STATES ---
sealed class AiUiState {
    object Idle : AiUiState()
    data class Loading(val message: String) : AiUiState()
    data class Error(val message: String) : AiUiState()
    data class SuccessGenerated(val deckId: String) : AiUiState()
    object SuccessSaved : AiUiState()
}