package com.mobile.memorise.ui.screen.password.forgot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.repository.UserRepository
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(ForgotPasswordState())
        private set

    fun onEvent(event: ForgotPasswordEvent) {
        when(event) {
            is ForgotPasswordEvent.EmailChanged -> {
                state = state.copy(email = event.email, error = null)
            }
            is ForgotPasswordEvent.Submit -> {
                sendResetLink()
            }
            is ForgotPasswordEvent.ResetState -> {
                // Reset sukses state agar tidak navigasi berulang
                state = state.copy(isSuccess = false, error = null)
            }
        }
    }

    private fun sendResetLink() {
        val email = state.email
        if (email.isBlank()) {
            state = state.copy(error = "Email cannot be empty")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            when (val result = userRepository.forgotPassword(email)) {
                is Resource.Success -> {
                    state = state.copy(isLoading = false, isSuccess = true)
                }
                is Resource.Error -> {
                    state = state.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {
                    // handled by isLoading flag above
                }
                // 🔥 TAMBAHKAN INI (Solusi Error)
                else -> {
                    // Menangani Resource.Idle atau state lain yang tidak didefinisikan
                    state = state.copy(isLoading = false)
                }
            }
        }
    }
}

// State Data Class
data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

// Event Sealed Class
sealed class ForgotPasswordEvent {
    data class EmailChanged(val email: String): ForgotPasswordEvent()
    object Submit: ForgotPasswordEvent()
    object ResetState: ForgotPasswordEvent()
}