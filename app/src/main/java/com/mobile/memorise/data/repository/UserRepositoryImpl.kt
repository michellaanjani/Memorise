package com.mobile.memorise.data.repository

import com.mobile.memorise.data.local.token.TokenStore
//import com.mobile.memorise.data.remote.api.AuthApi // Tambahkan ini
import com.mobile.memorise.data.remote.api.UserApi
import com.mobile.memorise.data.remote.dto.auth.* // Import semua DTO
import com.mobile.memorise.domain.model.EmailVerificationStatus
import com.mobile.memorise.domain.model.User
import com.mobile.memorise.domain.repository.UserRepository
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi, // Ganti nama 'api' jadi 'userApi' biar jelas
    //private val authApi: AuthApi, // Tambahkan ini untuk fitur public (Lupa Password)
    private val tokenStore: TokenStore
) : UserRepository {

    override suspend fun getEmailVerificationStatus(): EmailVerificationStatus {
        val token = tokenStore.accessToken.first()

        if(token.isNullOrBlank()){
            throw Exception("Token tidak ditemukan, user mungkin belum login.")
        }

        // Panggil userApi
        val response = userApi.getEmailVerificationStatus("Bearer $token")

        val isVerified = response.data?.isEmailVerified
            ?: throw Exception("Data verifikasi email tidak ditemukan dalam respons.")

        return EmailVerificationStatus(
            isEmailVerified = isVerified
        )
    }

    override suspend fun getUserProfile(): Resource<User> {
        return try {
            // Panggil userApi
            val response = userApi.getUserProfile()
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
            // PERBAIKAN: Gunakan 'UpdateProfileRequestDto' (sesuai file DTO)
            val request = UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                profile = null
            )

            // Panggil userApi
            val response = userApi.updateProfile(request)
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
            // PERBAIKAN: Gunakan 'ChangePasswordRequestDto'
            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )

            // Panggil userApi (karena butuh token login)
            val response = userApi.changePassword(request)
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

    override suspend fun forgotPassword(email: String): Resource<Unit> {
        return try {
            // PERBAIKAN: Gunakan 'ForgotPasswordRequestDto'
            val request = ForgotPasswordRequest(email = email)

            // PENTING: Gunakan 'authApi' (karena ini endpoint public)
            val response = userApi.forgotPassword(request)
            val body = response.body()

            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(Unit)
            } else {
                val message = body?.message ?: body?.error ?: "Failed to send reset link"
                Resource.Error(message)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Connection error")
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): Resource<Unit> {
        return try {
            // PERBAIKAN: Gunakan 'ResetPasswordRequestDto'
            val request = ResetPasswordRequest(
                token = token,
                newPassword = newPassword
            )

            // PENTING: Gunakan 'authApi' (ini juga endpoint public)
            val response = userApi.resetPassword(request)
            val body = response.body()

            if (response.isSuccessful && body != null && body.success) {
                Resource.Success(Unit)
            } else {
                val message = body?.message ?: body?.error ?: "Failed to reset password"
                Resource.Error(message)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Connection error")
        }
    }
}