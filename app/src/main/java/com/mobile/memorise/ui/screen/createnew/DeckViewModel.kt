package com.mobile.memorise.ui.screen.createnew

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class DeckItem(
    val name: String
)

class DeckViewModel : ViewModel() {

    var deckList = mutableStateListOf<DeckItem>()
        private set

    fun addDeck(name: String) {
        deckList.add(DeckItem(name))
    }
}
