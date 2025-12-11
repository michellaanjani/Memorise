package com.mobile.memorise.ui.screen.signup.linksentverif

import android.util.Log // Import Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.mobile.memorise.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class VerificationStatusViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _isVerified = MutableStateFlow(false)
    val isVerified = _isVerified.asStateFlow()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            var attempt = 1
            while (true) {
                try {
                    Log.d("VERIF_DEBUG", "Percobaan polling ke-$attempt...")

                    // Panggil API
                    val response = repository.getEmailVerificationStatus()

                    // Log hasil response
                    Log.d("VERIF_DEBUG", "Response API: ${response.isEmailVerified}")

                    _isVerified.value = response.isEmailVerified

                    if (response.isEmailVerified) {
                        Log.d("VERIF_DEBUG", "User Terverifikasi! Keluar dari loop.")
                        break // stop polling
                    }

                } catch (e: Exception) {
                    // PENTING: Print errornya agar tahu kenapa gagal
                    Log.e("VERIF_DEBUG", "Error saat polling: ${e.message}")
                    e.printStackTrace()
                }

                attempt++
                delay(15000) // polling 3 detik
            }
        }
    }
}