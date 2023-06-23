package com.talhakumru.marvelcomicsapp

import android.util.DisplayMetrics
import androidx.recyclerview.widget.RecyclerView
import com.talhakumru.marvelcomicsapp.local_data.Character
import kotlin.math.roundToInt

class RecyclerScrollListener(private val controller : MarvelAPIController, private val numOfFirstFetch : Int, private val display : DisplayMetrics) : RecyclerView.OnScrollListener() {

    // list=0, grid=1
    var mode = 0

    var listSize : Int = MarvelAPIController.dataWrapper.data.results.size

    val listCardHeight = display.density * 150
    val gridCardHeight = display.density * 260
    val gridCardWidth = display.density * 125

    val listCardPerPage = (display.heightPixels / listCardHeight).roundToInt()
    val gridCardPerPage = ((display.heightPixels / gridCardHeight) * (display.widthPixels / gridCardWidth)).roundToInt()

    // how many times a 100 character fetch occured
    val timesFirstFetch = (numOfFirstFetch / 100).toInt() + 1

    // listViewLimit = 100 * timesFirstFetch * listCardHeight * 0.7
    var listViewLimit : Double = 70.0 * timesFirstFetch * listCardHeight
    // gridViewLimit = (100 * gridCardHeight / (display.widthPixels / gridCardWidth).roundToInt()) * 0.7
    var gridViewLimit : Double = 70.0 * timesFirstFetch * gridCardHeight * (gridCardWidth / display.widthPixels).roundToInt()
    var viewLimit = listViewLimit
    var limitInc = 2

    var listPos = display.heightPixels
    var gridPos = display.heightPixels
    var pos = display.heightPixels

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (mode == 0) {
            listPos += dy
            pos = listPos
            //viewLimit = listViewLimit
        }
        else if (mode == 1) {
            gridPos += dy
            pos = gridPos
            //viewLimit = gridViewLimit
        }
        println("limit: ${viewLimit}")
        println("vPos: ${pos}")
        if (pos > viewLimit && listSize != MarvelAPIController.dataWrapper.data.results.size) {
            //println("${viewLimit} eşiği ${pos} ile aşıldı")
            //println("liste karti yuksekligi: ${listCardHeight}")
            //println("grid karti yuksekligi: ${gridCardHeight}")
            if (mode == 0) {
                listViewLimit = (100 * timesFirstFetch * limitInc++) * listCardHeight * 0.7
                viewLimit = listViewLimit
            }
            else if (mode == 1) {
                gridViewLimit = (100 * timesFirstFetch * limitInc++) * gridCardHeight * 0.7
                viewLimit = gridViewLimit
            }
            println("View limit: $viewLimit")
            val filter = "?offset=${listSize+100}&limit=${100}&"
            controller.getGson(filter)
            listSize = MarvelAPIController.dataWrapper.data.results.size
            println("listede ${MarvelAPIController.dataWrapper.data.results.size}=${listSize} tane eleman var")
        }
    }
}