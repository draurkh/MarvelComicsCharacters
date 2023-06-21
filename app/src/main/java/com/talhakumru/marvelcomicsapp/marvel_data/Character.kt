package com.talhakumru.marvelcomicsapp.marvel_data

import com.talhakumru.marvelcomicsapp.marvel_data.lists.ComicList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.EventList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.SeriesList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.StoryList

class Character {
    var id : Int = 0
    var name : String = ""
    var thumbnail = Image()
    val comics = ComicList()
    val stories = StoryList()
    val events = EventList()
    val series = SeriesList()
    var isFavourite = false
    var isLocal = false
}