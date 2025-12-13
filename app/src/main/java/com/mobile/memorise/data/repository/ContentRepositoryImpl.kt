package com.mobile.memorise.data.repository

import com.google.gson.JsonObject
import com.google.gson.JsonNull // <--- INI PENTING, TADI HILANG
import com.mobile.memorise.data.mapper.*
import com.mobile.memorise.data.remote.api.ApiService
import com.mobile.memorise.data.remote.dto.common.ApiResponseDto
import com.mobile.memorise.data.remote.dto.content.*
import com.mobile.memorise.domain.model.*
import com.mobile.memorise.domain.model.quiz.*
import com.mobile.memorise.domain.repository.ContentRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
            // Ini akan membuat JSON: {"folderId": null}
            gsonObject.add("folderId", JsonNull.INSTANCE)
        }

        // ✅ SOLUSI: Ubah JsonObject menjadi RequestBody
        val requestBody = gsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        // Sekarang tipe datanya sudah cocok dengan RequestBody
        return safeApiCall({ api.moveDeck(deckId, requestBody) }) { it.toDomain() }
    }

    // ✅ PERBAIKAN UTAMA DI SINI
    override suspend fun moveDeckToHome(deckId: String): Result<Deck> {
        return try {
            // KITA PAKSA TULIS STRING JSON SECARA MANUAL
            // Ini menjamin yang terkirim adalah {"folderId": null}
            val jsonString = """{"folderId": null}"""

            // Convert string ke RequestBody
            val requestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

            // Kirim requestBody, bukan JsonObject
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

    override suspend fun generateFlashcards(prompt: String, fileId: String?): Result<AiGeneratedContent> {
        val validFormat = when {
            prompt.contains("Question", ignoreCase = true) -> "question"
            else -> "definition"
        }

        val request = AiGenerateRequest(
            fileId = fileId,
            format = validFormat,
            cardAmount = 5
        )

        return safeApiCall(
            apiCall = { api.generateFlashcards(request) },
            mapper = { dto: AiGenerateResultData -> dto.toDomain() }
        )
    }

    override suspend fun getAiDraft(deckId: String): Result<Deck> {
        return safeApiCall(
            apiCall = { api.getDraftDeck(deckId) },
            mapper = { dto: AiDraftDetailData ->
                Deck(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description ?: "",
                    cardCount = dto.cards.size,
                    folderId = null,
                    updatedAt = ""
                )
            }
        )
    }

    override suspend fun updateDraftCard(deckId: String, cardId: String, front: String, back: String): Result<Card> {
        val request = UpdateCardRequest(front, back)
        return safeApiCall(
            apiCall = { api.updateDraftCard(deckId, cardId, request) },
            mapper = { dto: AiCard -> dto.toDomain() }
        )
    }

    override suspend fun deleteDraftCard(deckId: String, cardId: String): Result<Unit> {
        return safeApiCallUnit { api.deleteDraftCard(deckId, cardId) }
    }

    override suspend fun saveAiDraft(deckId: String, destinationFolderId: String?): Result<Deck> {
        val request = SaveDeckRequest(destinationFolderId)
        return safeApiCall(
            apiCall = { api.saveDeck(deckId, request) },
            mapper = { info: AiDeckInfo ->
                Deck(
                    id = info.id,
                    name = info.name,
                    description = info.description ?: "",
                    cardCount = 0,
                    folderId = destinationFolderId,
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
    // 5. QUIZ
    // =================================================================

    override suspend fun startQuiz(deckId: String): Result<QuizSession> {
        return safeApiCall(
            apiCall = { api.startQuiz(deckId) },
            mapper = { dto: QuizSessionDto ->
                QuizSession(
                    quizId = dto.quizId,
                    cards = dto.cards.map { it.toDomain().copy(deckId = deckId) }
                )
            }
        )
    }

    override suspend fun submitQuiz(quizId: String, answers: List<QuizAnswerInput>): Result<QuizResult> {
        val answerDtos = answers.map { QuizAnswerDto(it.cardId, it.answer) }
        val request = QuizSubmitRequestDto(quizId, answerDtos)

        return safeApiCall(
            apiCall = { api.submitQuiz(request) },
            mapper = { dto: QuizResultDto ->
                QuizResult(
                    id = dto.id,
                    deckId = dto.deckId,
                    score = dto.score,
                    totalQuestions = dto.totalQuestions,
                    correctAnswers = dto.correctAnswers,
                    playedAt = dto.playedAt
                )
            }
        )
    }

    override suspend fun getQuizHistory(): Result<List<QuizResult>> {
        return safeApiCall(
            apiCall = { api.getQuizHistory() },
            mapper = { list: List<QuizResultDto> ->
                list.map {
                    QuizResult(it.id, it.deckId, it.score, it.totalQuestions, it.correctAnswers, it.playedAt)
                }
            }
        )
    }

    override suspend fun getQuizDetail(quizId: String): Result<QuizResult> {
        return safeApiCall(
            apiCall = { api.getQuizDetail(quizId) },
            mapper = { dto: QuizResultDto ->
                QuizResult(dto.id, dto.deckId, dto.score, dto.totalQuestions, dto.correctAnswers, dto.playedAt)
            }
        )
    }
}