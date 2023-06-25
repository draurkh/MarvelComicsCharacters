package com.talhakumru.marvelcomicsapp.local_data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.talhakumru.marvelcomicsapp.local_data.tables.Character
import com.talhakumru.marvelcomicsapp.local_data.tables.Favourite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CharacterViewModel(application: Application) : AndroidViewModel(application) {
    val readAll : LiveData<List<Character>>
    val readFavourites : LiveData<List<Favourite>>
    private val repository : CharacterRepository

    init {
        val characterDao = CharacterDatabase.getDatabase(application).characterDao()
        val favouriteDao = CharacterDatabase.getDatabase(application).favouriteDao()
        repository = CharacterRepository(characterDao, favouriteDao)
        readAll = repository.readAll
        readFavourites = repository.readFavourites
    }

    // characters
    fun addCharacter(character : Character) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCharacter(character)
        }
    }

    fun removeCharacter(character : Character) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeCharacter(character)
        }
    }

    fun getCharacter(id : Int) : Character? = runBlocking  {
        withContext(Dispatchers.IO) {
            repository.getCharacter(id)
        }
    }

    // favourites

    fun addFavourite(favourite: Favourite) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFavourite(favourite)
        }
    }


    /*fun makeFavourite(id : Int, isLocal : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.makeFavourite(id, isLocal)
        }
    }

    fun removeFavourite(id : Int, isLocal : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeFavourite(id, isLocal)
        }
    }*/

    fun deleteFavourite(id : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFavourite(id)
        }
    }

    fun isFavourite(id : Int) : Favourite? = runBlocking {
        withContext(Dispatchers.IO) {
            repository.isFavourite(id)
        }
    }
}