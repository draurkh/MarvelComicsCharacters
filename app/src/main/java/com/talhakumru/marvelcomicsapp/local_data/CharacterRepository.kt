package com.talhakumru.marvelcomicsapp.local_data

import androidx.lifecycle.LiveData

class CharacterRepository(private val characterDao : CharacterDao) {

    val readAll : LiveData<List<Character>> = characterDao.readAll()

    suspend fun addCharacter(character : Character) { characterDao.addCharacter(character)}
}