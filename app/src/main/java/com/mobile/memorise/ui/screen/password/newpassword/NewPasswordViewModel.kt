package com.mobile.memorise.ui.screen.password.newpassword

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
class NewPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<NewPasswordState>(NewPasswordState.Idle)
    val state: StateFlow<NewPasswordState> = _state

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _state.value = NewPasswordState.Loading

            // Panggil Repository
            val result = userRepository.resetPassword(token, newPassword)

            when (result) {
                is Resource.Success -> {
                    _state.value = NewPasswordState.Success
                }
                is Resource.Error -> {
                    _state.value = NewPasswordState.Error(result.message ?: "Failed to reset password")
                }
                else -> {}
            }
        }
    }
}

sealed class NewPasswordState {
    object Idle : NewPasswordState()
    object Loading : NewPasswordState()
    object Success : NewPasswordState()
    data class Error(val message: String) : NewPasswordState()
}