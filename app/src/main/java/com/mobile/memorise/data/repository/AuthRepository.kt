package com.mobile.memorise.data.repository

import com.mobile.memorise.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isUserLoggedIn: Flow<Boolean>
    suspend fun signIn(email: String, pass: String): Resource<Unit>
    // Tambah firstName & lastName
    suspend fun signUp(firstName: String, lastName: String, email: String, pass: String): Resource<Unit>
    suspend fun logout()
}