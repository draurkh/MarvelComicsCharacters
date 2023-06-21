package com.talhakumru.marvelcomicsapp.local_data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CharacterViewModel(application: Application) : AndroidViewModel(application) {
    val readAll : LiveData<List<Character>>
    private val repository : CharacterRepository

    init {
        val characterDao = CharacterDatabase.getDatabase(application).characterDao()
        repository = CharacterRepository(characterDao)
        readAll = repository.readAll
    }

    fun addCharacter(character : Character) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCharacter(character)
        }
    }
}