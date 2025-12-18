package com.mobile.memorise.ui.screen.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.model.QuizResult
import com.mobile.memorise.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuizHistoryUiState {
    object Loading : QuizHistoryUiState()
    data class Success(
        val recentAttempt: QuizResult?, // Item pertama
        val pastAttempts: List<QuizResult> // Item sisanya
    ) : QuizHistoryUiState()
    data class Error(val message: String) : QuizHistoryUiState()
}

@HiltViewModel
class QuizHistoryViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizHistoryUiState>(QuizHistoryUiState.Loading)
    val uiState: StateFlow<QuizHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = QuizHistoryUiState.Loading

            repository.getQuizHistory()
                .onSuccess { list ->
                    // Urutkan berdasarkan playedAt terbaru (jika server belum sorting)
                    // Asumsi string ISO bisa disort secara lexicographical, atau parse dulu
                    val sortedList = list.sortedByDescending { it.playedAt }

                    if (sortedList.isEmpty()) {
                        _uiState.value = QuizHistoryUiState.Success(null, emptyList())
                    } else {
                        val mostRecent = sortedList.first()
                        val past = sortedList.drop(1)
                        _uiState.value = QuizHistoryUiState.Success(mostRecent, past)
                    }
                }
                .onFailure {
                    _uiState.value = QuizHistoryUiState.Error(it.message ?: "Failed to load history")
                }
        }
    }
}