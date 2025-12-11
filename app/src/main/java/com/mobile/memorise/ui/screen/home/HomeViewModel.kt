package com.mobile.memorise.ui.screen.home

import android.util.Log // Tambahkan import ini
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.data.local.token.TokenStore
import com.mobile.memorise.data.remote.api.HomeApi
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: HomeApi,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _homeState = MutableStateFlow<Resource<HomeData>>(Resource.Loading())
    val homeState: StateFlow<Resource<HomeData>> = _homeState.asStateFlow()

    init {
        getHomeData()
    }

    fun getHomeData() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "1. Memulai getHomeData...")
            _homeState.value = Resource.Loading()

            try {
                // Ambil Token
                val token = tokenStore.accessToken.first()
                Log.d("HomeViewModel", "2. Token didapat: $token")

                if (token.isNullOrBlank()) {
                    Log.e("HomeViewModel", "X. Token Kosong!")
                    _homeState.value = Resource.Error("Unauthorized: No token found")
                    return@launch
                }

                // Panggil API
                Log.d("HomeViewModel", "3. Memanggil API ke /home...")
                val response = api.getHomeData()

                Log.d("HomeViewModel", "4. Response Code: ${response.code()}")

                val body = response.body()

                if (response.isSuccessful && body != null && body.success && body.data != null) {
                    Log.d("HomeViewModel", "5. Sukses! Data diterima.")
                    Log.d("HomeViewModel", "   - Jumlah Folder: ${body.data.folders.size}")
                    Log.d("HomeViewModel", "   - Jumlah Deck: ${body.data.unassignedDecks.size}")

                    val folders = body.data.folders.map { dto ->
                        FolderItemData(
                            id = dto.id,
                            name = dto.name,
                            date = formatDate(dto.createdAt),
                            deckCount = dto.decksCount,
                            serverColor = dto.color
                        )
                    }

                    val decks = body.data.unassignedDecks.map { dto ->
                        DeckItemData(
                            id = dto.id,
                            deckName = dto.name,
                            cardCount = dto.cardsCount
                        )
                    }

                    _homeState.value = Resource.Success(HomeData(folders, decks))
                } else {
                    val errorMsg = body?.message ?: "Gagal load data. Code: ${response.code()}"
                    Log.e("HomeViewModel", "X. API Error: $errorMsg")
                    _homeState.value = Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "X. Exception: ${e.message}")
                e.printStackTrace()
                _homeState.value = Resource.Error("Error: ${e.localizedMessage}")
            }
        }
    }

    // ... (fungsi formatDate tetap sama)
    private fun formatDate(isoString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoString)
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: isoString
        } catch (e: Exception) {
            isoString
        }
    }
}