package com.talhakumru.marvelcomicsapp.local_data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.talhakumru.marvelcomicsapp.marvel_data.lists.ComicList

@Entity(tableName = "character_table")
data class Character(
    @PrimaryKey
    val id : Int,

    val name : String,
    @ColumnInfo(name = "is_favourite")
    val isFavourite : Int,
    @ColumnInfo(name = "image_url")
    val imageURL : String,
    @ColumnInfo(name = "series_json")
    val seriesJson : String,
    @ColumnInfo(name = "stories_json")
    val storiesJson : String,
    @ColumnInfo(name = "events_json")
    val eventsJson : String,
    @ColumnInfo(name = "comics_json")
    val comicsJson : String
)