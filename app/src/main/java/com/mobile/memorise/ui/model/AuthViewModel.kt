package com.mobile.memorise.ui.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.repository.AuthRepository
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // Login Form State
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    // One-time events (Error snackbars, Navigation)
    private val _authEvent = Channel<AuthEvent>()
    val authEvent = _authEvent.receiveAsFlow()

    fun onSignInClick() {
        viewModelScope.launch {
            isLoading = true
            val result = repository.signIn(email, password)
            isLoading = false

            when(result) {
                is Resource.Success -> {
                    // Navigasi ditangani otomatis oleh AppEntry karena isUserLoggedIn berubah
                    _authEvent.send(AuthEvent.Success)
                }
                is Resource.Error -> {
                    _authEvent.send(AuthEvent.Error(result.message ?: "Error"))
                }
                else -> Unit
            }
        }
    }
}

sealed class AuthEvent {
    object Success : AuthEvent()
    data class Error(val message: String) : AuthEvent()
}