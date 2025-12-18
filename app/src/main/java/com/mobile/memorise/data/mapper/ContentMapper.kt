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

// Helper khusus untuk mapping kartu yang butuh deckId dari luar
fun AiCard.toDomain(fallbackDeckId: String = ""): Card {
    return Card(
        id = this.id ?: "",
        // 🔥 LOGIKA: Jika deckId di item kartu ada, pakai itu.
        // Jika tidak ada (null), pakai fallbackDeckId yang dilempar dari parent.
        deckId = this.deckId ?: fallbackDeckId,
        front = this.front ?: "",
        back = this.back ?: ""
    )
}
// 🔥 TAMBAHAN: Mapper untuk AiDraftContent (Deck + List<Card>)
fun AiDraftDetailData.toDomainContent(): AiDraftContent {
    // Ambil ID deck utama (Parent ID)
    val parentDeckId = this.id ?: ""

    return AiDraftContent(
        deck = Deck(
            id = parentDeckId,
            name = this.name ?: "",
            description = this.description ?: "",
            cardCount = this.cards?.size ?: 0,
            folderId = this.folderId,
            updatedAt = ""
        ),
        // 🔥 PENTING: Kita kirim 'parentDeckId' ke fungsi toDomain()
        // agar setiap kartu punya referensi ke Deck ID-nya.
        cards = this.cards?.map { it.toDomain(fallbackDeckId = parentDeckId) } ?: emptyList()
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

// --- B. Result / History Mapper (Response API -> Domain) ---
fun QuizResultDto.toDomain(): QuizResult {
    return QuizResult(
        id = this.id ?: "", // Handle null safety jika perlu

        // ✅ FIX: Ambil langsung string-nya
        deckId = this.deckId,

        // ⚠️ MASALAH: Server tidak mengirim nama deck di response submit.
        // Solusi: Isi dengan string kosong atau placeholder sementara.
        // Nanti di UI, jika deckName kosong, kamu mungkin perlu fetch detail deck terpisah
        // atau pass nama deck dari screen sebelumnya.
        deckName = "",

        score = this.score,
        totalQuestions = this.totalQuestions,
        correctAnswers = this.correctAnswers,

        // Gunakan current time jika playedAt null (karena kadang server response beda format)
        playedAt = this.playedAt ?: java.time.Instant.now().toString(),

        details = this.details?.map { it.toDomain() } ?: emptyList()
    )
}

// Helper: Ubah item detail DTO ke Domain
fun QuizDetailItemDto.toDomain(): QuizHistoryDetail {
    return QuizHistoryDetail(
        // ✅ Ambil dari String langsung
        cardId = this.cardId,

        // ⚠️ Karena server response submit TIDAK mengirim teks soal (front),
        // kita tidak bisa menampilkan soal aslinya di sini.
        // Kita gunakan placeholder atau format ID.
        question = "Question #${this.cardId.takeLast(4)}",

        // Gunakan correctAnswer sebagai referensi jawaban yang benar
        answer = this.correctAnswer,

        isCorrect = this.isCorrect,
        userAnswer = this.userAnswer,
        correctAnswer = this.correctAnswer,
        explanation = this.explanation
    )
}

// =================================================================
// TAMBAHAN: QUIZ DETAIL MAPPER (DTO -> DOMAIN)
// =================================================================

// 1. Mapping dari Response Detail Utama (QuizDetailDto) ke Domain (QuizResult)
fun QuizDetailDto.toDomain(): QuizResult {
    return QuizResult(
        id = this.id,

        // 🔥 PERBAIKAN PENTING:
        // Di endpoint Detail, deck dikirim sebagai Object (DeckSummaryDto), bukan String.
        // Kita harus ambil properti .id dan .name dari object tersebut.
        deckId = this.deck.id,
        deckName = this.deck.name,

        score = this.score,
        totalQuestions = this.totalQuestions,
        correctAnswers = this.correctAnswers,
        playedAt = this.playedAt ?: java.time.Instant.now().toString(),

        // Map list detail item (Expanded)
        details = this.details?.map { it.toDomain() } ?: emptyList()
    )
}

// 2. Mapping dari Item Detail Expanded (QuizDetailItemExpandedDto) ke Domain (QuizHistoryDetail)
fun QuizDetailItemExpandedDto.toDomain(): QuizHistoryDetail {
    return QuizHistoryDetail(
        // 🔥 PERBAIKAN PENTING:
        // Di endpoint Detail, card dikirim sebagai Object (CardSummaryDto).
        // Kita bisa mengambil ID dan TEKS SOAL ASLI (front).

        cardId = this.card.id,

        // ✅ KUNCI: Karena ini detail history, server mengirimkan isi kartu.
        // Jadi kita bisa menampilkan pertanyaan asli, bukan placeholder.
        question = this.card.front,

        answer = this.correctAnswer, // Jawaban yang benar
        userAnswer = this.userAnswer,
        correctAnswer = this.correctAnswer,
        isCorrect = this.isCorrect,
        explanation = this.explanation
    )
}
// Tambahkan di bagian Quiz Mappers

fun QuizHistoryDto.toDomain(): QuizResult {
    return QuizResult(
        id = this.id,

        // 🔥 Ambil ID dan Name dari Object deck
        deckId = this.deck.id,
        deckName = this.deck.name,

        score = this.score,
        totalQuestions = this.totalQuestions,
        correctAnswers = this.correctAnswers,
        playedAt = this.playedAt ?: java.time.Instant.now().toString(),

        // History list tidak perlu detail jawaban per soal
        details = emptyList()
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