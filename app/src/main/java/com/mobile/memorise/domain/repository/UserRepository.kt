package com.mobile.memorise.domain.repository

import com.mobile.memorise.domain.model.EmailVerificationStatus
import com.mobile.memorise.domain.model.User
import com.mobile.memorise.util.Resource

interface UserRepository {
    suspend fun getEmailVerificationStatus(): EmailVerificationStatus
    suspend fun getUserProfile(): Resource<User>
    suspend fun updateProfile(firstName: String, lastName: String): Resource<User>
    suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit>
}
