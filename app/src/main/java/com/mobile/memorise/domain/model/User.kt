package com.mobile.memorise.domain.model

// Model User (Data Profil)
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isEmailVerified: Boolean,
    val bio: String? = null,
    val avatar: String? = null
) {
    val fullName: String get() = "$firstName $lastName"
}

// Model AuthToken (Data Sesi Login)
// Tambahkan class ini di bawah User
data class AuthToken(
    val accessToken: String,
    val refreshToken: String
)