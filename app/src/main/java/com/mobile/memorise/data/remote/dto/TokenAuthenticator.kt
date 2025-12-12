package com.mobile.memorise.data.remote.dto

// TokenAuthenticator.kt

import com.mobile.memorise.data.local.token.TokenStore
import com.mobile.memorise.data.remote.api.UserApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val apiProvider: Provider<UserApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            return runBlocking {
                // 1. Ambil Refresh Token Lama
                val refreshToken = tokenStore.getRefreshTokenSync()

                if (refreshToken.isNullOrBlank()) {
                    tokenStore.clearToken()
                    return@runBlocking null
                }

                try {
                    // 2. Request Token Baru
                    val api = apiProvider.get()
                    val newTokensResponse = api.refreshToken(
                        mapOf("refreshToken" to refreshToken)
                    )

                    // 3. Ambil Body Response
                    val authData = newTokensResponse.body()?.data

                    // PERBAIKAN DI SINI:
                    // Kita harus mengambil object 'tokens' dari dalam AuthDataDto
                    val newTokens = authData?.tokens

                    if (newTokensResponse.isSuccessful && newTokens != null) {

                        // 4. Simpan Token Baru (Ambil dari object tokens)
                        tokenStore.saveTokens(newTokens.accessToken, newTokens.refreshToken)

                        // 5. Retry Request dengan Access Token Baru
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                    } else {
                        // Gagal refresh atau object tokens null -> Logout
                        tokenStore.clearToken()
                        return@runBlocking null
                    }
                } catch (e: Exception) {
                    tokenStore.clearToken()
                    return@runBlocking null
                }
            }
        }
    }
}