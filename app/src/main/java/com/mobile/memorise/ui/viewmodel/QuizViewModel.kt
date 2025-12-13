package com.mobile.memorise.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.model.quiz.QuizStartData
import com.mobile.memorise.domain.model.quiz.QuizSubmitData
import com.mobile.memorise.domain.model.quiz.QuizSubmitRequest
import com.mobile.memorise.domain.repository.ContentRepository // Gunakan Repository
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: ContentRepository // Ganti ApiService dengan Repository
) : ViewModel() {

    private val _startState = MutableStateFlow<Resource<QuizStartData>>(Resource.Idle())
    val startState: StateFlow<Resource<QuizStartData>> = _startState

    private val _submitState = MutableStateFlow<Resource<QuizSubmitData>>(Resource.Idle())
    val submitState: StateFlow<Resource<QuizSubmitData>> = _submitState

    fun resetSubmitState() { _submitState.value = Resource.Idle() }
    fun resetStartState() { _startState.value = Resource.Idle() }

    fun startQuiz(deckId: String) {
        viewModelScope.launch {
            _startState.value = Resource.Loading()

            // Panggil Repository (Logic aman ada di sana)
            repository.startQuiz(deckId)
                .onSuccess { data ->
                    _startState.value = Resource.Success(data)
                }
                .onFailure { exception ->
                    _startState.value = Resource.Error(exception.message ?: "Gagal memulai kuis")
                }
        }
    }

    fun submitQuiz(request: QuizSubmitRequest) {
        viewModelScope.launch {
            _submitState.value = Resource.Loading()

            // Repository akan mengurus konversi ke DTO dan handling API
            repository.submitQuiz(request)
                .onSuccess { data ->
                    _submitState.value = Resource.Success(data)
                }
                .onFailure { exception ->
                    _submitState.value = Resource.Error(exception.message ?: "Gagal mengirim jawaban")
                }
        }
    }
}