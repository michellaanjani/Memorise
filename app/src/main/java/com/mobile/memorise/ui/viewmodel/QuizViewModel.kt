package com.mobile.memorise.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.data.remote.api.ApiService // Ganti QuizApi dengan ApiService
import com.mobile.memorise.data.mapper.toDomain
import com.mobile.memorise.data.mapper.toDto
import com.mobile.memorise.domain.model.quiz.QuizAnswerDetail
import com.mobile.memorise.domain.model.quiz.QuizStartData
import com.mobile.memorise.domain.model.quiz.QuizSubmitRequest
import com.mobile.memorise.domain.model.quiz.QuizSubmitData
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val api: ApiService // INJECT APISERVICE, BUKAN QUIZAPI
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
            try {
                // Panggil API baru
                val response = api.startQuiz(deckId)
                val body = response.body()

                // Cek ApiResponseDto
                if (response.isSuccessful && body?.success == true && body.data != null) {
                    // MAP DARI DTO (QuizSessionDto) KE DOMAIN (QuizStartData)
                    _startState.value = Resource.Success(body.data.toDomain())
                } else {
                    _startState.value = Resource.Error(body?.message ?: "Failed to start quiz")
                }
            } catch (e: Exception) {
                _startState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }

    fun submitQuiz(request: QuizSubmitRequest) {
        viewModelScope.launch {
            _submitState.value = Resource.Loading()
            try {
                // MAP REQUEST DOMAIN KE DTO SEBELUM KIRIM KE API
                val requestDto = request.toDto()

                val response = api.submitQuiz(requestDto)
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    // MAP DARI DTO (QuizResultDto) KE DOMAIN (QuizSubmitData)
                    _submitState.value = Resource.Success(body.data.toDomain())
                } else {
                    _submitState.value = Resource.Error(body?.message ?: "Failed to submit quiz")
                }
            } catch (e: Exception) {
                _submitState.value = Resource.Error(e.localizedMessage ?: "Network error")
            }
        }
    }
}