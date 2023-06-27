package com.talhakumru.marvelcomicsapp.local_data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.talhakumru.marvelcomicsapp.local_data.tables.Favourite

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavourite(fav: Favourite)

    @Query("DELETE FROM favourites WHERE id IS :id")
    suspend fun deleteFavourite(id : Int)

    @Query("SELECT * FROM favourites ORDER BY id ASC")
    fun readFavourites() : LiveData<List<Favourite>>

    @Query("SELECT * FROM favourites WHERE id IS :id")
    suspend fun isFavourite(id : Int) : Favourite?


}