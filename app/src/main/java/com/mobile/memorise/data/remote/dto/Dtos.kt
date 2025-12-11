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
    val firstName: String, // Ubah jadi firstName
    val lastName: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val profile: ProfileDataDto // Nested object sesuai API
)

data class ProfileDataDto(
    val bio: String
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

data class EmailVerificationStatusDataDto(
    val isEmailVerified: Boolean
)

// Mapping User dari JSON yang rumit ke object DTO
data class UserDto(
    @SerializedName("_id") val id: String, // API login pakai "_id"
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