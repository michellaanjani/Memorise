package com.mobile.memorise.data.remote.dto.content

import com.google.gson.annotations.SerializedName

// =================================================================
// 1. HELPER DTOs (Komponen Kecil)
// =================================================================

// Helper untuk menangkap object cardId: { "_id": "...", "front": "...", "back": "..." }
// Digunakan di endpoint History Detail
data class CardSummaryDto(
    @SerializedName("_id")
    val id: String,
    val front: String,
    val back: String
)

// Helper untuk menangkap object deckId: { "_id": "...", "name": "..." }
// Digunakan di endpoint History List & Detail
data class DeckSummaryDto(
    @SerializedName("_id")
    val id: String,
    val name: String
)

// =================================================================
// 2. STANDARD CONTENT (Folder, Deck, Card)
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
// 3. HOME & REQUEST BODIES
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
    val deckId: String? = null,
    val front: String,
    val back: String
)

data class MoveDeckRequestDto(
    val folderId: String?
)

// =================================================================
// 4. QUIZ START & SUBMIT (Request & Session)
// =================================================================

data class QuizSessionDto(
    @SerializedName("deckId")
    val deckId: String? = null,

    @SerializedName("totalQuestions")
    val totalQuestions: Int? = 0,

    @SerializedName("questions")
    val questions: List<QuizQuestionDto>? = emptyList()
)

data class QuizQuestionDto(
    @SerializedName("cardId")
    val cardId: String,

    @SerializedName("question")
    val question: String,

    @SerializedName("correctAnswer")
    val correctAnswer: String,

    @SerializedName("options")
    val options: List<String>? = emptyList(),

    @SerializedName("explanation")
    val explanation: String? = null
)

// Request body saat mengirim jawaban
data class QuizSubmitRequestDto(
    val deckId: String,
    val totalQuestions: Int,
    val correctAnswers: Int,

    @SerializedName("details")
    val answers: List<QuizAnswerDto>
)

data class QuizAnswerDto(
    val cardId: String,
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String
)

// =================================================================
// 5. QUIZ RESPONSES (PENANGANAN 3 SKENARIO BERBEDA)
// =================================================================

// --- SKENARIO A: RESPONSE SUBMIT QUIZ ---
// deckId = String, cardId = String
data class QuizResultDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("deckId") val deckId: String, // String
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    @SerializedName("createdAt") val playedAt: String?,
    @SerializedName("details") val details: List<QuizDetailItemDto>?
)

data class QuizDetailItemDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("cardId")
    val cardId: String, // String (Hanya ID)
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val explanation: String? = null
)

// --- SKENARIO B: RESPONSE HISTORY LIST ---
// deckId = Object (DeckSummaryDto), details biasanya tidak dipakai di list
data class QuizHistoryDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("deckId")
    val deck: DeckSummaryDto, // Object Deck
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    @SerializedName("createdAt")
    val playedAt: String?
)

// --- SKENARIO C: RESPONSE QUIZ DETAIL (HISTORY DETAIL) ---
// deckId = Object, cardId = Object (Expanded)
data class QuizDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("deckId") val deck: DeckSummaryDto, // Object Deck
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    @SerializedName("createdAt") val playedAt: String?,
    @SerializedName("details") val details: List<QuizDetailItemExpandedDto>?
)

data class QuizDetailItemExpandedDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("cardId")
    val card: CardSummaryDto, // Object Card (Front/Back)
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val explanation: String? = null
)

// =================================================================
// 6. AI & FILE MODULE DTOs
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