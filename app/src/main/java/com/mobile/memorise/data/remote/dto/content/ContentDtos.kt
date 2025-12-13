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
    val updatedAt: String? = null // Opsional: Tambahkan jika folder juga butuh tanggal update
)

data class DeckDto(
    @SerializedName("id", alternate = ["_id"])
    val id: String,
    val folderId: String?,
    val name: String,
    val description: String?,
    val cardCount: Int = 0,
    val createdAt: String? = null,
    // 🔥 UPDATE: Field ini wajib ada untuk fitur "Updated Date"
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
// 2. HOME & REQUEST BODIES (Folder/Deck/Card)
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
    val deckId: String?,
    val front: String,
    val back: String
)

data class MoveDeckRequestDto(
    val folderId: String?
)

// =================================================================
// 3. QUIZ SYSTEM DTOs
// =================================================================

data class QuizSessionDto(
    @SerializedName("quizId") val quizId: String,
    @SerializedName("cards") val cards: List<CardDto>
)

data class QuizSubmitRequestDto(
    val quizId: String,
    val answers: List<QuizAnswerDto>
)

data class QuizAnswerDto(
    val cardId: String,
    val answer: String
)

data class QuizResultDto(
    @SerializedName("_id") val id: String,
    val deckId: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val playedAt: String
)

// =================================================================
// 4. AI & FILE MODULE DTOs
// =================================================================

// --- AI GENERATION REQUEST ---
data class AiGenerateRequest(
    val fileId: String?,
    val format: String,     // 'question' or 'definition'
    val cardAmount: Int
)

// --- AI GENERATION RESPONSE ---
data class AiGenerateResultData(
    val deck: AiDeckInfo,
    val cards: List<AiCard>
)

data class AiDeckInfo(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String?
)

// --- AI CARD (Digunakan di Draft) ---
data class AiCard(
    @SerializedName("_id") val id: String,
    val front: String,
    val back: String,
    val deckId: String? = null
)

// --- AI DRAFT DETAIL ---
data class AiDraftDetailData(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String?,
    val isDraft: Boolean = true,
    val cards: List<AiCard>
)

// --- AI ACTIONS ---
data class UpdateCardRequest(
    val front: String,
    val back: String
)

data class SaveDeckRequest(
    val folderId: String?
)

// --- FILE UPLOAD ---
data class UploadResponseData(
    @SerializedName("_id") val id: String,
    val url: String?,
    val originalname: String
)

data class FileUrlDto(
    val url: String
)