package com.mobile.memorise.data.repository

import com.mobile.memorise.data.remote.api.UserApi
import com.mobile.memorise.data.remote.dto.UpdateProfileRequest
import com.mobile.memorise.data.remote.dto.ChangePasswordRequest
import com.mobile.memorise.data.remote.dto.ProfileDataDto
import com.mobile.memorise.domain.model.EmailVerificationStatus
import com.mobile.memorise.domain.model.User
import com.mobile.memorise.domain.repository.UserRepository
import com.mobile.memorise.util.Resource
import com.mobile.memorise.data.local.token.TokenStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val tokenStore: TokenStore
) : UserRepository {

    override suspend fun getEmailVerificationStatus(): EmailVerificationStatus {
        val token = tokenStore.accessToken.first()

        if(token.isNullOrBlank()){
            throw Exception("Token tidak ditemukan, user mungkin belum login.")
        }

        val response = api.getEmailVerificationStatus("Bearer $token")

        // Cek dulu response.data agar tidak NullPointerException
        val isVerified = response.data?.isEmailVerified
            ?: throw Exception("Data verifikasi email tidak ditemukan dalam respons.")

        return EmailVerificationStatus(
            isEmailVerified = isVerified
        )
    }

    override suspend fun getUserProfile(): Resource<User> {
        return try {
            val response = api.getUserProfile()
            val body = response.body()

            if (response.isSuccessful && body != null && body.success && body.data != null) {
                val user = body.data.user.toDomain()
                Resource.Success(user)
            } else {
                val message = body?.message ?: body?.error ?: "Failed to get user profile"
                Resource.Error(message)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Connection error")
        }
    }

    override suspend fun updateProfile(firstName: String, lastName: String): Resource<User> {
        return try {
            val request = UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                profile = null // Bio tidak diupdate untuk sekarang
            )
            val response = api.updateProfile(request)
            val body = response.body()

            if (response.isSuccessful && body != null && body.success && body.data != null) {
                val user = body.data.user.toDomain()
                Resource.Success(user)
            } else {
                val message = body?.message ?: body?.error ?: "Failed to update profile"
                Resource.Error(message)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Connection error")
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Resource<Unit> {
        return try {
            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
            val response = api.changePassword(request)
            val body = response.body()

            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(Unit)
            } else {
                val message = body?.message ?: body?.error ?: "Failed to change password"
                Resource.Error(message)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Connection error")
        }
    }
}