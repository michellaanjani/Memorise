package com.mobile.memorise.data.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val email: String, val password: String)

@Serializable
data class RegisterRequestDto(val email: String, val password: String, val firstName: String, val lastName: String)

@Serializable
data class AuthDataDto(
    val user: UserDto,
    val tokens: TokensDto? = null // Bisa null saat update profile
)

@Serializable
data class TokensDto(val accessToken: String, val refreshToken: String)

@Serializable
data class UserDto(
    @SerialName("_id") val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isEmailVerified: Boolean,
    // Profile nested object
    val profile: UserProfileDto? = null
)

@Serializable
data class UserProfileDto(val avatar: String?, val bio: String?)