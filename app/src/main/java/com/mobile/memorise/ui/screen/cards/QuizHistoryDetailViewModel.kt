package com.mobile.memorise.ui.screen.cards

import androidx.lifecycle.SavedStateHandle
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

sealed class QuizDetailUiState {
    object Loading : QuizDetailUiState()
    data class Success(val result: QuizResult) : QuizDetailUiState()
    data class Error(val message: String) : QuizDetailUiState()
}

@HiltViewModel
class QuizHistoryDetailViewModel @Inject constructor(
    private val repository: ContentRepository,
    savedStateHandle: SavedStateHandle // Fitur Hilt untuk ambil argument navigasi
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizDetailUiState>(QuizDetailUiState.Loading)
    val uiState: StateFlow<QuizDetailUiState> = _uiState.asStateFlow()

    // Ambil ID dari route navigasi "quiz_detail/{quizId}"
    private val quizId: String? = savedStateHandle["quizId"]

    init {
        // Otomatis load saat ViewModel dibuat
        quizId?.let { loadQuizDetail(it) }
    }

    fun loadQuizDetail(id: String) {
        viewModelScope.launch {
            _uiState.value = QuizDetailUiState.Loading
            repository.getQuizDetail(id)
                .onSuccess { result ->
                    _uiState.value = QuizDetailUiState.Success(result)
                }
                .onFailure { error ->
                    _uiState.value = QuizDetailUiState.Error(error.message ?: "Failed to load detail")
                }
        }
    }
}