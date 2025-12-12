package com.mobile.memorise.domain.model.deck

import com.google.gson.annotations.SerializedName
import com.mobile.memorise.domain.model.card.CardData

// ===== Requests =====
data class CreateDeckRequest(
    val name: String,
    val description: String? = null,
    val folderId: String? = null
)

data class UpdateDeckRequest(
    val name: String? = null,
    val description: String? = null
)

data class MoveDeckRequest(
    val folderId: String? = null // null means move to unassigned
)

// ===== Responses =====
data class DeckResponse(
    val success: Boolean,
    val message: String,
    val data: DeckData?
)

data class DeckListResponse(
    val success: Boolean,
    val message: String,
    val data: List<DeckData> = emptyList()
)

data class DeckDetailResponse(
    val success: Boolean,
    val message: String,
    val data: DeckDetailData?
)

data class DeckDetailData(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String?,
    val userId: String,
    val folderId: String?,
    val isDraft: Boolean,
    val draftData: Any?,
    val settings: DeckSettings?,
    val createdAt: String,
    val updatedAt: String,
    val cards: List<CardData>? = null,
    val stats: DeckStats? = null
)

data class DeckData(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String?,
    val userId: String,
    val folderId: String?,
    val isDraft: Boolean,
    val draftData: Any?,
    val settings: DeckSettings? = null,
    val createdAt: String,
    val updatedAt: String,
    val cardsCount: Int? = null,
    @SerializedName("__v") val version: Int? = null
)

data class DeckSettings(
    val studyMode: String?,
    val difficulty: String?
)

data class DeckStats(
    val deckId: String,
    val totalCards: Int,
    val progress: DeckProgress
)

data class DeckProgress(
    val notStudied: Int,
    val learning: Int,
    val mastered: Int
)
