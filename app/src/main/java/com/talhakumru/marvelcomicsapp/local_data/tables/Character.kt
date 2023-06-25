package com.talhakumru.marvelcomicsapp.local_data.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.talhakumru.marvelcomicsapp.marvel_data.Image
import com.talhakumru.marvelcomicsapp.marvel_data.lists.ComicList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.EventList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.SeriesList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.StoryList

@Entity(tableName = "characters", ignoredColumns = ["thumbnail", "comics", "stories", "events", "series"])
data class Character (
    @PrimaryKey
    val id : Int,

    val name : String,
    @ColumnInfo(name = "available_series")
    var numOfSeries : Int,
    @ColumnInfo(name = "image_url")
    var imageURL : String,
    @ColumnInfo(name = "series_json")
    var seriesJson : String,
    @ColumnInfo(name = "stories_json")
    var storiesJson : String,
    @ColumnInfo(name = "events_json")
    var eventsJson : String,
    @ColumnInfo(name = "comics_json")
    var comicsJson : String
    ) {

    // only Marvel data
    val thumbnail: Image = Image()
    var comics = ComicList()
    var stories = StoryList()
    var events = EventList()
    var series = SeriesList()

}