package com.mobile.memorise.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.ui.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow(
        UserProfile(
            firstName = "Reynard",
            lastName = "Wijaya",
            email = "contoh@email.com",
            avatarUri = null
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile

    /** Update nama depan */
    fun updateFirstName(name: String) {
        _userProfile.value = _userProfile.value.copy(firstName = name)
    }

    /** Update nama belakang */
    fun updateLastName(name: String) {
        _userProfile.value = _userProfile.value.copy(lastName = name)
    }

    /** Update email */
    fun updateEmail(email: String) {
        _userProfile.value = _userProfile.value.copy(email = email)
    }

    /** Update Avatar */
    fun updateAvatar(uri: String?) {
        _userProfile.value = _userProfile.value.copy(avatarUri = uri)
    }

    /** Update seluruh profil sekaligus */
    fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        avatarUri: String?
    ) {
        viewModelScope.launch {
            _userProfile.value = UserProfile(
                firstName = firstName,
                lastName = lastName,
                email = email,
                avatarUri = avatarUri
            )
        }
    }
}
