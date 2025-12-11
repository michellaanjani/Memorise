package com.mobile.memorise.data.repository

import com.mobile.memorise.data.remote.api.HomeApi
import com.mobile.memorise.domain.repository.HomeRepository
import com.mobile.memorise.ui.screen.home.DeckItemData
import com.mobile.memorise.ui.screen.home.FolderItemData
import com.mobile.memorise.ui.screen.home.HomeData
import com.mobile.memorise.data.local.token.TokenStore
import com.mobile.memorise.util.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: HomeApi,
    private val tokenStore: TokenStore
) : HomeRepository {

    override fun getHomeData(): Flow<Resource<HomeData>> = flow {
        emit(Resource.Loading())
        try {
            //val token = tokenStore.accessToken.first()
            val response = api.getHomeData(
                //token = "Bearer $token" // Ganti dengan token yang sesuai
            )
            val body = response.body()

            if (response.isSuccessful && body != null && body.success && body.data != null) {

                // MAPPING DTO (Data Server) KE UI MODEL
                val uiFolders = body.data.folders.map { dto ->
                    FolderItemData(
                        id = dto.id,
                        name = dto.name,
                        date = "Recently", // API tidak kasih tanggal folder, pakai placeholder/logic lain
                        deckCount = dto.decks.size,
                        // Kita bisa simpan hex color dari API di sini kalau mau dipakai nanti
                        // Tapi FolderItemData kamu saat ini belum punya field color,
                        // nanti kita akali di UI atau tambahkan field color di FolderItemData
                    )
                }

                val uiDecks = body.data.unassignedDecks.map { dto ->
                    DeckItemData(
                        id = dto.id,
                        deckName = dto.name,
                        cardCount = dto.cardsCount
                    )
                }

                val homeData = HomeData(
                    folders = uiFolders,
                    decks = uiDecks
                )

                emit(Resource.Success(homeData))
            } else {
                emit(Resource.Error(body?.message ?: "Failed to load home data"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Connection error"))
        }
    }
}