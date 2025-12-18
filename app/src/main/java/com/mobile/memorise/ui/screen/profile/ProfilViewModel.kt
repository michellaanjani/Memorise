package com.mobile.memorise.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.data.local.avatar.AvatarStore
import com.mobile.memorise.domain.repository.UserRepository
import com.mobile.memorise.ui.model.UserProfile
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val avatarStore: AvatarStore
) : ViewModel() {

    private val _userProfile = MutableStateFlow(
        UserProfile(
            firstName = "",
            lastName = "",
            email = "",
            avatarUri = null
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _updateState = MutableStateFlow<Resource<Unit>?>(null)
    val updateState: StateFlow<Resource<Unit>?> = _updateState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            // Load avatar from local storage
            val avatarUri = avatarStore.avatarUri.first()

            // Load user data from backend
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        _userProfile.value = UserProfile(
                            firstName = user.firstName.ifBlank { "" },
                            lastName = user.lastName.ifBlank { "" },
                            email = user.email.ifBlank { "" },
                            avatarUri = avatarUri
                        )
                    }
                }
                is Resource.Error -> {
                    // Handle error if needed
                }
                is Resource.Loading -> {
                    // Handle loading if needed
                }
                is Resource.Idle -> {
                    // Handle idle state if needed
                }
            }
        }
    }

    /** Update Avatar (hanya di local storage) */
    fun updateAvatar(uri: String?) {
        viewModelScope.launch {
            avatarStore.saveAvatarUri(uri)
            _userProfile.value = _userProfile.value.copy(avatarUri = uri)
        }
    }

    /** Update profile (firstName, lastName) ke backend */
    fun updateProfile(
        firstName: String,
        lastName: String,
        avatarUri: String?
    ) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading()

            // Save avatar to local storage
            if (avatarUri != null) {
                avatarStore.saveAvatarUri(avatarUri)
            }

            // Update profile to backend
            when (val result = userRepository.updateProfile(firstName, lastName)) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        _userProfile.value = UserProfile(
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            avatarUri = avatarUri
                        )
                    }
                    _updateState.value = Resource.Success(Unit)
                }
                is Resource.Error -> {
                    _updateState.value = Resource.Error(result.message ?: "Failed to update profile")
                }
                is Resource.Loading -> {
                    _updateState.value = Resource.Loading()
                }
                is Resource.Idle -> {
                    // No action needed for idle state
                }
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = null
    }
    private val _changePasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val changePasswordState: StateFlow<Resource<Unit>?> = _changePasswordState.asStateFlow()

    /** Change password */
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = Resource.Loading()
            when (val result = userRepository.changePassword(currentPassword, newPassword)) {
                is Resource.Success -> {
                    _changePasswordState.value = Resource.Success(Unit)
                }
                is Resource.Error -> {
                    _changePasswordState.value = Resource.Error(result.message ?: "Failed to change password")
                }
                is Resource.Loading -> {
                    _changePasswordState.value = Resource.Loading()
                }
                is Resource.Idle -> {
                    // No action needed for idle state
                }
            }
        }
    }
}