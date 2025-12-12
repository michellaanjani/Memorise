package com.mobile.memorise.data.remote

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
    // Kita pakai Provider agar tidak error Circular Dependency
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
                    // 2. Request Token Baru ke API
                    val api = apiProvider.get()
                    val newTokensResponse = api.refreshToken(
                        mapOf("refreshToken" to refreshToken)
                    )

                    // 3. Ambil data token dari response
                    val authData = newTokensResponse.body()?.data
                    // Ingat struktur DTO kamu: AuthDataDto -> tokens -> accessToken
                    val newTokens = authData?.tokens

                    if (newTokensResponse.isSuccessful && newTokens != null) {

                        // 4. Simpan Token Baru (Access & Refresh)
                        tokenStore.saveTokens(newTokens.accessToken, newTokens.refreshToken)

                        // 5. Ulangi request yang gagal tadi dengan Token Baru
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                    } else {
                        // Refresh gagal (misal expired), logout
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