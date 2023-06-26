package com.talhakumru.marvelcomicsapp

import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.SearchView.OnCloseListener
import android.widget.SearchView.OnQueryTextListener
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.talhakumru.marvelcomicsapp.databinding.ActivityMainBinding
import com.talhakumru.marvelcomicsapp.local_data.tables.Character
import com.talhakumru.marvelcomicsapp.local_data.CharacterViewModel
import kotlinx.coroutines.Runnable
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    // use View Binding
    private lateinit var binding : ActivityMainBinding
    private var appBar : ActionBar? = null

    private val displayMetrics : DisplayMetrics = Resources.getSystem().displayMetrics

    // accesses app database
    private val characterViewModel : CharacterViewModel by viewModels()

    private val gridCols = calculateGridColumnCount()
    private var gridLayoutManager = GridLayoutManager(this, 1)
    private lateinit var adapter : RecyclerViewAdapter

    /*
    * size of online characters array,
    * holds the previous size of online characters array if it is updated
    */
    var listSize : Int = 0

    // create a MarvelAPIController to fetch data
    val apiController = MarvelAPIController()

    // first fetched data must fit to 4 screens to enable scrolling
    val listCardPerPage = (displayMetrics.heightPixels / (displayMetrics.density * 150)).roundToInt()
    val minNumberOfFirstFetch = 4 * listCardPerPage

    // listens to scroll events
    lateinit var onScrollListener : RecyclerScrollListener

    // runs every second
    lateinit var mainHandler : Handler
    private val updateTask = object : Runnable {
        override fun run() {
            listingTask()
            mainHandler.postDelayed(this, 500)
        }
    }

    var isFiltered : Boolean = false

    var localCharacters = emptyList<Character>()
    var filteredLocalCharacters = emptyList<Character>()

    override fun onCreate(savedInstanceState: Bundle?) {
        //println(listCardPerPage)
        //println(minNumberOfFirstFetch)
        println("MainActivity.OnCreate is called.")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // set toolbar as appbar of the activity
        setSupportActionBar(findViewById(R.id.appToolbar))
        appBar = supportActionBar

        adapter = RecyclerViewAdapter(gridLayoutManager, characterViewModel)

        binding.recyclerView.layoutManager = gridLayoutManager
        binding.recyclerView.adapter = adapter

        characterViewModel.readAll.observe(this, Observer { localList ->
            // update the array at every update in database
            localCharacters = localList
            adapter.setLocalList(localList)
            for (item in localList) {
                println(item)
            }
        })

        characterViewModel.readFavourites.observe(this) { favList ->
            println("fav list changed")
            println(favList)
            adapter.notifyDataSetChanged()
        }

        onScrollListener = RecyclerScrollListener(apiController, minNumberOfFirstFetch, displayMetrics)
        binding.recyclerView.addOnScrollListener(onScrollListener)

        // fetch first items to initiate scrolling
        apiController.minNumber = minNumberOfFirstFetch
        apiController.getGson("?limit=100&")

        mainHandler = Handler(Looper.getMainLooper())

        println("onCreate exited")
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)

        val searchItem = menu?.findItem(R.id.appbarSearch)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                println("$newText entered")
                return true
            }
            override fun onQueryTextSubmit(query: String?): Boolean {
                println("text submitted")
                mainHandler.removeCallbacks(updateTask)
                isFiltered = true
                filteredLocalCharacters = localCharacters.filter {
                    if (query != null) {
                        it.name.startsWith(prefix = query, ignoreCase = true)
                    } else return true
                }
                MarvelAPIController.dataWrapper.data.results.clear()
                apiController.getGson("?nameStartsWith=${query}&limit=100&")
                adapter.setLocalList(filteredLocalCharacters)
                adapter.notifyDataSetChanged()
                mainHandler.post(updateTask)
                return false
            }
        })

        searchView.setOnCloseListener(object : OnCloseListener {
            override fun onClose(): Boolean {
                println("searchview closed")
                mainHandler.removeCallbacks(updateTask)
                adapter.setLocalList(localCharacters)
                MarvelAPIController.dataWrapper.data.results.clear()
                apiController.getGson("?limit=100&")
                adapter.setOnlineList(MarvelAPIController.dataWrapper.data.results)
                adapter.notifyDataSetChanged()
                mainHandler.post(updateTask)
                return false
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTask)
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTask)
    }

    fun listingTask() {
        //println("continues...")
        val marvelList = MarvelAPIController.dataWrapper.data.results
        if (marvelList.size != listSize) {
            println("dataset changed")
            adapter.setOnlineList(marvelList)
            listSize = marvelList.size
            println("size: ${adapter.itemCount}")
            println("Size: ${marvelList.size}")
        }
    }

    fun switchLayout(view: View) {

        if (gridLayoutManager.spanCount == 1) {
            // switch list to grid layout
            gridLayoutManager.spanCount = gridCols
            binding.layoutButton.setImageResource(R.drawable.view_grid)
        }
        else {
            // switch grid to list layout
            gridLayoutManager.spanCount = 1
            binding.layoutButton.setImageResource(R.drawable.view_list)
        }
        onScrollListener.switchLayout()
        adapter.notifyItemRangeChanged(0, adapter.itemCount)


/*
        binding.recyclerView.removeOnScrollListener(onScrollListener)
        onScrollListener = RecyclerScrollListener(marvelAPIController, deviceMetrics, selectedLayout)
        binding.recyclerView.addOnScrollListener(onScrollListener)*/
    }

    fun calculateGridColumnCount() : Int {
        val gridCardWidthPixels = displayMetrics.density * 125
        return (displayMetrics.widthPixels / gridCardWidthPixels).roundToInt()
        //val displayWidth = deviceMetrics.widthPixels / deviceMetrics.density
        //val gridCardWidth = 125
        //return (displayWidth / gridCardWidth).roundToInt()
    }

    fun scrollToYfromListToGrid() {
        //onScrollListener.yPos = (onScrollListener.yPos * onScrollListener.gridCardHeight / (onScrollListener.listCardHeight * calculateGridColumnCount())).roundToInt()
        val pos = (onScrollListener.listPos / onScrollListener.listCardHeight).roundToInt()
        println("scrolled to $pos")
        binding.recyclerView.scrollToPosition(pos)

    }

    fun scrollToYfromGridToList() {
        //onScrollListener.yPos = (onScrollListener.yPos * onScrollListener.listCardHeight * calculateGridColumnCount() / onScrollListener.gridCardHeight).roundToInt()
        val pos = (onScrollListener.gridPos * calculateGridColumnCount() / onScrollListener.gridCardHeight).roundToInt()
        println("scrolled to $pos")
        binding.recyclerView.scrollToPosition(pos)
    }
}