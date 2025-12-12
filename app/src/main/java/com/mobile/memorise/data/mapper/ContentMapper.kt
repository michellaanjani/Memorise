package com.mobile.memorise.data.mapper

import com.mobile.memorise.data.remote.dto.auth.AuthDataDto
import com.mobile.memorise.data.remote.dto.auth.UserDto
import com.mobile.memorise.data.remote.dto.content.*
import com.mobile.memorise.domain.model.*

// =================================================================
// 1. CONTENT MAPPER (Deck, Folder, Card)
// =================================================================

fun DeckDto.toDomain(): Deck {
    return Deck(
        // Gunakan ?: "" untuk mencegah crash jika ID dari API null/beda nama
        id = this.id ?: "",
        folderId = this.folderId, // folderId boleh null di domain
        name = this.name ?: "Unknown Deck",
        description = this.description ?: "",
        cardCount = this.cardCount
    )
}

fun FolderDto.toDomain(): Folder {
    return Folder(
        id = this.id ?: "",
        name = this.name ?: "Unknown Folder",
        description = this.description ?: "",
        color = this.color ?: "#FFFFFF", // Default putih jika warna null
        deckCount = this.decksCount,
        createdAt = this.createdAt ?: ""
    )
}

fun CardDto.toDomain(): Card {
    return Card(
        id = this.id ?: "",
        deckId = this.deckId ?: "",
        front = this.front ?: "",
        back = this.back ?: ""
    )
}

// =================================================================
// 2. AUTH & USER MAPPER
// =================================================================

fun UserDto.toDomain(): User {
    return User(
        id = this.id ?: "",
        email = this.email ?: "",
        firstName = this.firstName ?: "",
        lastName = this.lastName ?: "",
        isEmailVerified = this.isEmailVerified ?: false,
        avatar = this.profile?.avatar,
        bio = this.profile?.bio
    )
}

fun AuthDataDto.toDomainToken(): AuthToken? {
    // Pastikan tokens tidak null
    return tokens?.let {
        AuthToken(
            accessToken = it.accessToken ?: "",
            refreshToken = it.refreshToken ?: ""
        )
    }
}

// =================================================================
// 3. QUIZ MAPPER
// =================================================================

fun QuizStartResponseDto.toDomain(): QuizSession {
    return QuizSession(
        quizId = this.quizId ?: "",
        // Mapping list aman: jika list null, kembalikan list kosong
        cards = this.cards?.map { it.toDomain() } ?: emptyList()
    )
}

fun QuizResultDto.toDomain(): QuizResult {
    return QuizResult(
        id = this.id ?: "",
        deckId = this.deckId ?: "",
        score = this.score,
        totalQuestions = this.totalQuestions,
        correctAnswers = this.correctAnswers,
        playedAt = this.playedAt ?: ""
    )
}

// =================================================================
// 4. FILE & AI MAPPER
// =================================================================

fun FileUploadResponseDto.toDomain(): UploadedFile {
    return UploadedFile(
        id = this.id ?: "",
        url = this.url ?: "",
        originalName = this.originalName ?: "unknown_file"
    )
}

fun AiGeneratedResponseDto.toDomain(): AiGeneratedContent {
    return AiGeneratedContent(
        deckId = this.deckId ?: "",
        summary = this.summary ?: "No summary available",
        cardCount = this.cardCount
    )
}