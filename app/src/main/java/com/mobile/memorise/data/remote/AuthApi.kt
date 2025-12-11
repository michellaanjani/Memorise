package com.mobile.memorise.data.remote

import com.google.gson.annotations.SerializedName
import com.mobile.memorise.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.*

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResponse<AuthDataDto>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<BaseResponse<AuthDataDto>>

    @GET("users/profile")
    suspend fun getProfile(): Response<BaseResponse<UserWrapperDto>>

    @POST("auth/logout")
    suspend fun logoutServer(): Response<BaseResponse<Any>>
}