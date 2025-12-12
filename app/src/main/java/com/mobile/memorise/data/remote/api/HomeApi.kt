//package com.mobile.memorise.data.remote.api
//
//import com.mobile.memorise.data.remote.dto.HomeResponse
//import retrofit2.Response
//import retrofit2.http.GET
//import retrofit2.http.POST
//import retrofit2.http.Path
//import retrofit2.http.PATCH
//import retrofit2.http.DELETE
//import retrofit2.http.Body
//// Pastikan semua import model ini ada
//import com.mobile.memorise.domain.model.CreateFolderRequest
//import com.mobile.memorise.domain.model.UpdateFolderRequest
//import com.mobile.memorise.domain.model.FolderResponse
//import com.mobile.memorise.domain.model.CreateDeckRequest
//import com.mobile.memorise.domain.model.UpdateDeckRequest // <-- Import Model Baru
//import com.mobile.memorise.domain.model.DeckResponse
//
//interface HomeApi {
//    @GET("home")
//    suspend fun getHomeData(): Response<HomeResponse>
//}
//
//interface FolderApi {
//    @POST("folders")
//    suspend fun createFolder(
//        @Body request: CreateFolderRequest
//    ): Response<FolderResponse>
//
//    @PATCH("folders/{id}")
//    suspend fun updateFolder(
//        @Path("id") id: String,
//        @Body request: UpdateFolderRequest
//    ): Response<FolderResponse>
//
//    @DELETE("folders/{id}")
//    suspend fun deleteFolder(
//        @Path("id") id: String
//    ): Response<Unit>
//}
//
//interface DeckApi {
//    @POST("decks")
//    suspend fun createDeck(
//        @Body request: CreateDeckRequest
//    ): Response<DeckResponse>
//
//    // ⬇️ FITUR BARU: UPDATE DECK
//    @PATCH("decks/{id}")
//    suspend fun updateDeck(
//        @Path("id") id: String,
//        @Body request: UpdateDeckRequest
//    ): Response<DeckResponse>
//
//    // ⬇️ FITUR TAMBAHAN: DELETE DECK (Biar lengkap sekalian)
//    @DELETE("decks/{id}")
//    suspend fun deleteDeck(
//        @Path("id") id: String
//    ): Response<Unit>
//}