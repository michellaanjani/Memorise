package com.mobile.memorise.data.repository

import com.mobile.memorise.data.local.token.TokenStore
import com.mobile.memorise.data.remote.AuthApi
import com.mobile.memorise.data.remote.dto.LoginRequest
import com.mobile.memorise.data.remote.dto.RegisterRequest
import com.mobile.memorise.domain.repository.AuthRepository
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override val isUserLoggedIn: Flow<Boolean> = tokenStore.accessToken
        .map { !it.isNullOrBlank() }

    override suspend fun signIn(email: String, pass: String): Resource<Unit> {
        return try {
            val response = api.login(LoginRequest(email, pass))
            val body = response.body()

            if (response.isSuccessful && body != null && body.success && body.data != null) {
                // Simpan Access Token (Refresh token diabaikan sesuai request awal)
                tokenStore.saveToken(body.data.tokens.accessToken)
                Resource.Success(Unit)
            } else {
                // Ambil pesan error dari API jika ada, atau fallback
                val msg = body?.message ?: body?.error ?: "Login failed"
                Resource.Error(msg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Connection error")
        }
    }

    // Update parameter sesuai kebutuhan API (Name)
    override suspend fun signUp(firstName: String, lastName: String, email: String, pass: String): Resource<Unit> {
        return try {
            val request = RegisterRequest(firstName = firstName, lastName = lastName, email = email, password = pass)
            val response = api.register(request)
            val body = response.body()

            if (response.isSuccessful && body != null && body.success) {
                // API register ini mengembalikan token juga, jadi bisa auto-login
                // Jika data token ada, simpan.
                body.data?.tokens?.let {
                    tokenStore.saveToken(it.accessToken)
                }
                Resource.Success(Unit)
            } else {
                val msg = body?.message ?: body?.error ?: "Registration failed"
                Resource.Error(msg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Connection error")
        }
    }

    override suspend fun logout() {
        try {
            // Optional: Beritahu server kita logout
            api.logoutServer()
        } catch (e: Exception) {
            // Ignore network error saat logout
        } finally {
            // Hapus token lokal (Penting)
            tokenStore.clearToken()
        }
    }
}