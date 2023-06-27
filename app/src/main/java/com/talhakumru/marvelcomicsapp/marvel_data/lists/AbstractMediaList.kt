package com.talhakumru.marvelcomicsapp.marvel_data.lists

// MediaLists data from Marvel API are in the same format with different names
// ComicList, EventList, SeriesList, StoryList
abstract class AbstractMediaList {
    val available : Int = 0
    val collectionURI : String = ""
    var elements = ArrayList<String>()
}