package com.mobile.memorise.ui.screen.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.repository.AuthRepository // Sesuaikan package repository Anda
import com.mobile.memorise.util.Resource // Sesuaikan package Resource Anda
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Event untuk mengirim sinyal Sukses atau Error ke UI
sealed class SignUpEvent {
    object Success : SignUpEvent()
    data class Error(val message: String) : SignUpEvent()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // State Form (disimpan di ViewModel agar tidak hilang saat rotasi layar)
    var email by mutableStateOf("")
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var password by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)

    // State UI (Loading)
    var isLoading by mutableStateOf(false)

    // Channel untuk One-time events (Navigasi / Snackbar)
    private val _signUpEvent = Channel<SignUpEvent>()
    val signUpEvent = _signUpEvent.receiveAsFlow()

    fun onSignUpClick() {
        // Validasi dasar sebelum kirim ke API
        if (password.length < 8) return
        if (!termsAccepted) return

        viewModelScope.launch {
            isLoading = true

            // Panggil fungsi signUp di Repository
            // Pastikan repository Anda punya fungsi signUp(first, last, email, pass)
            val result = repository.signUp(firstName = firstName, lastName = lastName, email = email.trim(), pass = password.trim())

            isLoading = false

            when(result) {
                is Resource.Success -> {
                    _signUpEvent.send(SignUpEvent.Success)
                }
                is Resource.Error -> {
                    _signUpEvent.send(SignUpEvent.Error(result.message ?: "Unknown Error"))
                }
                else -> Unit
            }
        }
    }
}