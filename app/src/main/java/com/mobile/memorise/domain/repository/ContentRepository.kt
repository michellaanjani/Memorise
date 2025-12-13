package com.mobile.memorise.domain.repository

import com.mobile.memorise.domain.model.*
import com.mobile.memorise.domain.model.quiz.* // Pastikan import ini ada
import java.io.File

interface ContentRepository {

    // =================================================================
    // HOME & DASHBOARD
    // =================================================================
    suspend fun getHomeData(): Result<Pair<List<Folder>, List<Deck>>>

    // =================================================================
    // FOLDER MODULE
    // =================================================================
    suspend fun getAllFolders(): Result<List<Folder>>
    suspend fun createFolder(name: String, desc: String, color: String): Result<Folder>
    suspend fun updateFolder(id: String, name: String, desc: String, color: String): Result<Folder>
    suspend fun deleteFolder(id: String): Result<Unit>

    // =================================================================
    // DECK MODULE
    // =================================================================
    suspend fun getDecks(folderId: String?): Result<List<Deck>>
    suspend fun createDeck(name: String, desc: String, folderId: String?): Result<Deck>
    suspend fun updateDeck(id: String, name: String, desc: String, folderId: String?): Result<Deck>
    suspend fun moveDeck(deckId: String, folderId: String?): Result<Deck>
    suspend fun moveDeckToHome(deckId: String): Result<Deck>
    suspend fun deleteDeck(id: String): Result<Unit>

    // =================================================================
    // CARD MODULE
    // =================================================================
    suspend fun getCardsByDeckId(deckId: String): Result<List<Card>>
    suspend fun createCard(deckId: String, front: String, back: String): Result<Card>
    suspend fun updateCard(id: String, front: String, back: String): Result<Card>
    suspend fun deleteCard(id: String): Result<Unit>

    // =================================================================
    // AI GENERATOR MODULE
    // =================================================================
    suspend fun generateFlashcards(prompt: String, fileId: String? = null): Result<AiGeneratedContent>

    suspend fun getAiDraft(deckId: String): Result<Deck>

    suspend fun updateDraftCard(deckId: String, cardId: String, front: String, back: String): Result<Card>

    suspend fun deleteDraftCard(deckId: String, cardId: String): Result<Unit>

    suspend fun saveAiDraft(deckId: String, destinationFolderId: String?): Result<Deck>

    // =================================================================
    // QUIZ SYSTEM (DIPERBARUI SESUAI MAPPER & DTO BARU)
    // =================================================================

    // Return tipe berubah dari QuizSession -> QuizStartData
    suspend fun startQuiz(deckId: String): Result<QuizStartData>

    /**
     * Menggunakan Request Object lengkap (termasuk score & totalQuestions)
     */
    suspend fun submitQuiz(request: QuizSubmitRequest): Result<QuizSubmitData>

    // Menggunakan QuizSubmitData karena itu hasil mapping dari QuizResultDto
    suspend fun getQuizHistory(): Result<List<QuizSubmitData>>

    suspend fun getQuizDetail(quizId: String): Result<QuizSubmitData>

    // =================================================================
    // FILE UPLOAD MODULE
    // =================================================================
    suspend fun uploadFile(file: File): Result<UploadedFile>

    suspend fun getFileUrl(id: String): Result<String>

    suspend fun deleteFile(id: String): Result<Unit>
}