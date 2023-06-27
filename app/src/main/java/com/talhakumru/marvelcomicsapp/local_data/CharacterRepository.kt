package com.talhakumru.marvelcomicsapp.local_data

import androidx.lifecycle.LiveData
import com.talhakumru.marvelcomicsapp.local_data.dao.CharacterDao
import com.talhakumru.marvelcomicsapp.local_data.dao.FavouriteDao
import com.talhakumru.marvelcomicsapp.local_data.tables.Character
import com.talhakumru.marvelcomicsapp.local_data.tables.Favourite

class CharacterRepository(private val characterDao : CharacterDao, private val favouriteDao: FavouriteDao) {

    val readAll : LiveData<List<Character>> = characterDao.readAll()
    val readFavourites : LiveData<List<Favourite>> = favouriteDao.readFavourites()

    // characters methods
    suspend fun addCharacter(character : Character) { characterDao.addCharacter(character)}

    suspend fun removeCharacter(character : Character) { characterDao.removeCharacter(character)}

    suspend fun getCharacter(id : Int) : Character? { return characterDao.getCharacter(id) }

    // favourites methods

    suspend fun addFavourite(fav: Favourite) { favouriteDao.addFavourite(fav) }

    suspend fun deleteFavourite(id : Int) { favouriteDao.deleteFavourite(id) }

    suspend fun isFavourite(id : Int) : Favourite? { return favouriteDao.isFavourite(id) }
}