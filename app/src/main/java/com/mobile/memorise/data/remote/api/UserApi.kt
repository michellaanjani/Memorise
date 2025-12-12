package com.mobile.memorise.data.remote.api

import com.mobile.memorise.data.remote.dto.BaseResponse
import com.mobile.memorise.data.remote.dto.EmailVerificationStatusDataDto
import com.mobile.memorise.data.remote.dto.UserProfileResponseDto
import com.mobile.memorise.data.remote.dto.UpdateProfileRequest
import com.mobile.memorise.data.remote.dto.ChangePasswordRequest
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Header
import retrofit2.http.Body


interface UserApi {
    @GET("users/email-verification-status")
    suspend fun getEmailVerificationStatus(
        @Header("Authorization") token: String
    ): BaseResponse<EmailVerificationStatusDataDto>

    @GET("users/profile")
    suspend fun getUserProfile(): Response<BaseResponse<UserProfileResponseDto>>

    @PUT("users/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<BaseResponse<UserProfileResponseDto>>

    @PUT("users/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<BaseResponse<Unit>>
}
