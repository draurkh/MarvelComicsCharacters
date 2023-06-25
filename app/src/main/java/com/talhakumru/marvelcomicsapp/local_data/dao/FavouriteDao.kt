package com.talhakumru.marvelcomicsapp.local_data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.talhakumru.marvelcomicsapp.local_data.tables.Favourite

@Dao
interface FavouriteDao {

    /*@Query("INSERT OR REPLACE INTO favourites VALUES (:id, 1, :isLocal)")
    suspend fun makeFavourite(id : Int, isLocal : Int)

    @Query("INSERT OR REPLACE INTO favourites VALUES (:id, 0, :isLocal)")
    suspend fun removeFavourite(id : Int, isLocal : Int)

    @Query("INSERT OR REPLACE INTO favourites VALUES (:id, 1, :isFavourite)")
    suspend fun makeLocal(id : Int, isFavourite : Int)

    @Query("INSERT OR REPLACE INTO favourites VALUES (:id, 0, :isFavourite)")
    suspend fun removeLocal(id : Int, isFavourite : Int)*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavourite(fav: Favourite)

    @Query("DELETE FROM favourites WHERE id IS :id")
    suspend fun deleteFavourite(id : Int)

    @Query("SELECT * FROM favourites ORDER BY id ASC")
    fun readFavourites() : LiveData<List<Favourite>>

    @Query("SELECT * FROM favourites WHERE id IS :id")
    suspend fun isFavourite(id : Int) : Favourite?


}