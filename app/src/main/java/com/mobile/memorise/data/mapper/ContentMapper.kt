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

// 🔥 UPDATE: Menambahkan mapping untuk 'updatedAt'
fun DeckDto.toDomain(): Deck {
    return Deck(
        id = this.id,
        name = this.name,
        description = this.description ?: "",
        cardCount = this.cardCount,
        folderId = this.folderId,
        updatedAt = this.updatedAt ?: "" // Mapping field tanggal update
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
// 4. QUIZ MAPPERS
// =================================================================

// --- A. Start Quiz (Response API -> Domain) ---
fun QuizSessionDto.toDomain(): QuizStartData {
    return QuizStartData(
        deckId = this.quizId,
        totalQuestions = this.cards.size,
        questions = this.cards.map { it.toQuizQuestion() }
    )
}

// Helper: Mengubah CardDto ke QuizQuestion
fun CardDto.toQuizQuestion(): QuizQuestion {
    return QuizQuestion(
        cardId = this.id,
        question = this.front,
        correctAnswer = this.back,
        options = emptyList(),
        explanation = null
    )
}

// --- B. Submit Quiz (Request Domain -> Request API) ---
fun QuizSubmitRequest.toDto(): QuizSubmitRequestDto {
    return QuizSubmitRequestDto(
        quizId = this.deckId,
        answers = this.details.map { it.toDto() }
    )
}

fun QuizAnswerDetail.toDto(): QuizAnswerDto {
    return QuizAnswerDto(
        cardId = this.cardId,
        answer = this.userAnswer
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
        userId = "",
        details = emptyList(),
        version = 0
    )
}