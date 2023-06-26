package com.talhakumru.marvelcomicsapp

import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class RecyclerScrollListener(private val controller : MarvelAPIController, private val numOfFirstFetch : Int, private val display : DisplayMetrics) : RecyclerView.OnScrollListener() {

    // list=0, grid=1
    private var mode = 0

    private var listSize : Int = MarvelAPIController.dataWrapper.data.results.size

    val listCardHeight = display.density * (150 + 6)
    val gridCardHeight = display.density * (260 + 6)
    private val gridCardWidth = display.density * (125 + 6)
    private val numOfGridColumns = display.widthPixels / gridCardWidth

    //private val listCardPerPage = display.heightPixels / listCardHeight
    //private val gridCardPerPage = (display.heightPixels / gridCardHeight) * numOfGridColumns

    // how many times a 100 character fetch occured
    val timesFirstFetch = numOfFirstFetch / 100 + 1

    // listViewLimit = 100 * timesFirstFetch * listCardHeight * 0.6
    var listViewLimit : Double = 60.0 * listCardHeight * timesFirstFetch
    // gridViewLimit = (100 * gridCardHeight / (display.widthPixels / gridCardWidth).roundToInt()) * 0.6
    var gridViewLimit : Double = 60.0 * gridCardHeight * timesFirstFetch / numOfGridColumns
    var viewLimit = listViewLimit
    var limitInc = 1

    // starts from the bottom of the screen
    var listPos = display.heightPixels
    var gridPos = display.heightPixels
    var pos = display.heightPixels

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (mode == 0) {
            listPos += dy
            pos = listPos
        }
        else{
            gridPos += dy
            pos = gridPos
        }
        //println("limit: ${viewLimit}")
        println("vPos: ${pos}")
        if (pos > viewLimit && listSize != MarvelAPIController.dataWrapper.data.results.size) {
            println("${viewLimit} eşiği ${pos} ile aşıldı")
            //println("liste karti yuksekligi: ${listCardHeight}")
            //println("grid karti yuksekligi: ${gridCardHeight}")
            if (mode == 0) {
                listViewLimit = 60.0 * timesFirstFetch * ++limitInc * listCardHeight
                viewLimit = listViewLimit
            }
            else if (mode == 1) {
                gridViewLimit = 60.0 * timesFirstFetch * ++limitInc * gridCardHeight / numOfGridColumns
                viewLimit = gridViewLimit
            }
            println("View limit: $viewLimit")
            val filter = "?offset=${listSize+100}&limit=${100}&"
            controller.getGson(filter)
            listSize = MarvelAPIController.dataWrapper.data.results.size
            println("listede ${MarvelAPIController.dataWrapper.data.results.size}=${listSize} tane eleman var")
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
        println("viewlimit: $viewLimit")
    }
}