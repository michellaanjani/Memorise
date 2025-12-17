package com.mobile.memorise.ui.screen.password.sent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.repository.UserRepository
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetOtpViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResetOtpUiState>(ResetOtpUiState.Idle)
    val uiState: StateFlow<ResetOtpUiState> = _uiState

    // State untuk Timer Resend
    private val _timerState = MutableStateFlow(0)
    val timerState: StateFlow<Int> = _timerState

    // Fungsi Resend OTP (Menggunakan endpoint forgotPassword untuk kirim ulang)
    fun resendOtp(email: String) {
        viewModelScope.launch {
            _uiState.value = ResetOtpUiState.Loading

            val result = userRepository.forgotPassword(email)

            when (result) {
                is Resource.Success -> {
                    _uiState.value = ResetOtpUiState.ResendSuccess("OTP Resent successfully")
                    startTimer()
                }
                is Resource.Error -> {
                    _uiState.value = ResetOtpUiState.Error(result.message ?: "Failed to resend OTP")
                }
                else -> {}
            }
        }
    }

    private fun startTimer() {
        _timerState.value = 120 // 2 Menit
    }

    fun decrementTimer() {
        if (_timerState.value > 0) {
            _timerState.value -= 1
        }
    }

    fun resetState() {
        _uiState.value = ResetOtpUiState.Idle
    }
}

sealed class ResetOtpUiState {
    object Idle : ResetOtpUiState()
    object Loading : ResetOtpUiState()
    data class ResendSuccess(val message: String) : ResetOtpUiState()
    data class Error(val message: String) : ResetOtpUiState()
}