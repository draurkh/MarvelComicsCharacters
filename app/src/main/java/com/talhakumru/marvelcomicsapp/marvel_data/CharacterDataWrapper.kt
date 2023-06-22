package com.talhakumru.marvelcomicsapp.marvel_data

class CharacterDataWrapper {
    val data = CharacterDataContainer()
    fun append(newData : CharacterDataWrapper) {
        data.results.addAll(newData.data.results)
        /*for (item in newData.data.results) {
            data.results.add(item)
        }*/
    }
}