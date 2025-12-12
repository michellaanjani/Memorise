package com.mobile.memorise.data.remote.api

import com.mobile.memorise.data.remote.dto.auth.AuthDataDto
import com.mobile.memorise.data.remote.dto.auth.LoginRequestDto
import com.mobile.memorise.data.remote.dto.auth.RegisterRequestDto
import com.mobile.memorise.data.remote.dto.common.ApiResponseDto
// Import ini sudah mencakup DeckDto, FolderDto, CardDto, dll.
import com.mobile.memorise.data.remote.dto.content.*

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // =================================================================
    // 1. AUTH MODULE
    // =================================================================
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<ApiResponseDto<AuthDataDto>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<ApiResponseDto<AuthDataDto>>

    // =================================================================
    // 3. HOME & DASHBOARD
    // =================================================================
    @GET("home")
    suspend fun getHomeData(): Response<ApiResponseDto<HomeDataDto>>

    // =================================================================
    // 4. FOLDER MODULE
    // =================================================================
    @GET("folders")
    suspend fun getAllFolders(): Response<ApiResponseDto<List<FolderDto>>>

    @POST("folders")
    suspend fun createFolder(@Body request: CreateFolderRequestDto): Response<ApiResponseDto<FolderDto>>

    @PATCH("folders/{id}")
    suspend fun updateFolder(
        @Path("id") id: String,
        @Body request: CreateFolderRequestDto
    ): Response<ApiResponseDto<FolderDto>>

    @DELETE("folders/{id}")
    suspend fun deleteFolder(@Path("id") id: String): Response<ApiResponseDto<Unit>>

    // =================================================================
    // 5. DECK MODULE
    // =================================================================
    @GET("decks")
    suspend fun getDecks(@Query("folderId") folderId: String? = null): Response<ApiResponseDto<List<DeckDto>>>

    @POST("decks")
    suspend fun createDeck(@Body request: CreateDeckRequestDto): Response<ApiResponseDto<DeckDto>>

    @PATCH("decks/{id}")
    suspend fun updateDeck(
        @Path("id") id: String,
        @Body request: CreateDeckRequestDto
    ): Response<ApiResponseDto<DeckDto>>

    // Tambahkan method ini di interface ContentApi
    @PATCH("decks/{id}/move")
    suspend fun moveDeck(
        @Path("id") deckId: String,
        @Body request: MoveDeckRequestDto
    ): Response<ApiResponseDto<DeckDto>>

    @DELETE("decks/{id}")
    suspend fun deleteDeck(@Path("id") id: String): Response<ApiResponseDto<Unit>>

    // =================================================================
    // 6. CARD MODULE
    // =================================================================
    @POST("cards")
    suspend fun createCard(@Body request: CreateCardRequestDto): Response<ApiResponseDto<CardDto>>

    @GET("cards/deck/{deckId}")
    suspend fun getCardsByDeckId(@Path("deckId") deckId: String): Response<ApiResponseDto<List<CardDto>>>

    @PATCH("cards/{id}")
    suspend fun updateCard(
        @Path("id") id: String,
        @Body request: CreateCardRequestDto
    ): Response<ApiResponseDto<CardDto>>

    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") id: String): Response<ApiResponseDto<Unit>>

    // =================================================================
    // 7. AI GENERATOR MODULE
    // =================================================================
    @POST("ai/generate-flashcards")
    suspend fun generateFlashcards(@Body request: AiGenerateRequestDto): Response<ApiResponseDto<AiGeneratedResponseDto>>

    @GET("ai/draft/{deckId}")
    suspend fun getAiDraft(@Path("deckId") deckId: String): Response<ApiResponseDto<DeckDto>>

    @PATCH("ai/draft/{deckId}/cards/{cardId}")
    suspend fun updateDraftCard(
        @Path("deckId") deckId: String,
        @Path("cardId") cardId: String,
        @Body request: CreateCardRequestDto
    ): Response<ApiResponseDto<CardDto>>

    @DELETE("ai/draft/{deckId}/cards/{cardId}")
    suspend fun deleteDraftCard(
        @Path("deckId") deckId: String,
        @Path("cardId") cardId: String
    ): Response<ApiResponseDto<Unit>>

    @POST("ai/draft/{deckId}/save")
    suspend fun saveAiDraft(
        @Path("deckId") deckId: String,
        @Body request: AiSaveRequestDto
    ): Response<ApiResponseDto<DeckDto>>

    // =================================================================
    // 8. QUIZ SYSTEM
    // =================================================================
    @GET("quiz/start/{deckId}")
    suspend fun startQuiz(@Path("deckId") deckId: String): Response<ApiResponseDto<QuizStartResponseDto>>

    @POST("quiz/submit")
    suspend fun submitQuiz(@Body request: QuizSubmitRequestDto): Response<ApiResponseDto<QuizResultDto>>

    @GET("quiz/history")
    suspend fun getQuizHistory(): Response<ApiResponseDto<List<QuizResultDto>>>

    @GET("quiz/{quizId}")
    suspend fun getQuizDetail(@Path("quizId") quizId: String): Response<ApiResponseDto<QuizResultDto>>

    // =================================================================
    // 9. FILE UPLOAD MODULE
    // =================================================================
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<ApiResponseDto<FileUploadResponseDto>>

    @GET("files/{id}/url")
    suspend fun getFileUrl(@Path("id") id: String): Response<ApiResponseDto<FileUrlResponseDto>>

    @DELETE("files/{id}")
    suspend fun deleteFile(@Path("id") id: String): Response<ApiResponseDto<Unit>>
}