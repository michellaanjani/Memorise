package com.mobile.memorise.data.repository

import com.mobile.memorise.data.remote.api.UserApi
import com.mobile.memorise.domain.model.EmailVerificationStatus
import com.mobile.memorise.domain.repository.UserRepository
//import com.mobile.memorise.data.remote.dto.EmailVerificationStatusDataDto

import com.mobile.memorise.data.local.token.TokenStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val tokenStore: TokenStore
) : UserRepository {

    override suspend fun getEmailVerificationStatus(): EmailVerificationStatus {
        val token = tokenStore.accessToken.first()

        if(token.isNullOrBlank()){
            throw Exception("Token tidak ditemukan, user mungkin belum login.")
        }

        val response = api.getEmailVerificationStatus("Bearer $token")

        // Cek dulu response.data agar tidak NullPointerException
        val isVerified = response.data?.isEmailVerified
            ?: throw Exception("Data verifikasi email tidak ditemukan dalam respons.")

        return EmailVerificationStatus(
            isEmailVerified = isVerified
        )
    }
}