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
    suspend fun generateFlashcards(prompt: String, fileId: String? = null, cardAmount: Int ): Result<AiGeneratedContent>

    // 🔥 PERBAIKAN: Mengembalikan AiDraftContent (Deck + Cards)
    // agar ViewModel bisa langsung update UI tanpa request ulang
    suspend fun getAiDraft(deckId: String): Result<AiDraftContent>

    suspend fun updateDraftCard(deckId: String, cardId: String, front: String, back: String): Result<AiDraftContent>

    suspend fun deleteDraftCard(deckId: String, cardId: String): Result<AiDraftContent>

    // Save biasanya mengembalikan Deck final yang sudah tersimpan
    suspend fun saveAiDraft(deckId: String, destinationFolderId: String?, name: String?): Result<Deck>

    // =================================================================
    // QUIZ SYSTEM (DIPERBARUI SESUAI DOMAIN MODEL BARU)
    // =================================================================

    // Menggunakan QuizSession (bukan QuizStartData)
    suspend fun startQuiz(deckId: String): Result<QuizSession>

    /**
     * Submit Quiz sekarang menerima parameter spesifik karena kita tidak
     * mendefinisikan 'QuizSubmitRequest' di file Domain Model.
     * Kita menggunakan List<QuizAnswerInput> untuk detail jawaban.
     */
    suspend fun submitQuiz(
        deckId: String,
        totalQuestions: Int,
        correctAnswers: Int,
        answers: List<QuizAnswerInput>
    ): Result<QuizResult>

    // Menggunakan QuizResult (bukan QuizSubmitData)
    suspend fun getQuizHistory(): Result<List<QuizResult>>

    suspend fun getQuizDetail(quizId: String): Result<QuizResult>

    // =================================================================
    // FILE UPLOAD MODULE
    // =================================================================
    suspend fun uploadFile(file: File): Result<UploadedFile>

    suspend fun getFileUrl(id: String): Result<String>

    suspend fun deleteFile(id: String): Result<Unit>
}

