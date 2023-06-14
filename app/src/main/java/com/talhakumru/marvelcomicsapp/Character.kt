package com.talhakumru.marvelcomicsapp

import java.util.ArrayList

class Character(
    private val name : String,
    private val numOfSeries : Int,
) {
    private val series = ArrayList<String>()
    private val stories = ArrayList<String>()
    private val events = ArrayList<String>()
    private val comics = ArrayList<String>()
}