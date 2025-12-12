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

            // Panggil Repository (Result pattern sudah di-handle di Repo)
            val result = repository.getHomeData()

            result.onSuccess { (folders, decks) ->
                Log.d("HomeViewModel", "Success: ${folders.size} folders, ${decks.size} decks")

                // Map Domain Model (Folder/Deck) ke UI Model (ItemData)
                val folderItems = folders.map { folder ->
                    FolderItemData(
                        id = folder.id,
                        name = folder.name,
                        date = formatDate(folder.createdAt),
                        deckCount = folder.deckCount,
                        serverColor = folder.color
                    )
                }

                val deckItems = decks.map { deck ->
                    DeckItemData(
                        id = deck.id,
                        deckName = deck.name,
                        cardCount = deck.cardCount
                    )
                }

                _homeState.value = Resource.Success(HomeData(folderItems, deckItems))

            }.onFailure { exception ->
                Log.e("HomeViewModel", "Error: ${exception.message}")
                // Pesan error ramah pengguna
                val errorMessage = exception.message ?: "Terjadi kesalahan yang tidak diketahui"
                _homeState.value = Resource.Error(errorMessage)
            }
        }
    }

    // Utility untuk formatting date (UI helper)
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