package com.talhakumru.marvelcomicsapp.local_data.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class Favourite(
    @PrimaryKey
    val id : Int,

    @ColumnInfo(name = "is_favourite", defaultValue = "0")
    var isFavourite: Int,
    @ColumnInfo(name = "is_local", defaultValue = "0")
    var isLocal : Int
)
