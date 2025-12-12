package com.mobile.memorise.data.remote

import com.mobile.memorise.data.remote.dto.common.ApiResponseDto
import com.mobile.memorise.data.remote.dto.auth.AuthDataDto
import com.mobile.memorise.data.remote.dto.auth.LoginRequestDto    // <--- Pastikan Import DTO
import com.mobile.memorise.data.remote.dto.auth.RegisterRequestDto // <--- Pastikan Import DTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto // <--- Ubah dari LoginRequest ke LoginRequestDto
    ): Response<ApiResponseDto<AuthDataDto>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto // <--- Ubah dari RegisterRequest ke RegisterRequestDto
    ): Response<ApiResponseDto<AuthDataDto>>
}