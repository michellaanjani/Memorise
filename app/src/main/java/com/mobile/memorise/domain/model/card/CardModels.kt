package com.mobile.memorise.domain.model.card

import com.google.gson.annotations.SerializedName

// ===== Requests =====
data class CardRequest(
    val deckId: String,
    val front: String,
    val back: String,
    val notes: String? = null,
    val tags: List<String>? = null
)

// ===== Responses =====
data class CardResponse(
    val success: Boolean,
    val message: String,
    val data: CardData?
)

data class CardListResponse(
    val success: Boolean,
    val message: String,
    val data: List<CardData> = emptyList()
)

data class CardData(
    @SerializedName("_id") val id: String,
    val front: String,
    val back: String,
    val deckId: String,
    val userId: String,
    val imageFront: String?,
    val imageBack: String?,
    val notes: String?,
    val status: String?,
    val difficulty: String?,
    val studyData: StudyData?,
    val tags: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v") val version: Int? = null
)

data class StudyData(
    val timesStudied: Int,
    val lastStudied: String?,
    val nextReview: String?,
    val easeFactor: Double?,
    val interval: Int?
)
