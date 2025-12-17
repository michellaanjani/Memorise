package com.mobile.memorise.data.remote.api

import com.google.gson.JsonObject
import com.mobile.memorise.data.remote.dto.common.ApiResponseDto
import com.mobile.memorise.data.remote.dto.content.*
//import com.mobile.memorise.domain.model.quiz.QuizStartResponse
//import com.mobile.memorise.domain.model.quiz.QuizSubmitRequest
//import com.mobile.memorise.domain.model.quiz.QuizSubmitResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // =================================================================
    // HOME & FOLDER
    // =================================================================
    @GET("home")
    suspend fun getHomeData(): Response<ApiResponseDto<HomeDataDto>>

    @POST("folders")
    suspend fun createFolder(@Body request: CreateFolderRequestDto): Response<ApiResponseDto<FolderDto>>

    @PUT("folders/{id}")
    suspend fun updateFolder(
        @Path("id") id: String,
        @Body request: CreateFolderRequestDto
    ): Response<ApiResponseDto<FolderDto>>

    @DELETE("folders/{id}")
    suspend fun deleteFolder(@Path("id") id: String): Response<ApiResponseDto<Unit>>

    // =================================================================
    // DECK
    // =================================================================
    @GET("decks")
    suspend fun getDecks(@Query("folderId") folderId: String?): Response<ApiResponseDto<List<DeckDto>>>

    @POST("decks")
    suspend fun createDeck(@Body request: CreateDeckRequestDto): Response<ApiResponseDto<DeckDto>>

    @PATCH("decks/{id}")
    suspend fun updateDeck(
        @Path("id") id: String,
        @Body request: CreateDeckRequestDto
    ): Response<ApiResponseDto<DeckDto>>

    // Endpoint khusus untuk memindahkan deck (Move Deck)
//    @PATCH("decks/{id}/move")
//    suspend fun moveDeck(
//        @Path("id") id: String,
//        @Body body: JsonObject
//    ): Response<ApiResponseDto<DeckDto>>
    @PATCH("decks/{id}/move")
    suspend fun moveDeck(
        @Path("id") id: String,
        @Body body: RequestBody // Tetap pakai JsonObject untuk keamanan 'null'
    ): Response<ApiResponseDto<DeckDto>>

    @DELETE("decks/{id}")
    suspend fun deleteDeck(@Path("id") id: String): Response<ApiResponseDto<Unit>>

    // =================================================================
    // CARD
    // =================================================================
    @GET("cards/deck/{deckId}")
    suspend fun getCardsByDeckId(@Path("deckId") deckId: String): Response<ApiResponseDto<List<CardDto>>>

    @POST("cards")
    suspend fun createCard(@Body request: CreateCardRequestDto): Response<ApiResponseDto<CardDto>>

    @PATCH("cards/{id}")
    suspend fun updateCard(
        @Path("id") id: String,
        @Body request: CreateCardRequestDto
    ): Response<ApiResponseDto<CardDto>>

    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") id: String): Response<ApiResponseDto<Unit>>

    // =================================================================
    // AI (UPDATED: Menggunakan ApiResponseDto)
    // =================================================================


    @POST("ai/generate-flashcards")
    suspend fun generateFlashcards(
        @Body request: AiGenerateRequest
    ): Response<ApiResponseDto<AiGenerateResultData>>

    @GET("ai/draft/{deckId}")
    suspend fun getDraftDeck(
        @Path("deckId") deckId: String
    ): Response<ApiResponseDto<AiDraftDetailData>>

    @PATCH("ai/draft/{deckId}/cards/{cardId}")
    suspend fun updateDraftCard(
        @Path("deckId") deckId: String,
        @Path("cardId") cardId: String,
        @Body request: UpdateCardRequest
    ): Response<ApiResponseDto<AiDraftDetailData>>

    @DELETE("ai/draft/{deckId}/cards/{cardId}") // sesuaikan path
    suspend fun deleteDraftCard(
        @Path("deckId") deckId: String,
        @Path("cardId") cardId: String
    ): Response<ApiResponseDto<AiDraftDetailData>>

    @POST("ai/draft/{deckId}/save")
    suspend fun saveDeck(
        @Path("deckId") deckId: String,
        @Body request: SaveDeckRequest
    ): Response<ApiResponseDto<AiDraftDetailData>>



//    // =================================================================
//    // QUIZ
//    // =================================================================
//    //
//
//    @GET("quiz/start/{deckId}")
//    suspend fun startQuiz(@Path("deckId") deckId: String): Response<QuizStartResponse>
//
//    @POST("quiz/submit")
//    suspend fun submitQuiz(@Body request: QuizSubmitRequest): Response<QuizSubmitResponse>
//
//    @GET("quiz/history")
//    suspend fun getQuizHistory(): Response<ApiResponseDto<List<QuizResultDto>>>
//
//    @GET("quiz/{id}")
//    suspend fun getQuizDetail(@Path("id") id: String): Response<ApiResponseDto<QuizResultDto>>
//
//    // --- QUIZ ENDPOINTS ---
    @GET("quiz/start/{deckId}")
    suspend fun startQuiz(@Path("deckId") deckId: String): Response<ApiResponseDto<QuizSessionDto>>

    @POST("quiz/submit")
    suspend fun submitQuiz(@Body request: QuizSubmitRequestDto): Response<ApiResponseDto<QuizResultDto>>

    @GET("quiz/history")
    suspend fun getQuizHistory(): Response<ApiResponseDto<List<QuizResultDto>>>

    @GET("quiz/{id}")
    suspend fun getQuizDetail(@Path("id") id: String): Response<ApiResponseDto<QuizResultDto>>

    // --- FILE ENDPOINTS (INI YANG HILANG SEBELUMNYA) ---
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<ApiResponseDto<UploadResponseData>>

    @GET("files/{id}/url")
    suspend fun getFileUrl(@Path("id") id: String): Response<ApiResponseDto<FileUrlDto>>

    @DELETE("files/{id}")
    suspend fun deleteFile(@Path("id") id: String): Response<ApiResponseDto<Unit>>

}