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
        val originalRequest = chain.request()

        // 1. Ambil token (blocking)
        val token = runBlocking {
            tokenStore.accessToken.first()
        }

        // 2. Jika token ada, suntikkan ke header
        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()

        // 3. Lanjutkan request
        // PENTING: Jangan cek kode 401 di sini!
        // Biarkan Authenticator yang menangani 401.
        return chain.proceed(request)
    }
}