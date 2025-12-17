package com.mobile.memorise.domain.model

// ==========================
// 1. BASIC MODELS
// ==========================

data class Folder(
    val id: String,
    val name: String,
    val description: String,
    val color: String,
    val deckCount: Int,
    val createdAt: String
)

data class Deck(
    val id: String,
    val folderId: String?,
    val name: String,
    val description: String,
    val cardCount: Int,
    val updatedAt: String
)

data class Card(
    val id: String,
    val deckId: String,
    val front: String,
    val back: String
)

// ==========================
// 2. AI MODELS
// ==========================

data class AiGeneratedContent(
    val deckId: String,
    val summary: String?,
    val cardCount: Int
)

/**
 * 🔥 TAMBAHAN PENTING:
 * Model ini digunakan untuk menampung respon dari:
 * - getAiDraft
 * - updateDraftCard
 * - deleteDraftCard
 * Agar ViewModel bisa langsung mendapatkan Deck info DAN List Card terbaru.
 */
data class AiDraftContent(
    val deck: Deck,
    val cards: List<Card>
)

// ==========================
// 3. QUIZ MODELS
// ==========================

/**
 * Model khusus untuk satu pertanyaan kuis.
 * Dibuat terpisah dari 'Card' karena kuis mungkin memiliki pilihan ganda (options).
 */
data class QuizQuestion(
    val cardId: String,
    val question: String,
    val correctAnswer: String,
    val options: List<String>, // List pilihan jawaban (A, B, C, D)
    val explanation: String?
)

data class QuizSession(
    val deckId: String,        // Disesuaikan dengan DTO (deckId)
    val totalQuestions: Int,
    val questions: List<QuizQuestion> // Menggunakan QuizQuestion, bukan Card
)

data class QuizAnswerInput(
    val cardId: String,
    val userAnswer: String,    // Jawaban user
    val correctAnswer: String  // Jawaban benar (diperlukan server untuk validasi)
)

data class QuizResult(
    val id: String,
    val deckId: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val playedAt: String
)

// ==========================
// 4. FILE UPLOAD MODELS
// ==========================

data class UploadedFile(
    val id: String,
    val url: String,
    val originalName: String
)