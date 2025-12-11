package com.mobile.memorise.data.remote

import com.mobile.memorise.data.local.token.TokenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. Ambil token (blocking karena interceptor synchronous)
        val token = runBlocking {
            tokenStore.accessToken.first()
        }

        // 2. Buat request baru dengan header
        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)

        // 3. Cek apakah token expired (401)
        if (response.code == 401) {
            // Token tidak valid -> Hapus token -> UI akan otomatis kembali ke Login
            runBlocking {
                tokenStore.clearToken()
            }
        }

        return response
    }
}