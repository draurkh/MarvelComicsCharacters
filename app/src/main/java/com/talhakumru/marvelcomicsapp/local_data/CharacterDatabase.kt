package com.talhakumru.marvelcomicsapp.local_data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 1, entities = [Character::class], exportSchema = false)
abstract class CharacterDatabase : RoomDatabase() {

    abstract fun characterDao() : CharacterDao

    // only one database object can exist
    companion object {
        @Volatile
        private var INSTANCE : CharacterDatabase? = null

        fun getDatabase(context : Context) : CharacterDatabase {
            val temp = INSTANCE

            // if database exists, return that database
            if (temp != null) return temp

            // create the database
            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,CharacterDatabase::class.java,"characters").build()
                INSTANCE = instance
                return instance
            }
        }
    }
}