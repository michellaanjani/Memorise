package com.mobile.memorise.data.remote.api

import com.mobile.memorise.data.remote.dto.HomeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Body
import com.mobile.memorise.domain.model.CreateFolderRequest
import com.mobile.memorise.domain.model.UpdateFolderRequest
import com.mobile.memorise.domain.model.FolderResponse

interface HomeApi {

    // Header Authorization biasanya otomatis diurus oleh Interceptor (OkHttp)
    // Jika belum ada interceptor, tambahkan @Header("Authorization") token: String
    @GET("home")
    suspend fun getHomeData(
//        @Header("Authorization") token: String // Tambahkan parameter ini
    ): Response<HomeResponse>
}

interface FolderApi {
    @POST("folders")
    suspend fun createFolder(
//        @Header("Authorization") token: String,
        @Body request: CreateFolderRequest): Response<FolderResponse>
    @PATCH("folders/{id}")
    suspend fun updateFolder(
        @Path("id") id: String,
        @Body request: UpdateFolderRequest
    ): Response<FolderResponse>
    @DELETE("folders/{id}")
    suspend fun deleteFolder(@Path("id") id: String): Response<Unit>

    @GET("folders")
    suspend fun getFolders(): Response<com.mobile.memorise.domain.model.FolderListResponse>
}