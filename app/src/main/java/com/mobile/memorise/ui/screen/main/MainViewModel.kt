package com.mobile.memorise.ui.screen.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.repository.AuthRepository
import com.mobile.memorise.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository, // Untuk Cek Token Lokal
    private val userRepository: UserRepository  // Untuk Cek Status Verifikasi ke API
) : ViewModel() {

    // null = loading (Splash screen tampil)
    // true = Home Screen (Token Ada & Email Verified)
    // false = Login Screen (Token Tidak Ada atau Email Belum Verified)
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        observeLoginStatus()
    }

    private fun observeLoginStatus() {
        viewModelScope.launch {
            // 1. Pantau Token dari DataStore
            authRepository.isUserLoggedIn.collect { hasToken ->
                if (hasToken) {
                    // 2. Jika Token Ada, validasi ke Server apakah sudah Verified
                    checkVerificationStatus()
                } else {
                    // Tidak ada token -> Ke Login Screen
                    _isLoggedIn.value = false
                }
            }
        }
    }

    private suspend fun checkVerificationStatus() {
        try {
            // Panggil API User Profile / Status
            val response = userRepository.getEmailVerificationStatus()

            Log.d("MAIN_VM", "User Verified Status: ${response.isEmailVerified}")

            // Logika Penentu Halaman:
            // Jika Verified = true -> Masuk Home (true)
            // Jika Verified = false -> Balik Login/Landing (false)
            _isLoggedIn.value = response.isEmailVerified

        } catch (e: Exception) {
            Log.e("MAIN_VM", "Error checking verification: ${e.message}")
            // Jika gagal koneksi/error, anggap belum login agar aman
            _isLoggedIn.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // UI akan otomatis pindah ke Login karena kita observe `isUserLoggedIn` di atas
        }
    }
}