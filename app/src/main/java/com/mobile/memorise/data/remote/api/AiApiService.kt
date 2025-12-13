//package com.mobile.memorise.data.remote.api
//
//import com.mobile.memorise.data.remote.dto.content.AiCard
//import com.mobile.memorise.data.remote.dto.content.AiDeckInfo
//import com.mobile.memorise.data.remote.dto.content.AiDraftDetailData
//import com.mobile.memorise.data.remote.dto.content.AiGenerateRequest
//import com.mobile.memorise.data.remote.dto.content.AiGenerateResultData
//import com.mobile.memorise.data.remote.dto.content.ApiResponse
//import com.mobile.memorise.data.remote.dto.content.SaveDeckRequest
//import com.mobile.memorise.data.remote.dto.content.UpdateCardRequest
//import com.mobile.memorise.data.remote.dto.content.UploadResponseData
//import okhttp3.MultipartBody
//import retrofit2.Response
//import retrofit2.http.Body
//import retrofit2.http.DELETE
//import retrofit2.http.GET
//import retrofit2.http.Multipart
//import retrofit2.http.PATCH
//import retrofit2.http.POST
//import retrofit2.http.Part
//import retrofit2.http.Path
//
//interface AiApiService {
//
//    // 1. Upload File (Support Image & PDF/Docx)
//    @Multipart
//    @POST("upload/file") // Sesuaikan endpoint upload Anda
//    suspend fun uploadFile(
//        @Part file: MultipartBody.Part
//    ): Response<ApiResponse<UploadResponseData>>
//
//    // 2. Generate Flashcards
//    @POST("ai/generate-flashcards")
//    suspend fun generateFlashcards(
//        @Body request: AiGenerateRequest
//    ): Response<ApiResponse<AiGenerateResultData>>
//
//    // 3. Get Draft Details
//    @GET("ai/draft/{deckId}")
//    suspend fun getDraftDeck(
//        @Path("deckId") deckId: String
//    ): Response<ApiResponse<AiDraftDetailData>>
//
//    // 4. Update Card (Draft)
//    @PATCH("ai/draft/{deckId}/cards/{cardId}")
//    suspend fun updateDraftCard(
//        @Path("deckId") deckId: String,
//        @Path("cardId") cardId: String,
//        @Body request: UpdateCardRequest
//    ): Response<ApiResponse<AiCard>> // Backend return updated card object
//
//    // 5. Delete Card (Draft)
//    @DELETE("ai/draft/{deckId}/cards/{cardId}")
//    suspend fun deleteDraftCard(
//        @Path("deckId") deckId: String,
//        @Path("cardId") cardId: String
//    ): Response<ApiResponse<Any>>
//
//    // 6. Finalize / Save Deck
//    @POST("ai/draft/{deckId}/save")
//    suspend fun saveDeck(
//        @Path("deckId") deckId: String,
//        @Body request: SaveDeckRequest
//    ): Response<ApiResponse<AiDeckInfo>>
//}