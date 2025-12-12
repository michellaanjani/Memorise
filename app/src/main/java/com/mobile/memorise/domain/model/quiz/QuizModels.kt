package com.mobile.memorise.domain.model.quiz

import com.google.gson.annotations.SerializedName

data class QuizStartResponse(
    val success: Boolean,
    val message: String,
    val data: QuizStartData?
)

data class QuizStartData(
    val deckId: String,
    val totalQuestions: Int,
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val cardId: String,
    val question: String,
    val correctAnswer: String,
    val options: List<String>,
    val explanation: String?
)

data class QuizSubmitRequest(
    val deckId: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val details: List<QuizAnswerDetail>
)

data class QuizAnswerDetail(
    val cardId: String,
    val isCorrect: Boolean,
    val userAnswer: String,
    val correctAnswer: String,
    val explanation: String?
)

data class QuizSubmitResponse(
    val success: Boolean,
    val message: String,
    val data: QuizSubmitData?
)

data class QuizSubmitData(
    val userId: String,
    val deckId: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Int,
    val details: List<QuizAnswerDetail>,
    @SerializedName("_id") val id: String,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v") val version: Int?
)

