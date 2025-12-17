package com.mobile.memorise.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.mobile.memorise.domain.model.User

// --- 1. Generic Wrapper untuk Response API ---
data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?, // Nullable karena kalau error data kosong
    val error: String? = null,
    val details: Any? = null
)

// --- 2. Request Bodies ---
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

// 🔥 INI YANG DITAMBAHKAN
data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,       // Ini adalah kode OTP yang diinput user sebelumnya
    val newPassword: String  // Password baru yang diinput user
)
data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val profile: ProfileDataDto? = null // Nested object sesuai API, optional
)

data class ProfileDataDto(
    val bio: String? = null
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// --- 3. Response Data Structures ---
// Struktur data di dalam "data": { "user": ..., "tokens": ... }
data class AuthDataDto(
    val user: UserDto,
    val tokens: TokensDto
)

data class TokensDto(
    val accessToken: String,
    val refreshToken: String
)

data class UserWrapperDto(
    val user: UserDto
)

data class UserProfileResponseDto(
    val user: UserDto
)

data class EmailVerificationStatusDataDto(
    val isEmailVerified: Boolean
)

// Mapping User dari JSON yang rumit ke object DTO
data class UserDto(
    @SerializedName(value = "id", alternate = ["_id"]) val id: String, // profile pakai "id", login pakai "_id"
    val email: String,
    val firstName: String,
    val lastName: String,
    val isEmailVerified: Boolean,
    val profile: UserProfileDetailDto?
) {
    // Mapper ke Domain Model
    fun toDomain(): User {
        return User(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            isEmailVerified = isEmailVerified,
            bio = profile?.bio,
            avatar = profile?.avatar
        )
    }
}

data class UserProfileDetailDto(
    val bio: String?,
    val avatar: String?,
    val language: String?
)