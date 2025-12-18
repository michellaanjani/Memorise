package com.mobile.memorise.data.remote.api

import com.mobile.memorise.data.remote.dto.auth.*
//import com.mobile.memorise.data.remote.dto.EmailVerificationStatusDataDto
import com.mobile.memorise.data.remote.dto.auth.AuthDataDto
import com.mobile.memorise.data.remote.dto.common.ApiResponseDto
//import com.mobile.memorise.data.remote.dto.UserProfileResponseDto
//import com.mobile.memorise.data.remote.dto.UpdateProfileRequest
//import com.mobile.memorise.data.remote.dto.ChangePasswordRequest
//import com.mobile.memorise.data.remote.dto.ForgotPasswordRequest
//import com.mobile.memorise.data.remote.dto.ResetPasswordRequest
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.Response



interface UserApi {
    @GET("users/email-verification-status")
    suspend fun getEmailVerificationStatus(
        @Header("Authorization") token: String
    ): BaseResponse<EmailVerificationStatusDataDto>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponseDto<Unit>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponseDto<Unit>>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: Map<String, String>): Response<ApiResponseDto<Unit>>

    @POST("auth/resend-verification")
    suspend fun resendVerification(): Response<ApiResponseDto<Unit>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: Map<String, String>): Response<ApiResponseDto<AuthDataDto>>

    // =================================================================
    // 2. USER MODULE
    // =================================================================
    @GET("users/profile")
    suspend fun getUserProfile(): Response<BaseResponse<UserProfileResponseDto>>
    // Butuh UserDto di response

    @PUT("users/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<BaseResponse<UserProfileResponseDto>>
    @PUT("users/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<BaseResponse<Unit>>


    @GET("users/email-verification-status")
    suspend fun getEmailVerificationStatus(): Response<ApiResponseDto<Map<String, Boolean>>>

    @DELETE("users/account")
    suspend fun deleteAccount(@Body request: Map<String, String>): Response<ApiResponseDto<Unit>>
}
