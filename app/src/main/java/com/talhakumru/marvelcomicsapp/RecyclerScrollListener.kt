package com.talhakumru.marvelcomicsapp

import android.util.DisplayMetrics
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class RecyclerScrollListener(private val controller : MarvelAPIController, private val display : DisplayMetrics) : RecyclerView.OnScrollListener() {

    // list=0, grid=1
    var mode = 0

    val list = controller.dataWrapper.data.results

    val listCardHeight = display.density * 150
    val gridCardHeight = display.density * 260
    val gridCardWidth = display.density * 125

    val listCardPerPage = (display.heightPixels / listCardHeight).roundToInt()
    val gridCardPerPage = ((display.heightPixels / gridCardHeight) * (display.widthPixels / gridCardWidth)).roundToInt()

    // 100 * listCardHeight * 0.8
    var listViewLimit : Double = 80.0 * listCardHeight
    // (100 * gridCardHeight / (display.widthPixels / gridCardWidth).roundToInt()) * 0.8
    var gridViewLimit : Double = 80.0 * gridCardHeight * (gridCardWidth / display.widthPixels).roundToInt()
    var viewLimit = listViewLimit
    var limitInc = 2

    var listPos = 0
    var gridPos = 0
    var pos = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (mode == 0) {
            listPos += dy
            pos = listPos
            viewLimit = listViewLimit
        }
        else if (mode == 1) {
            gridPos += dy
            pos = gridPos
            viewLimit = gridViewLimit
        }
        println("limit: ${viewLimit}")
        println("vPos: ${pos}")
        if (pos >= viewLimit && list.size % 100 == 0) {
            println("${viewLimit} eşiği ${pos} ile aşıldı")
            println("liste karti yuksekligi: ${listCardHeight}")
            println("grid karti yuksekligi: ${gridCardHeight}")
            if (mode == 0) listViewLimit = (100 * limitInc++) * listCardHeight * 0.8
            else if (mode == 1) listViewLimit = (100 * limitInc++) * gridCardHeight * 0.8
            println("View limit: $listViewLimit")
            val filter = "?offset=${list.size}&limit=${100}&"
            controller.getGson(filter)
            println("listede ${controller.dataWrapper.data.results.size} tane eleman var")

        }
    }

    fun whenToggled(dy: Int) {

    }
}