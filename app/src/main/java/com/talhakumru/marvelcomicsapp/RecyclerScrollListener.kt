package com.talhakumru.marvelcomicsapp

import android.util.DisplayMetrics
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class RecyclerScrollListener(private val controller : MarvelAPIController, private val display : DisplayMetrics) : RecyclerView.OnScrollListener() {

    // list=0, grid=1
    private var mode = 0

    private var listSize : Int = MarvelAPIController.listSize

    private val listCardHeight = display.density * (150 + 6)
    private val gridCardHeight = display.density * (260 + 6)
    private val gridCardWidth = display.density * (125 + 6)
    private val numOfGridColumns = display.widthPixels / gridCardWidth

    private val listCardPerPage = display.heightPixels / listCardHeight
    //private val gridCardPerPage = (display.heightPixels / gridCardHeight) * numOfGridColumns

    // first fetched data must fit to 4 screens to enable scrolling
    private val numOfFirstFetch = 4 * (listCardPerPage).roundToInt()

    // how many times a 100 character fetch occurred
    private val timesFirstFetch = numOfFirstFetch / 100 + 1

    // additional filter parameter to pass with controller.getData
    private var addFilter : String = ""

    // viewLimit: 60% of vertical position of loaded data
    private var listViewLimit : Double = 60.0 * listCardHeight * timesFirstFetch
    private var gridViewLimit : Double = 60.0 * gridCardHeight * timesFirstFetch / numOfGridColumns
    private var viewLimit = listViewLimit
    // a counter of how many times the limit is increased
    private var limitInc = 1

    // pos denotes which position we are in recyclerView,
    // starts from the bottom of the screen
    private var listPos = display.heightPixels
    private var gridPos = display.heightPixels
    private var pos = listPos

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (mode == 0) {
            // list view
            listPos += dy
            pos = listPos
        }
        else{
            // grid view
            gridPos += dy
            pos = gridPos
        }
        // println("vPos: ${pos}")
        if (pos > viewLimit && listSize != MarvelAPIController.dataWrapper.data.results.size) {
            // when the pixels scrolled amount (pos) has reached viewLimit,
            // and the list isn't currently updating itself
            // then fetch new data and increase viewLimit

            // println("$viewLimit limit is exceeded with $pos")
            if (mode == 0) {
                // list view
                listViewLimit = 60.0 * timesFirstFetch * ++limitInc * listCardHeight
                viewLimit = listViewLimit
            }
            else if (mode == 1) {
                // grid view
                gridViewLimit = 60.0 * timesFirstFetch * ++limitInc * gridCardHeight / numOfGridColumns
                viewLimit = gridViewLimit
            }
            // println("View limit: $viewLimit")
            val filter = "?${addFilter}offset=${listSize+100}&limit=${100}&"
            controller.getData(filter, numOfFirstFetch)
            listSize = MarvelAPIController.dataWrapper.data.results.size
            // println("the list has ${MarvelAPIController.dataWrapper.data.results.size}=${listSize} elements")
        }
    }

    fun switchLayout() {
        when (mode) {
            0 -> {
                // switch from list to grid
                gridPos = (listPos * gridCardHeight / listCardHeight / numOfGridColumns).roundToInt()
                if (gridPos < display.heightPixels) gridPos = display.heightPixels
                gridViewLimit = 60.0 * timesFirstFetch * limitInc * gridCardHeight / numOfGridColumns
                viewLimit = gridViewLimit
                mode = 1
            }
            else -> {
                // switch from grid to list
                listPos = (gridPos * numOfGridColumns * listCardHeight / gridCardHeight).roundToInt()
                if (listPos < display.heightPixels) listPos = display.heightPixels
                listViewLimit = 60.0 * timesFirstFetch * limitInc * listCardHeight
                viewLimit = listViewLimit
                mode = 0
            }
        }
        // println("viewLimit: $viewLimit")
    }

    fun onFilteredByName(filter : String) {
        addFilter = filter
        listViewLimit = 60.0 * listCardHeight * timesFirstFetch
        gridViewLimit = 60.0 * gridCardHeight * timesFirstFetch / numOfGridColumns
        viewLimit = listViewLimit
        limitInc = 1
        listPos = display.heightPixels
        gridPos = display.heightPixels
    }
}