package com.talhakumru.marvelcomicsapp.marvel_data

// CharacterDataWrapper format of incoming data from Marvel API
class CharacterDataWrapper {
    val data = CharacterDataContainer()

    fun append(newData : CharacterDataWrapper) {
        data.results.addAll(newData.data.results)
    }
}