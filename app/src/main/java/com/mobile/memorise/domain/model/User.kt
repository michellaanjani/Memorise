package com.mobile.memorise.domain.model

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