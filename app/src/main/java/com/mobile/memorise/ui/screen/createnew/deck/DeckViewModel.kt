package com.mobile.memorise.ui.screen.createnew.deck

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

    fun updateDeck(oldName: String, newName: String) {
        val index = deckList.indexOfFirst { it.name.equals(oldName, true) }
        if (index != -1) {
            deckList[index] = DeckItem(newName)
        }
    }

    // ✔ FIXED — delete pakai `name`, sesuai HomeScreen
    fun deleteDeck(name: String) {
        deckList.removeAll { it.name == name }
        println("Deleted deck: $name")
    }
}
