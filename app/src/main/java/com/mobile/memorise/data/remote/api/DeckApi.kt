package com.mobile.memorise.data.remote.api

import com.mobile.memorise.domain.model.card.CardListResponse
import com.mobile.memorise.domain.model.card.CardRequest
import com.mobile.memorise.domain.model.card.CardResponse
import com.mobile.memorise.domain.model.deck.CreateDeckRequest
import com.mobile.memorise.domain.model.deck.DeckDetailResponse
import com.mobile.memorise.domain.model.deck.DeckListResponse
import com.mobile.memorise.domain.model.deck.DeckResponse
import com.mobile.memorise.domain.model.deck.MoveDeckRequest
import com.mobile.memorise.domain.model.deck.UpdateDeckRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Deck & Card related endpoints.
 * All endpoints already authenticated by the global AuthInterceptor.
 */
interface DeckApi {
    // Decks
    @POST("decks")
    suspend fun createDeck(@Body request: CreateDeckRequest): Response<DeckResponse>

    @GET("decks")
    suspend fun getDecks(
        @Query("folderId") folderId: String? = null,
        @Query("unassigned") unassigned: Boolean? = null
    ): Response<DeckListResponse>

    @GET("decks/{id}")
    suspend fun getDeckDetail(
        @Path("id") id: String,
        @Query("include") include: String? = null // e.g. "cards,stats"
    ): Response<DeckDetailResponse>

    @PATCH("decks/{id}")
    suspend fun updateDeck(
        @Path("id") id: String,
        @Body request: UpdateDeckRequest
    ): Response<DeckResponse>

    @PATCH("decks/{id}/move")
    suspend fun moveDeck(
        @Path("id") id: String,
        @Body request: MoveDeckRequest
    ): Response<DeckResponse>

    @DELETE("decks/{id}")
    suspend fun deleteDeck(@Path("id") id: String): Response<DeckResponse>
}

interface CardApi {
    @POST("cards")
    suspend fun createCard(@Body request: CardRequest): Response<CardResponse>

    @GET("cards/deck/{deckId}")
    suspend fun getCardsByDeck(@Path("deckId") deckId: String): Response<CardListResponse>

    @GET("cards/{id}")
    suspend fun getCard(@Path("id") id: String): Response<CardResponse>

    @PATCH("cards/{id}")
    suspend fun updateCard(
        @Path("id") id: String,
        @Body request: CardRequest
    ): Response<CardResponse>

    @DELETE("cards/{id}")
    suspend fun deleteCard(@Path("id") id: String): Response<CardResponse>
}

