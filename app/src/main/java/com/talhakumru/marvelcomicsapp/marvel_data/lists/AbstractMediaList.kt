package com.talhakumru.marvelcomicsapp.marvel_data.lists

abstract class AbstractMediaList {
    val available : Int = 0
    val collectionURI : String = ""
    var elements = ArrayList<String>()
}