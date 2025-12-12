package com.mobile.memorise.data.remote.dto.content

import com.google.gson.annotations.SerializedName
import com.mobile.memorise.data.remote.dto.common.*

// =================================================================
// 1. DEFINISI KELAS DASAR (PERBAIKAN UTAMA DI SINI)
// =================================================================

data class FolderDto(
    @SerializedName("id", alternate = ["_id"]) // Tambahan wajib: Backend kirim "_id", bukan "id"
    val id: String,

    val name: String,
    val description: String?,
    val color: String,
    val decksCount: Int = 0,
    val createdAt: String? = null
)

data class DeckDto(
    @SerializedName("id", alternate = ["_id"])  // Tambahan wajib: Backend kirim "_id", bukan "id"
    val id: String,

    val folderId: String?,
    val name: String,
    val description: String?,
    val cardCount: Int = 0,
    val createdAt: String? = null
)

data class CardDto(
    @SerializedName("id", alternate = ["_id"])  // Tambahan wajib untuk konsistensi
    val id: String,

    val deckId: String,
    val front: String,
    val back: String,
    val createdAt: String? = null
)

// =================================================================
// 2. MODUL HOME & REQUEST BODIES
// =================================================================

// --- HOME ---
data class HomeDataDto(
    @SerializedName("folders") val folders: List<FolderDto>?,
    @SerializedName("unassignedDecks") val unassignedDecks: List<DeckDto>?
)

// --- FOLDER REQUEST ---
data class CreateFolderRequestDto(
    val name: String,
    val description: String,
    val color: String,
    val deckCount: Int = 0,
    val createdAt: String? = null
)

// --- DECK REQUEST ---
data class CreateDeckRequestDto(
    val name: String,
    val description: String,
    val folderId: String?
)

// --- CARD REQUEST ---
data class CreateCardRequestDto(
    val deckId: String?,
    val front: String,
    val back: String
)

// Request Body untuk Move Deck
data class MoveDeckRequestDto(
    val folderId: String? // Nullable: Kirim null untuk pindah ke Home, kirim ID untuk masuk folder
)

// =================================================================
// 3. AI GENERATOR MODULE
// =================================================================

data class AiGenerateRequestDto(
    val prompt: String,
    val fileId: String?
)

data class AiGeneratedResponseDto(
    @SerializedName("deckId") val deckId: String,
    @SerializedName("summary") val summary: String?,
    @SerializedName("cardCount") val cardCount: Int
)

data class AiSaveRequestDto(
    val destinationFolderId: String?
)

// =================================================================
// 4. QUIZ SYSTEM
// =================================================================

data class QuizStartResponseDto(
    @SerializedName("quizId") val quizId: String,
    @SerializedName("cards") val cards: List<CardDto>
)

data class QuizSubmitRequestDto(
    val quizId: String,
    val answers: List<QuizAnswerDto>
)

data class QuizAnswerDto(
    val cardId: String,
    val answer: String // 'easy', 'hard', etc
)

data class QuizResultDto(
    @SerializedName("_id") // Kemungkinan besar ini juga pakai _id
    val id: String,

    val deckId: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val playedAt: String
)

// =================================================================
// 5. FILE UPLOAD MODULE
// =================================================================

data class FileUploadResponseDto(
    @SerializedName("_id") // Tambahkan jaga-jaga jika file object juga pakai _id
    val id: String,

    val url: String,
    val originalName: String
)

data class FileUrlResponseDto(
    val url: String
)