package com.mobile.memorise.domain.model

// ==========================
// 1. BASIC MODELS
// ==========================

data class Folder(
    val id: String,
    val name: String,
    val description: String,
    val color: String,
    val deckCount: Int,      // Tambahan
    val createdAt: String
)

data class Deck(
    val id: String,
    val folderId: String?,
    val name: String,
    val description: String,
    val cardCount: Int

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

// ==========================
// 3. QUIZ MODELS
// ==========================

data class QuizSession(
    val quizId: String,
    val cards: List<Card> // Diubah agar sesuai dengan Repository (List<Card>)
)

data class QuizAnswerInput(
    val cardId: String,
    val answer: String
)

data class QuizResult(
    val id: String,
    val deckId: String,
    val score: Int, // Diubah ke Int agar sesuai dengan DTO dan Repo
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
    val originalName: String // Diubah ke String agar sesuai dengan Repo
)