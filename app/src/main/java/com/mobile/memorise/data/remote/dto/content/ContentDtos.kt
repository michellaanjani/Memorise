package com.mobile.memorise.data.remote.dto.content

import com.google.gson.annotations.SerializedName

// =================================================================
// 1. STANDARD CONTENT (Folder, Deck, Card)
// =================================================================

data class FolderDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val name: String,
    val description: String?,
    val color: String,
    val decksCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class DeckDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val folderId: String?,
    val name: String,
    val description: String?,
    val cardCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CardDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// =================================================================
// 2. HOME & REQUEST BODIES
// =================================================================

data class HomeDataDto(
    @SerializedName("folders") val folders: List<FolderDto>?,
    @SerializedName("unassignedDecks") val unassignedDecks: List<DeckDto>?
)

data class CreateFolderRequestDto(
    val name: String,
    val description: String,
    val color: String
)

data class CreateDeckRequestDto(
    val name: String,
    val description: String,
    val folderId: String?
)

data class CreateCardRequestDto(
    val deckId: String? = null, // Nullable agar bisa dipakai untuk Update (PATCH)
    val front: String,
    val back: String
)

data class MoveDeckRequestDto(
    val folderId: String?
)

// =================================================================
// 3. QUIZ SYSTEM DTOs (BAGIAN INI YANG DIPERBAIKI)
// =================================================================

// Sesuaikan dengan JSON Log: { "deckId": "...", "totalQuestions": 6, "questions": [...] }
data class QuizSessionDto(
    @SerializedName("deckId")
    val deckId: String? = null,

    @SerializedName("totalQuestions")
    val totalQuestions: Int? = 0,

    @SerializedName("questions")
    val questions: List<QuizQuestionDto>? = emptyList() // Default empty biar gak crash
)

// Class baru untuk menampung detail pertanyaan dari server
data class QuizQuestionDto(
    @SerializedName("cardId")
    val cardId: String,

    @SerializedName("question")
    val question: String,

    @SerializedName("correctAnswer")
    val correctAnswer: String,

    @SerializedName("options")
    val options: List<String>? = emptyList(), // Penting: List String, bukan Object

    @SerializedName("explanation")
    val explanation: String? = null
)

data class QuizSubmitRequestDto(
    val deckId: String,          // Biasanya butuh deckId
    val totalQuestions: Int,
    val correctAnswers: Int,

    @SerializedName("details") // <--- TAMBAHKAN INI AGAR SERVER BACA SEBAGAI "details"
    val answers: List<QuizAnswerDto>
)
data class QuizAnswerDto(
    val cardId: String,
    val isCorrect: Boolean,
    val userAnswer: String,

    // 🔥 TAMBAHKAN INI
    val correctAnswer: String
)


// =================================================================
// DTOs untuk HISTORY & DETAIL QUIZ
// =================================================================

data class QuizResultDto(
    @SerializedName("_id")
    val id: String,

    // 🔥 FIX 1: deckId di JSON adalah Object, bukan String
    @SerializedName("deckId")
    val deck: DeckSummaryDto,

    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,

    @SerializedName("createdAt")
    val playedAt: String,

    // 🔥 FIX 2: Tambahkan field details (Nullable, karena di list history field ini tidak ada)
    @SerializedName("details")
    val details: List<QuizDetailItemDto>? = null
)

// Helper DTO untuk menangkap object deckId: { "_id": "...", "name": "..." }
data class DeckSummaryDto(
    @SerializedName("_id")
    val id: String,

    @SerializedName("name")
    val name: String
)

// Helper DTO untuk item di dalam array "details"
data class QuizDetailItemDto(
    @SerializedName("_id")
    val id: String,

    // 🔥 FIX 3: cardId di sini adalah Object, berisi front & back
    @SerializedName("cardId")
    val card: CardSummaryDto,

    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val explanation: String?
)

// Helper DTO untuk menangkap object cardId: { "_id": "...", "front": "...", "back": "..." }
data class CardSummaryDto(
    @SerializedName("_id")
    val id: String,
    val front: String,
    val back: String
)
// =================================================================
// 4. AI & FILE MODULE DTOs
// =================================================================

data class AiGenerateRequest(
    val fileId: String?,
    val format: String,
    val cardAmount: Int
)

data class AiGenerateResultData(
    val deck: AiDeckInfo,
    val cards: List<AiCard>
)

data class AiDeckInfo(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val name: String,
    val description: String?
)

data class AiCard(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val front: String,
    val back: String,
    val deckId: String? = null
)

data class AiDraftDetailData(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val name: String,
    val description: String?,
    val userId: String,
    val folderId: String?,
    val isDraft: Boolean,
    val draftData: Any?,
    val cards: List<AiCard> = emptyList()
)

data class UpdateCardRequest(
    val front: String,
    val back: String
)

data class SaveDeckRequest(
    val folderId: String?,
    val name: String?
)

data class UploadResponseData(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val url: String?,
    val originalname: String
)

data class FileUrlDto(
    val url: String
)