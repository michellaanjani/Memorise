package com.mobile.memorise.data.repository

import com.mobile.memorise.data.local.token.TokenStore
import com.mobile.memorise.data.local.user.UserStore
import com.mobile.memorise.data.remote.AuthApi
// --- PERHATIKAN IMPORT DI BAWAH INI ---
import com.mobile.memorise.data.remote.dto.auth.LoginRequestDto
import com.mobile.memorise.data.remote.dto.auth.RegisterRequestDto
// ---------------------------------------
import com.mobile.memorise.domain.repository.AuthRepository
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val userStore: UserStore
) : AuthRepository {

    override val isUserLoggedIn: Flow<Boolean> = tokenStore.accessToken
        .map { !it.isNullOrBlank() }

    override suspend fun signIn(email: String, pass: String): Resource<Unit> {
        return try {
            // Karena AuthApi sudah diubah menerima LoginRequestDto, ini tidak akan merah lagi
            val response = api.login(LoginRequestDto(email, pass))
            val body = response.body()

            if (response.isSuccessful && body != null && body.success && body.data != null) {
                val authData = body.data
                val tokens = authData.tokens

                if (tokens != null) {
                    tokenStore.saveTokens(tokens.accessToken, tokens.refreshToken)
                    userStore.saveUser(authData.user)
                    Resource.Success(Unit)
                } else {
                    Resource.Error("Token tidak ditemukan")
                }
            } else {
                val msg = body?.message ?: "Login failed"
                Resource.Error(msg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Connection error")
        }
    }

    override suspend fun signUp(firstName: String, lastName: String, email: String, pass: String): Resource<Unit> {
        return try {
            // Karena AuthApi sudah diubah menerima RegisterRequestDto, ini aman
            val request = RegisterRequestDto(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = pass
            )
            val response = api.register(request)
            val body = response.body()

            if (response.isSuccessful && body != null && body.success) {
                val authData = body.data
                val tokens = authData?.tokens

                if (tokens != null && authData != null) {
                    tokenStore.saveTokens(tokens.accessToken, tokens.refreshToken)
                    userStore.saveUser(authData.user)
                }
                Resource.Success(Unit)
            } else {
                val msg = body?.message ?: "Registration failed"
                Resource.Error(msg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Connection error")
        }
    }

    override suspend fun logout() {
        try {
            tokenStore.clearToken()
            userStore.clearUser()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}