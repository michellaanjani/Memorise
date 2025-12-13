package com.mobile.memorise.domain.repository

import com.mobile.memorise.domain.model.*
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
    suspend fun updateFolder(id: String, name: String, desc: String,color: String): Result<Folder>
    suspend fun deleteFolder(id: String): Result<Unit>

    // =================================================================
    // DECK MODULE
    // =================================================================
    suspend fun getDecks(folderId: String?): Result<List<Deck>>
    suspend fun createDeck(name: String, desc: String, folderId: String?): Result<Deck>
    // PERBAIKAN: Tambahkan parameter folderId: String?
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
    /**
     * @param prompt Topik atau teks instruksi
     * @param fileId ID file yang sudah diupload (opsional)
     */
    suspend fun generateFlashcards(prompt: String, fileId: String? = null): Result<AiGeneratedContent>

    suspend fun getAiDraft(deckId: String): Result<Deck>

    suspend fun updateDraftCard(deckId: String, cardId: String, front: String, back: String): Result<Card>

    suspend fun deleteDraftCard(deckId: String, cardId: String): Result<Unit>

    suspend fun saveAiDraft(deckId: String, destinationFolderId: String?): Result<Deck>

    // =================================================================
    // QUIZ SYSTEM
    // =================================================================
    suspend fun startQuiz(deckId: String): Result<QuizSession>

    /**
     * @param quizId ID sesi kuis
     * @param answers Map berisi cardId dan jawaban user (misal: "easy", "hard", atau boolean score)
     */
    suspend fun submitQuiz(quizId: String, answers: List<QuizAnswerInput>): Result<QuizResult>

    suspend fun getQuizHistory(): Result<List<QuizResult>>

    suspend fun getQuizDetail(quizId: String): Result<QuizResult>

    // =================================================================
    // FILE UPLOAD MODULE
    // =================================================================
    /**
     * Repository implementation akan mengubah File menjadi MultipartBody.Part
     */
    suspend fun uploadFile(file: File): Result<UploadedFile>

    suspend fun getFileUrl(id: String): Result<String>

    suspend fun deleteFile(id: String): Result<Unit>
}