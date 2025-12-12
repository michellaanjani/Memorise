package com.mobile.memorise.data.repository

// 1. Import Mapper (pastikan file mapper sudah dibuat seperti jawaban sebelumnya)
import com.mobile.memorise.data.mapper.toDomain

// 2. Import API Service
import com.mobile.memorise.data.remote.api.ApiService

// 3. Import DTO
// Ambil ApiResponseDto dari package common
import com.mobile.memorise.data.remote.dto.common.ApiResponseDto
// Ambil DeckDto, FolderDto, CardDto, dll dari package content
import com.mobile.memorise.data.remote.dto.content.*

// 4. Import Domain Models & Repo Interface
import com.mobile.memorise.domain.model.*
import com.mobile.memorise.domain.repository.ContentRepository

// 5. Import Utility
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class ContentRepositoryImpl @Inject constructor(
    private val api: ApiService
) : ContentRepository {
    // --- Helper Safe API Call ---
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
                val msg = body?.message ?: response.errorBody()?.string() ?: "Unknown API Error"
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
                val msg = response.body()?.message ?: "Unknown API Error"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =================================================================
    // IMPLEMENTASI FUNGSI
    // =================================================================

    override suspend fun getHomeData(): Result<Pair<List<Folder>, List<Deck>>> {
        return safeApiCall(
            apiCall = { api.getHomeData() },
            mapper = { dto ->
                // FolderDto & DeckDto diambil dari package content.*
                val folders = dto.folders?.map { it.toDomain() } ?: emptyList()
                val decks = dto.unassignedDecks?.map { it.toDomain() } ?: emptyList()
                Pair(folders, decks)
            }
        )
    }

    override suspend fun getAllFolders(): Result<List<Folder>> {
        // Kita menggunakan endpoint getHomeData karena list folder ada di sana
        return safeApiCall(
            apiCall = { api.getHomeData() },
            mapper = { dto ->
                // Ambil hanya folders, abaikan decks
                dto.folders?.map { it.toDomain() } ?: emptyList()
            }
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

    override suspend fun getDecks(folderId: String?): Result<List<Deck>> {
        return safeApiCall(
            apiCall = { api.getDecks(folderId) },
            mapper = { list -> list.map { it.toDomain() } }
        )
    }

    override suspend fun createDeck(name: String, desc: String, folderId: String?): Result<Deck> {
        val request = CreateDeckRequestDto(name, desc, folderId)
        return safeApiCall({ api.createDeck(request) }) { it.toDomain() }
    }

    override suspend fun updateDeck(id: String, name: String, desc: String, folderId: String?): Result<Deck> {
        // HAPUS: val request = CreateDeckRequestDto(name, desc, null) <--- INI PENYEBABNYA

        // GANTI DENGAN:
        val request = CreateDeckRequestDto(name, desc, folderId)

        return safeApiCall({ api.updateDeck(id, request) }) { it.toDomain() }
    }

    // --- PERBAIKAN 2: moveDeck ---
    override suspend fun moveDeck(deckId: String, folderId: String?): Result<Deck> {
        // LOGIC FIX:
        // Jika folderId itu null ATAU string kosong (""), paksa ubah jadi null
        val finalFolderId = if (folderId.isNullOrBlank()) null else folderId

        return safeApiCall(
            apiCall = {
                // Kirim finalFolderId yang sudah pasti null atau ID valid
                api.moveDeck(deckId, MoveDeckRequestDto(finalFolderId))
            },
            mapper = { dto -> dto.toDomain() }
        )
    }

    override suspend fun deleteDeck(id: String): Result<Unit> {
        return safeApiCallUnit { api.deleteDeck(id) }
    }

    override suspend fun getCardsByDeckId(deckId: String): Result<List<Card>> {
        return safeApiCall(
            apiCall = { api.getCardsByDeckId(deckId) },
            mapper = { list -> list.map { it.toDomain() } }
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

    // --- AI ---
    override suspend fun generateFlashcards(prompt: String, fileId: String?): Result<AiGeneratedContent> {
        val request = AiGenerateRequestDto(prompt, fileId)
        return safeApiCall(
            apiCall = { api.generateFlashcards(request) },
            mapper = { dto ->
                AiGeneratedContent(dto.deckId, dto.summary, dto.cardCount)
            }
        )
    }

    override suspend fun getAiDraft(deckId: String): Result<Deck> {
        return safeApiCall({ api.getAiDraft(deckId) }) { it.toDomain() }
    }

    override suspend fun updateDraftCard(deckId: String, cardId: String, front: String, back: String): Result<Card> {
        val request = CreateCardRequestDto(deckId, front, back)
        return safeApiCall({ api.updateDraftCard(deckId, cardId, request) }) { it.toDomain() }
    }

    override suspend fun deleteDraftCard(deckId: String, cardId: String): Result<Unit> {
        return safeApiCallUnit { api.deleteDraftCard(deckId, cardId) }
    }

    override suspend fun saveAiDraft(deckId: String, destinationFolderId: String?): Result<Deck> {
        val request = AiSaveRequestDto(destinationFolderId)
        return safeApiCall({ api.saveAiDraft(deckId, request) }) { it.toDomain() }
    }

    // --- QUIZ ---
    override suspend fun startQuiz(deckId: String): Result<QuizSession> {
        return safeApiCall(
            apiCall = { api.startQuiz(deckId) },
            mapper = { dto ->
                QuizSession(dto.quizId, dto.cards.map { it.toDomain() })
            }
        )
    }

    override suspend fun submitQuiz(quizId: String, answers: List<QuizAnswerInput>): Result<QuizResult> {
        val answerDtos = answers.map { QuizAnswerDto(it.cardId, it.answer) }
        val request = QuizSubmitRequestDto(quizId, answerDtos)
        return safeApiCall(
            apiCall = { api.submitQuiz(request) },
            mapper = { dto ->
                QuizResult(dto.id, dto.deckId, dto.score, dto.totalQuestions, dto.correctAnswers, dto.playedAt)
            }
        )
    }

    override suspend fun getQuizHistory(): Result<List<QuizResult>> {
        return safeApiCall(
            apiCall = { api.getQuizHistory() },
            mapper = { list ->
                list.map { QuizResult(it.id, it.deckId, it.score, it.totalQuestions, it.correctAnswers, it.playedAt) }
            }
        )
    }

    override suspend fun getQuizDetail(quizId: String): Result<QuizResult> {
        return safeApiCall(
            apiCall = { api.getQuizDetail(quizId) },
            mapper = { dto ->
                QuizResult(dto.id, dto.deckId, dto.score, dto.totalQuestions, dto.correctAnswers, dto.playedAt)
            }
        )
    }

    // --- FILE ---
    override suspend fun uploadFile(file: File): Result<UploadedFile> {
        return try {
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = api.uploadFile(body)
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data!!
                Result.success(UploadedFile(dto.id, dto.url, dto.originalName))
            } else {
                Result.failure(Exception(response.body()?.message ?: "Upload Failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFileUrl(id: String): Result<String> {
        return safeApiCall({ api.getFileUrl(id) }) { it.url }
    }

    override suspend fun deleteFile(id: String): Result<Unit> {
        return safeApiCallUnit { api.deleteFile(id) }
    }
}