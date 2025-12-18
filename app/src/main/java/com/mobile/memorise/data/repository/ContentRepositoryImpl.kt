package com.mobile.memorise.data.repository

import com.google.gson.JsonObject
import com.google.gson.JsonNull
import com.mobile.memorise.data.mapper.*
import com.mobile.memorise.data.remote.api.ApiService
import com.mobile.memorise.data.remote.dto.common.ApiResponseDto
import com.mobile.memorise.data.remote.dto.content.*
import com.mobile.memorise.domain.model.*
import com.mobile.memorise.domain.model.quiz.* // Hati-hati dengan package ini jika Anda sudah menghapus folder quiz
import com.mobile.memorise.domain.repository.ContentRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.net.URLConnection
import javax.inject.Inject

class ContentRepositoryImpl @Inject constructor(
    private val api: ApiService
) : ContentRepository {

    // =================================================================
    // HELPER FUNCTIONS
    // =================================================================

    private suspend fun <T, R> safeApiCall(
        apiCall: suspend () -> Response<ApiResponseDto<T>>,
        mapper: (T) -> R
    ): Result<R> {
        return try {
            val response = apiCall()
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(mapper(body.data))
            } else {
                val msg = body?.message ?: response.errorBody()?.string() ?: "Unknown API Error: ${response.code()}"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun safeApiCallUnit(
        apiCall: suspend () -> Response<ApiResponseDto<Unit>>
    ): Result<Unit> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val msg = response.body()?.message ?: "Unknown API Error: ${response.code()}"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =================================================================
    // 1. HOME & FOLDERS
    // =================================================================

    override suspend fun getHomeData(): Result<Pair<List<Folder>, List<Deck>>> {
        return safeApiCall(
            apiCall = { api.getHomeData() },
            mapper = { dto: HomeDataDto ->
                val folders = dto.folders?.map { it.toDomain() } ?: emptyList()
                val decks = dto.unassignedDecks?.map { it.toDomain() } ?: emptyList()
                Pair(folders, decks)
            }
        )
    }

    override suspend fun getAllFolders(): Result<List<Folder>> {
        return safeApiCall(
            apiCall = { api.getHomeData() },
            mapper = { dto: HomeDataDto -> dto.folders?.map { it.toDomain() } ?: emptyList() }
        )
    }

    override suspend fun createFolder(name: String, desc: String, color: String): Result<Folder> {
        val request = CreateFolderRequestDto(name, desc, color)
        return safeApiCall({ api.createFolder(request) }) { it.toDomain() }
    }

    override suspend fun updateFolder(id: String, name: String, desc: String, color: String): Result<Folder> {
        val request = CreateFolderRequestDto(name, desc, color)
        return safeApiCall({ api.updateFolder(id, request) }) { it.toDomain() }
    }

    override suspend fun deleteFolder(id: String): Result<Unit> {
        return safeApiCallUnit { api.deleteFolder(id) }
    }

    // =================================================================
    // 2. DECKS & CARDS
    // =================================================================

    override suspend fun getDecks(folderId: String?): Result<List<Deck>> {
        return safeApiCall(
            apiCall = { api.getDecks(folderId) },
            mapper = { list: List<DeckDto> -> list.map { it.toDomain() } }
        )
    }

    override suspend fun createDeck(name: String, desc: String, folderId: String?): Result<Deck> {
        val request = CreateDeckRequestDto(name, desc, folderId)
        return safeApiCall({ api.createDeck(request) }) { it.toDomain() }
    }

    override suspend fun updateDeck(id: String, name: String, desc: String, folderId: String?): Result<Deck> {
        val request = CreateDeckRequestDto(name, desc, folderId)
        return safeApiCall({ api.updateDeck(id, request) }) { it.toDomain() }
    }

    override suspend fun moveDeck(deckId: String, folderId: String?): Result<Deck> {
        val gsonObject = JsonObject()

        if (folderId != null) {
            gsonObject.addProperty("folderId", folderId)
        } else {
            gsonObject.add("folderId", JsonNull.INSTANCE)
        }

        val requestBody = gsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        // Gunakan requestBody, bukan JsonObject jika method api.moveDeck membutuhkan RequestBody
        // Pastikan signature di ApiService menerima @Body RequestBody
        return safeApiCall({ api.moveDeck(deckId, requestBody) }) { it.toDomain() }
    }

    override suspend fun moveDeckToHome(deckId: String): Result<Deck> {
        return try {
            val jsonString = """{"folderId": null}"""
            val requestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

            // Asumsi: api.moveDeck menerima RequestBody sebagai @Body
            val response = api.moveDeck(deckId, requestBody)
            val responseBody = response.body()

            if (response.isSuccessful && responseBody?.success == true && responseBody.data != null) {
                Result.success(responseBody.data.toDomain())
            } else {
                val msg = responseBody?.message ?: "Gagal memindahkan ke Home"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDeck(id: String): Result<Unit> {
        return safeApiCallUnit { api.deleteDeck(id) }
    }

    override suspend fun getCardsByDeckId(deckId: String): Result<List<Card>> {
        return safeApiCall(
            apiCall = { api.getCardsByDeckId(deckId) },
            mapper = { list: List<CardDto> -> list.map { it.toDomain() } }
        )
    }

    override suspend fun createCard(deckId: String, front: String, back: String): Result<Card> {
        val request = CreateCardRequestDto(deckId, front, back)
        return safeApiCall({ api.createCard(request) }) { it.toDomain() }
    }

    override suspend fun updateCard(id: String, front: String, back: String): Result<Card> {
        val request = CreateCardRequestDto(null, front, back)
        return safeApiCall({ api.updateCard(id, request) }) { it.toDomain() }
    }

    override suspend fun deleteCard(id: String): Result<Unit> {
        return safeApiCallUnit { api.deleteCard(id) }
    }

    // =================================================================
    // 3. AI GENERATOR IMPLEMENTATION
    // =================================================================

    override suspend fun generateFlashcards(prompt: String, fileId: String?, cardAmount: Int ): Result<AiGeneratedContent> {
        val validFormat = when {
            prompt.contains("Question", ignoreCase = true) -> "question"
            else -> "definition"
        }

        val request = AiGenerateRequest(
            fileId = fileId,
            format = validFormat,
            cardAmount = cardAmount
        )

        return safeApiCall(
            apiCall = { api.generateFlashcards(request) },
            mapper = { dto: AiGenerateResultData -> dto.toDomain() }
        )
    }

    // 🔥 PERBAIKAN: Menggunakan mapper .toDomainContent() untuk mengembalikan AiDraftContent
    override suspend fun getAiDraft(deckId: String): Result<AiDraftContent> {
        return safeApiCall(
            apiCall = { api.getDraftDeck(deckId) },
            mapper = { dto: AiDraftDetailData -> dto.toDomainContent() } // Mapper Baru
        )
    }

    // 🔥 PERBAIKAN: Update card mengembalikan konten draft terbaru (Deck + Cards)
    override suspend fun updateDraftCard(deckId: String, cardId: String, front: String, back: String): Result<AiDraftContent> {
        val request = UpdateCardRequest(front, back)
        return safeApiCall(
            apiCall = { api.updateDraftCard(deckId, cardId, request) },
            mapper = { dto: AiDraftDetailData -> dto.toDomainContent() } // Mapper Baru
        )
    }

    // 🔥 PERBAIKAN: Delete card mengembalikan konten draft terbaru (Deck + Cards)
    override suspend fun deleteDraftCard(deckId: String, cardId: String): Result<AiDraftContent> {
        return safeApiCall(
            apiCall = { api.deleteDraftCard(deckId, cardId) },
            mapper = { dto: AiDraftDetailData -> dto.toDomainContent() } // Mapper Baru
        )
    }

    override suspend fun saveAiDraft(deckId: String, destinationFolderId: String?, name: String?): Result<Deck> {
        val request = SaveDeckRequest(
            folderId = destinationFolderId,
            name = name // Pastikan parameter ini dikirim!
        )
        return safeApiCall(
            apiCall = { api.saveDeck(deckId, request) },
            mapper = { dto: AiDraftDetailData ->
                // Saat save, biasanya kita hanya butuh Deck info akhir
                Deck(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description ?: "",
                    // 🔥 PERBAIKAN: Gunakan "?." dan "?: 0"
                    // Artinya: Kalau cards null, anggap jumlahnya 0.
                    cardCount = dto.cards?.size ?: 0,
                    folderId = null,
                    updatedAt = ""
                )
            }
        )
    }

    // =================================================================
    // 4. FILE UPLOAD
    // =================================================================

    override suspend fun uploadFile(file: File): Result<UploadedFile> {
        return try {
            var mimeType = URLConnection.guessContentTypeFromName(file.name)

            if (mimeType == null) {
                mimeType = when (file.extension.lowercase()) {
                    "pdf" -> "application/pdf"
                    "jpg", "jpeg" -> "image/jpeg"
                    "png" -> "image/png"
                    "doc" -> "application/msword"
                    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    "txt" -> "text/plain"
                    else -> "application/octet-stream"
                }
            }

            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = api.uploadFile(body)
            val responseBody = response.body()

            if (response.isSuccessful && responseBody?.success == true && responseBody.data != null) {
                Result.success(responseBody.data.toDomain())
            } else {
                val msg = responseBody?.message ?: "Upload failed with code ${response.code()}"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFileUrl(id: String): Result<String> {
        return safeApiCall(
            apiCall = { api.getFileUrl(id) },
            mapper = { it.url }
        )
    }

    override suspend fun deleteFile(id: String): Result<Unit> {
        return safeApiCallUnit { api.deleteFile(id) }
    }

    // =================================================================
    // 5. QUIZ (IMPLEMENTASI BARU)
    // =================================================================

    override suspend fun startQuiz(deckId: String): Result<QuizSession> {
        return safeApiCall(
            apiCall = { api.startQuiz(deckId) },
            mapper = { it.toDomain() } // Mapper QuizSessionDto -> QuizSession
        )
    }

    override suspend fun submitQuiz(
        deckId: String,
        totalQuestions: Int,
        correctAnswers: Int,
        answers: List<QuizAnswerInput>
    ): Result<QuizResult> {
        // Build DTO Request Manual
        val answerDtos = answers.map { it.toDto() } // Menggunakan mapper QuizAnswerInput -> QuizAnswerDto

        val requestDto = QuizSubmitRequestDto(
            deckId = deckId,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            answers = answerDtos
        )

        return safeApiCall(
            apiCall = { api.submitQuiz(requestDto) },
            mapper = { it.toDomain() } // Mapper QuizResultDto -> QuizResult
        )
    }

    override suspend fun getQuizHistory(): Result<List<QuizResult>> {
        return safeApiCall(
            apiCall = { api.getQuizHistory() },
            mapper = { list -> list.map { it.toDomain() } }
        )
    }

    override suspend fun getQuizDetail(quizId: String): Result<QuizResult> {
        return safeApiCall(
            apiCall = { api.getQuizDetail(quizId) },
            mapper = { it.toDomain() }
        )
    }
}