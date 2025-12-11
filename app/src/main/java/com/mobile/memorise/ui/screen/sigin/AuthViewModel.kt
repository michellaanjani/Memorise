package com.mobile.memorise.ui.screen.sigin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.repository.AuthRepository // Ganti dengan package repository Anda
import com.mobile.memorise.util.Resource // Ganti dengan package Resource Anda
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthEvent {
    object Success : AuthEvent()
    data class Error(val message: String) : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // State untuk Input Form
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // State untuk UI
    var isLoading by mutableStateOf(false)
    var isError by mutableStateOf(false) // Untuk mengubah warna textfield jadi merah

    // Channel untuk mengirim event satu kali (Navigasi / Toast)
    private val _authEvent = Channel<AuthEvent>()
    val authEvent = _authEvent.receiveAsFlow()

    fun onSignInClick() {
        // Validasi sederhana sebelum request API
        if(email.isBlank() || password.isBlank()) {
            isError = true
            return
        }

        viewModelScope.launch {
            isLoading = true
            isError = false // Reset error sebelum request

            val result = repository.signIn(email = email.trim(), pass = password.trim())

            isLoading = false

            when(result) {
                is Resource.Success -> {
                    _authEvent.send(AuthEvent.Success)
                }
                is Resource.Error -> {
                    isError = true
                    _authEvent.send(AuthEvent.Error(result.message ?: "Login Failed"))
                }
                else -> Unit
            }
        }
    }
}