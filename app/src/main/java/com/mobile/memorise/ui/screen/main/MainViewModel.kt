package com.mobile.memorise.ui.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // null = loading (Splash screen tampil)
    // true = Home Screen
    // false = Login Screen
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        observeLoginStatus()
    }

    private fun observeLoginStatus() {
        viewModelScope.launch {
            // Kita meng-collect (memantau) Flow dari repository.
            // 1. Saat aplikasi dibuka, dia cek DataStore -> Update _isLoggedIn
            // 2. Saat User Login -> DataStore terisi -> Flow memancarkan True -> Otomatis pindah ke Home
            // 3. Saat User Logout -> DataStore dihapus -> Flow memancarkan False -> Otomatis pindah ke Login
            repository.isUserLoggedIn.collect { hasToken ->
                _isLoggedIn.value = hasToken
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Panggil fungsi logout di repo (menghapus token).
            // Karena kita memantau flow di atas, UI akan otomatis bereaksi pindah ke layar Login.
            repository.logout()
        }
    }
}