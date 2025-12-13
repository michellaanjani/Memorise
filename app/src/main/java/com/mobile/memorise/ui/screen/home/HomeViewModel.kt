package com.mobile.memorise.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.repository.ContentRepository
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ContentRepository
) : ViewModel() {

    // State UI
    private val _homeState = MutableStateFlow<Resource<HomeData>>(Resource.Loading())
    val homeState: StateFlow<Resource<HomeData>> = _homeState.asStateFlow()

    init {
        getHomeData()
    }

    fun getHomeData() {
        viewModelScope.launch {
            _homeState.value = Resource.Loading()

            // Panggil Repository
            val result = repository.getHomeData()

            result.onSuccess { (folders, decks) ->
                Log.d("HomeViewModel", "Success: ${folders.size} folders, ${decks.size} decks")

                // 1. MAP FOLDERS
                // UI Folder menampilkan tanggal jadi (String formatted), jadi kita format di sini
                val folderItems = folders.map { folder ->
                    FolderItemData(
                        id = folder.id,
                        name = folder.name,
                        date = formatDate(folder.createdAt),
                        deckCount = folder.deckCount,
                        serverColor = folder.color
                    )
                }

                // 2. MAP DECKS
                // UI Deck menggunakan helper 'formatHomeDeckDate' yang butuh ISO String
                // Jadi kita kirim raw string 'updatedAt' dari API
                val deckItems = decks.map { deck ->
                    DeckItemData(
                        id = deck.id,
                        deckName = deck.name,
                        cardCount = deck.cardCount,
                        updatedAt = deck.updatedAt // 🔥 UPDATE: Kirim Raw ISO String
                    )
                }

                _homeState.value = Resource.Success(HomeData(folderItems, deckItems))

            }.onFailure { exception ->
                Log.e("HomeViewModel", "Error: ${exception.message}")
                val errorMessage = exception.message ?: "Terjadi kesalahan yang tidak diketahui"
                _homeState.value = Resource.Error(errorMessage)
            }
        }
    }

    // Utility untuk formatting date (Khusus Folder)
    private fun formatDate(isoString: String): String {
        if (isoString.isBlank()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoString)
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: isoString
        } catch (e: Exception) {
            isoString // Kembalikan string asli jika gagal parse
        }
    }
}