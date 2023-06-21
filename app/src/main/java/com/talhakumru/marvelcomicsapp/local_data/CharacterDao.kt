package com.talhakumru.marvelcomicsapp.local_data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCharacter(character: Character)

    @Delete
    suspend fun removeCharacter(character: Character)

    @Query("SELECT * FROM character_table ORDER BY name ASC")
    fun readAll() : LiveData<List<Character>>
}