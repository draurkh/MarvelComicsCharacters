package com.talhakumru.marvelcomicsapp.local_data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Character(
    @PrimaryKey
    val id : Int,

    val name : String,
    @ColumnInfo(name = "image_url")
    val imageURL : String,
    val series : String,
    val stories : String,
    val events : String,
    val comics : String,
)