package com.talhakumru.marvelcomicsapp

import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.talhakumru.marvelcomicsapp.local_data.Character
import com.talhakumru.marvelcomicsapp.local_data.CharacterViewModel
import kotlinx.coroutines.Runnable
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    // use View Binding
    private lateinit var binding : ActivityMainBinding
    private var appBar : ActionBar? = null

    val displayMetrics = Resources.getSystem().displayMetrics

    val gridCols = calculateGridColumnCount()
    var gridLayoutManager = GridLayoutManager(this, 1)
    var adapter = RecyclerViewAdapter(gridLayoutManager)

    // character list, shown by recycler view
    var characters = ArrayList<Character>()
    // holds the previous size of characters array if it is updated
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
            mainHandler.postDelayed(this, 1000)
        }
    }

    // accesses app database
    val characterViewModel : CharacterViewModel by viewModels()
    var localCharacters = emptyList<Character>()

    override fun onCreate(savedInstanceState: Bundle?) {
        println(listCardPerPage)
        println(minNumberOfFirstFetch)
        println("MainActivity.OnCreate is called.")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // set toolbar as appbar of the activity
        setSupportActionBar(findViewById(R.id.appToolbar))
        appBar = supportActionBar

        binding.recyclerView.layoutManager = gridLayoutManager
        binding.recyclerView.adapter = adapter

        characterViewModel.readAll.observe(this, Observer { localList ->
            // update the array at every update in database
            localCharacters = localList
            if (apiController.dataWrapper.data.results.isEmpty()) {
                // show local characters when online data is not available
                characters = localList as ArrayList<Character>
            }
        })

        onScrollListener = RecyclerScrollListener(apiController, minNumberOfFirstFetch, displayMetrics)

        // fetch first items to initiate scrolling
        apiController.minNumber = minNumberOfFirstFetch
        val filters = "?limit=100&"
        getCharacters(filters)

        binding.recyclerView.addOnScrollListener(onScrollListener)

        mainHandler = Handler(Looper.getMainLooper())

        println("onCreate exited")
    }

    fun getCharacters(filter : String) {
        println("getCharacters entered")
        apiController.getGson(filter)
        println("getCharacters exited")
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
                apiController.dataWrapper.data.results.clear()
                listSize = 0
                mainHandler.post(updateTask)
                //marvelAPIController.getCharacters("?nameStartsWith=${query}&")
                getCharacters("?nameStartsWith=${query}&")
                return false
            }
        })

        searchView.setOnCloseListener(object : OnCloseListener {
            override fun onClose(): Boolean {
                println("searchview closed")
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
        val marvelList = apiController.dataWrapper.data.results
        /*if (marvelList.isEmpty()) {
            // load 4 pages of characters when available
            var numOfFirstFetch : Int = if (adapter.mode == R.drawable.view_list) 4 * onScrollListener.listCardPerPage
            else 4 * onScrollListener.gridCardPerPage
            val numOfRequests : Int = (numOfFirstFetch / 100.0).toInt()

            lifecycleScope.launch() {
                for (i in 0..numOfRequests) {
                    val task = launch { getCharacters("?offset=${i * 100}&limit=${100}&") }
                    task.await()
                }
            }
            adapter.setList(marvelList)
            listSize = marvelList.size
        }*/
        if (marvelList.size != listSize) {
            println("dataset changed")
            //adapter.addToList(marvelList)
            adapter.setList(marvelList)
            listSize = marvelList.size
            println("size: ${adapter.itemCount}")
            println("Size: ${marvelList.size}")
        }
    }

    fun toggleLayout(view: View) {
        /*when (adapter.mode) {
            R.drawable.view_list -> {
                adapter.mode = R.drawable.view_grid
                binding.recyclerView.layoutManager = gridLayoutManager
                onScrollListener.mode = 1
                scrollToYfromListToGrid()
                //binding.recyclerView.scrollBy(0, onScrollListener.yPos)
            }

            R.drawable.view_grid -> {
                adapter.mode = R.drawable.view_list
                binding.recyclerView.layoutManager = linearLayoutManager
                onScrollListener.mode = 0
                scrollToYfromGridToList()
                //binding.recyclerView.scrollBy(0, onScrollListener.yPos)
            }
        }*/

        if (gridLayoutManager.spanCount == 1) {
            gridLayoutManager.spanCount = gridCols
            binding.layoutButton.setImageResource(R.drawable.view_grid)
        }
        else {
            gridLayoutManager.spanCount = 1
            binding.layoutButton.setImageResource(R.drawable.view_list)
        }
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