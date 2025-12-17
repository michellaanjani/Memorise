package com.mobile.memorise.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Pastikan import ini sesuai dengan lokasi file model Anda (tanpa .quiz jika satu file)
import com.mobile.memorise.domain.model.QuizAnswerInput
import com.mobile.memorise.domain.model.QuizResult
import com.mobile.memorise.domain.model.QuizSession
import com.mobile.memorise.domain.repository.ContentRepository
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {

    // ==========================================
    // 1. STATE MANAGEMENT
    // ==========================================

    // State untuk Start Quiz (Mengembalikan QuizSession berisi soal-soal)
    private val _startState = MutableStateFlow<Resource<QuizSession>>(Resource.Idle())
    val startState: StateFlow<Resource<QuizSession>> = _startState

    // State untuk Submit Quiz (Mengembalikan QuizResult berisi skor)
    private val _submitState = MutableStateFlow<Resource<QuizResult>>(Resource.Idle())
    val submitState: StateFlow<Resource<QuizResult>> = _submitState

    // Fungsi reset state (berguna saat keluar dari layar kuis)
    fun resetSubmitState() { _submitState.value = Resource.Idle() }
    fun resetStartState() { _startState.value = Resource.Idle() }

    // ==========================================
    // 2. LOGIC FUNCTIONS
    // ==========================================

    /**
     * Memulai sesi kuis baru berdasarkan Deck ID.
     */
    fun startQuiz(deckId: String) {
        viewModelScope.launch {
            _startState.value = Resource.Loading()

            repository.startQuiz(deckId)
                .onSuccess { data ->
                    // Data: QuizSession (deckId, totalQuestions, list questions)
                    _startState.value = Resource.Success(data)
                }
                .onFailure { exception ->
                    _startState.value = Resource.Error(exception.message ?: "Gagal memulai kuis")
                }
        }
    }

    /**
     * Mengirim jawaban kuis ke server.
     * Parameter disesuaikan dengan fungsi di ContentRepositoryImpl.
     */
    fun submitQuiz(
        deckId: String,
        totalQuestions: Int,
        correctAnswers: Int,
        answers: List<QuizAnswerInput> // List input jawaban user
    ) {
        viewModelScope.launch {
            _submitState.value = Resource.Loading()

            // Repository akan mengurus konversi List<QuizAnswerInput> ke List<QuizAnswerDto>
            repository.submitQuiz(deckId, totalQuestions, correctAnswers, answers)
                .onSuccess { result ->
                    // Data: QuizResult (score, playedAt, dll)
                    _submitState.value = Resource.Success(result)
                }
                .onFailure { exception ->
                    _submitState.value = Resource.Error(exception.message ?: "Gagal mengirim jawaban")
                }
        }
    }
}