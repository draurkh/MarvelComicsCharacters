package com.talhakumru.marvelcomicsapp.local_data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.talhakumru.marvelcomicsapp.local_data.tables.Character

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCharacter(character: Character)

    @Delete
    suspend fun removeCharacter(character: Character)

    @Query("SELECT * FROM characters ORDER BY name ASC")
    fun readAll() : LiveData<List<Character>>

    @Query("SELECT * FROM characters WHERE id IS :id")
    suspend fun getCharacter(id : Int) : Character?
}