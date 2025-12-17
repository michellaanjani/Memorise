package com.mobile.memorise.data.mapper

import com.mobile.memorise.data.remote.dto.content.*
import com.mobile.memorise.domain.model.*

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
        // 🔥 PERBAIKAN: Gunakan safe call (?.) dan elvis operator (?: 0)
        cardCount = this.cards?.size ?: 0
    )
}

fun AiCard.toDomain(): Card {
    return Card(
        id = this.id,
        deckId = this.deckId, // Hapus elvis operator ?: "" karena field di domain non-nullable
        front = this.front,
        back = this.back
    )
}

// 🔥 TAMBAHAN: Mapper untuk AiDraftContent (Deck + List<Card>)
fun AiDraftDetailData.toDomainContent(): AiDraftContent {
    return AiDraftContent(
        deck = Deck(
            // 🔥 MASALAHNYA DISINI SEBELUMNYA: this.id bernilai null
            // ✅ SOLUSI: Gunakan ?: "" agar jika null, diganti string kosong
            id = this.id ?: "",
            name = this.name ?: "",
            description = this.description ?: "",
            // ❌ SALAH (Penyebab Crash): this.cards.size
            // ✅ BENAR: Cek dulu apakah cards null. Jika null, anggap 0.
            cardCount = this.cards?.size ?: 0,
            folderId = this.folderId,
            updatedAt = "" // Default value jika tidak ada di response AI
        ),
        // Mapping list card dari response AI ke domain Card
        // Cegah crash saat mapping list cards yang null
        cards = this.cards?.map { it.toDomain() } ?: emptyList()
    )
}

// =================================================================
// 4. QUIZ MAPPERS (DISESUAIKAN DENGAN DOMAIN MODEL BARU)
// =================================================================

// --- A. Start Quiz (Response API -> Domain) ---
fun QuizSessionDto.toDomain(): QuizSession {
    return QuizSession(
        deckId = this.deckId ?: "",
        totalQuestions = this.totalQuestions ?: 0,
        // Mapping dari List<QuizQuestionDto> ke List<QuizQuestion>
        questions = this.questions?.map { it.toDomain() } ?: emptyList()
    )
}

// Helper: Mengubah QuizQuestionDto ke Domain QuizQuestion
fun QuizQuestionDto.toDomain(): QuizQuestion {
    return QuizQuestion(
        cardId = this.cardId,
        question = this.question,
        correctAnswer = this.correctAnswer,
        options = this.options ?: emptyList(),
        explanation = this.explanation
    )
}

// --- B. Submit Result (Response API -> Domain) ---
fun QuizResultDto.toDomain(): QuizResult {
    return QuizResult(
        id = this.id,
        deckId = this.deckId,
        score = this.score,
        totalQuestions = this.totalQuestions,
        correctAnswers = this.correctAnswers,
        playedAt = this.playedAt // Menggunakan field 'playedAt' dari domain model
    )
}

// --- C. Request Mappers (Jika dibutuhkan di ViewModel/Repo) ---
// Catatan: Biasanya request mapper (Domain -> DTO) dibuat manual di RepositoryImpl
// saat memanggil API, tapi jika ingin dipisah, bisa seperti ini:

fun QuizAnswerInput.toDto(): QuizAnswerDto {
    // Logika menentukan isCorrect biasanya ada di server atau dihitung di UI
    // Tapi untuk mapping DTO standar:
    val isCorrect = this.userAnswer.equals(this.correctAnswer, ignoreCase = true)

    return QuizAnswerDto(
        cardId = this.cardId,
        userAnswer = this.userAnswer,
        correctAnswer = this.correctAnswer,
        isCorrect = isCorrect
    )
}