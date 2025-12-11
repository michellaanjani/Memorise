package com.mobile.memorise.data.remote.api

import com.mobile.memorise.data.remote.dto.BaseResponse
import com.mobile.memorise.data.remote.dto.EmailVerificationStatusDataDto
import retrofit2.http.GET
import retrofit2.http.Header


interface UserApi {
    @GET("users/email-verification-status")
    suspend fun getEmailVerificationStatus(
        @Header("Authorization") token: String
    ): BaseResponse<EmailVerificationStatusDataDto>
}
