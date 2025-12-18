package com.mobile.memorise.data.remote.dto.auth

import com.google.gson.annotations.SerializedName // PENTING: Tambahkan dependency GSON jika merah
import com.mobile.memorise.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==========================================
// 1. REQUEST DTOs
// ==========================================

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(val token: String, val newPassword: String)

@Serializable
data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val profile: ProfileDataDto? = null
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ProfileDataDto(
    val bio: String? = null,
    val avatar: String? = null
)

// ==========================================
// 2. RESPONSE DTOs
// ==========================================

@Serializable
data class UserProfileResponseDto(
    val user: UserDto
)

@Serializable
data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class AuthDataDto(
    val user: UserDto,
    val tokens: TokensDto? = null
)

@Serializable
data class TokensDto(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class EmailVerificationStatusDataDto(
    val isEmailVerified: Boolean
)

// ==========================================
// 3. USER MODEL (FIX CRASH DI SINI)
// ==========================================

@Serializable
data class UserDto(
    // === SAFETY FIX ===
    // 1. Tambahkan @SerializedName("_id") agar terbaca oleh Retrofit (GSON)
    // 2. Tambahkan @SerialName("_id") agar terbaca oleh DataStore (Kotlinx)
    // 3. Ubah tipe menjadi String? (Nullable) agar tidak crash jika API error/null

    @SerialName("_id")
    @SerializedName("_id")
    val id: String? = null,

    val email: String? = null,

    val firstName: String? = null,
    val lastName: String? = null,

    val isEmailVerified: Boolean = false,

    val profile: UserProfileDetailDto? = null
) {
    // Mapper ke Domain Model
    // Kita handle null di sini agar Domain layer tetap bersih (tidak null)
    fun toDomain(): User {
        return User(
            id = id ?: "", // Jika null, ganti string kosong
            email = email ?: "",
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            isEmailVerified = isEmailVerified,
            bio = profile?.bio ?: "",
            avatar = profile?.avatar ?: ""
        )
    }
}

@Serializable
data class UserProfileDetailDto(
    val bio: String? = null,
    val avatar: String? = null,
    val language: String? = null
)