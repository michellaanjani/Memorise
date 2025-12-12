package com.mobile.memorise.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val success: Boolean,
    val message: String,
    val data: HomeContentDto? = null
)

@Serializable
data class HomeContentDto(
    val folders: List<ApiFolderDto> = emptyList(),
    @SerialName("unassignedDecks") val unassignedDecks: List<ApiDeckDto> = emptyList()
)

@Serializable
data class ApiFolderDto(
    val id: String,
    val name: String,
    val description: String?,
    val color: String?,
    val createdAt: String, // Tambahan dari JSON baru
    val updatedAt: String,
    val decksCount: Int = 0, // Tambahan dari JSON baru (langsung Int)
    val decks: List<ApiDeckDto> = emptyList() // Sekarang list Object, bukan String ID
)

@Serializable
data class ApiDeckDto(
    val id: String,
    val name: String,
    val description: String?,
    val cardsCount: Int = 0,
    val createdAt: String,
    val updatedAt: String
)