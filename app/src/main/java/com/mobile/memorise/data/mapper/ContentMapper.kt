package com.mobile.memorise.data.mapper

import com.mobile.memorise.data.remote.dto.content.*
import com.mobile.memorise.domain.model.*
import com.mobile.memorise.domain.model.quiz.*

// =================================================================
// 1. STANDARD CONTENT MAPPERS (Folder, Deck, Card)
// =================================================================

fun FolderDto.toDomain(): Folder {
    return Folder(
        id = this.id,
        name = this.name,
        description = this.description ?: "",
        color = this.color,
        deckCount = this.decksCount,
        createdAt = this.createdAt ?: ""
    )
}

fun DeckDto.toDomain(): Deck {
    return Deck(
        id = this.id,
        name = this.name,
        description = this.description ?: "",
        cardCount = this.cardCount,
        folderId = this.folderId,
        updatedAt = this.updatedAt ?: ""
    )
}

fun CardDto.toDomain(): Card {
    return Card(
        id = this.id,
        front = this.front,
        back = this.back,
        deckId = this.deckId
    )
}

// =================================================================
// 2. FILE UPLOAD MAPPER
// =================================================================

fun UploadResponseData.toDomain(): UploadedFile {
    return UploadedFile(
        id = this.id,
        url = this.url ?: "",
        originalName = this.originalname
    )
}

// =================================================================
// 3. AI GENERATION MAPPERS
// =================================================================

fun AiGenerateResultData.toDomain(): AiGeneratedContent {
    return AiGeneratedContent(
        deckId = this.deck.id,
        summary = this.deck.description ?: "No description provided",
        cardCount = this.cards.size
    )
}

fun AiCard.toDomain(): Card {
    return Card(
        id = this.id,
        deckId = this.deckId ?: "",
        front = this.front,
        back = this.back
    )
}

// =================================================================
// 4. QUIZ MAPPERS (BAGIAN INI YANG DIPERBAIKI TOTAL)
// =================================================================

// --- A. Start Quiz (Response API -> Domain) ---
fun QuizSessionDto.toDomain(): QuizStartData {
    return QuizStartData(
        deckId = this.deckId ?: "", // Handle null
        totalQuestions = this.totalQuestions ?: 0,
        // PENTING: Gunakan map dari 'questions', bukan 'cards'.
        // Jika null, return emptyList() agar tidak crash saat di-loop/size().
        questions = this.questions?.map { it.toDomain() } ?: emptyList()
    )
}

// Helper: Mengubah QuizQuestionDto ke Domain QuizQuestion
fun QuizQuestionDto.toDomain(): QuizQuestion {
    return QuizQuestion(
        cardId = this.cardId,
        question = this.question,
        correctAnswer = this.correctAnswer,
        // PENTING: Handle null options agar UI tidak crash saat .take(4)
        options = this.options ?: emptyList(),
        explanation = this.explanation
    )
}

// --- B. Submit Quiz (Request Domain -> Request API) ---
fun QuizSubmitRequest.toDto(): QuizSubmitRequestDto {
    return QuizSubmitRequestDto(
        deckId = this.deckId,
        totalQuestions = this.totalQuestions, // Sertakan data statistik
        correctAnswers = this.correctAnswers,
        answers = this.details.map { it.toDto() }
    )
}

fun QuizAnswerDetail.toDto(): QuizAnswerDto {
    return QuizAnswerDto(
        cardId = this.cardId,
        isCorrect = this.isCorrect,
        userAnswer = this.userAnswer,

        // 🔥 TAMBAHKAN MAPPING INI
        correctAnswer = this.correctAnswer
    )
}

// --- C. Submit Result (Response API -> Domain) ---
fun QuizResultDto.toDomain(): QuizSubmitData {
    return QuizSubmitData(
        id = this.id,
        deckId = this.deckId,
        score = this.score,
        correctAnswers = this.correctAnswers,
        totalQuestions = this.totalQuestions,
        createdAt = this.playedAt,
        updatedAt = this.playedAt,
        userId = "", // Tidak dikirim server, biarkan kosong
        details = emptyList(),
        version = 0
    )
}