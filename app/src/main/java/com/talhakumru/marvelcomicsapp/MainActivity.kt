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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.talhakumru.marvelcomicsapp.databinding.ActivityMainBinding
import com.talhakumru.marvelcomicsapp.local_data.tables.Character
import com.talhakumru.marvelcomicsapp.local_data.CharacterViewModel
import kotlinx.coroutines.Runnable
import kotlin.math.roundToInt

// Main activity contains a list of characters from Marvel
class MainActivity : AppCompatActivity() {
    // use View Binding
    private lateinit var binding : ActivityMainBinding

    private val displayMetrics : DisplayMetrics = Resources.getSystem().displayMetrics

    // access app database
    private val viewModel : CharacterViewModel by viewModels()

    // Recycler view is used for listing data
    private val gridCols = calculateGridColumnCount()
    private var layoutManager = GridLayoutManager(this, 1)
    private lateinit var adapter : RecyclerViewAdapter

    // size of online characters array,
    // holds the previous size of online characters array if it is updated
    var listSize : Int = 0

    // create a MarvelAPIController to fetch data
    val apiController = MarvelAPIController()

    // first fetched data must fit to 4 screens to enable scrolling
    val listCardPerPage = (displayMetrics.heightPixels / displayMetrics.density / 150).roundToInt()
    val minNumberOfFirstFetch = 4 * listCardPerPage

    // listens to scroll events
    lateinit var onScrollListener : RecyclerScrollListener

    lateinit var mainHandler : Handler
    // runs every half a second
    private val updateTask = object : Runnable {
        override fun run() {
            listingTask()
            mainHandler.postDelayed(this, 500)
        }
    }

    // lists of locally stored characters
    var localCharacters = emptyList<Character>()

    var isNameFiltered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // println("entered MainActivity.OnCreate.")
        // println(listCardPerPage)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // set toolbar as appbar of the activity
        setSupportActionBar(findViewById(R.id.appToolbar))

        adapter = RecyclerViewAdapter(layoutManager, viewModel)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        onScrollListener = RecyclerScrollListener(apiController, displayMetrics)
        binding.recyclerView.addOnScrollListener(onScrollListener)

        // fetch first items to initiate scrolling
        apiController.getData("?limit=100&", minNumberOfFirstFetch)

        mainHandler = Handler(Looper.getMainLooper())

        // println("exited MainActivity.onCreate")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)

        val searchItem = menu?.findItem(R.id.appbarSearch)
        val searchView = searchItem?.actionView as SearchView

        // handle filter by name feature
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            // list characters whose names start with the entered query
            override fun onQueryTextSubmit(query: String?): Boolean {
                // println("text submitted")
                mainHandler.removeCallbacks(updateTask)
                isNameFiltered = true
                onScrollListener.onFilteredByName("nameStartsWith=${query}&")
                MarvelAPIController.dataWrapper.data.results.clear()
                apiController.getData("?nameStartsWith=${query}&limit=100&", minNumberOfFirstFetch)
                mainHandler.post(updateTask)
                return true
            }
        })

        searchView.setOnCloseListener(object : OnCloseListener {
            override fun onClose(): Boolean {
                // println("searchview closed")
                if (!isNameFiltered)
                    // SearchView opened but closed without searching
                    return false

                // disable filtering
                mainHandler.removeCallbacks(updateTask)
                isNameFiltered = false
                onScrollListener.onFilteredByName("")
                MarvelAPIController.dataWrapper.data.results.clear()
                apiController.getData("?limit=100&", minNumberOfFirstFetch)
                mainHandler.post(updateTask)
                return false
            }
        })
        return true
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTask)
        // update list when database is changed
        viewModel.readAll.observe(this) { localList ->
            // update the array at every update in database

            localCharacters = localList
            adapter.setLocalList(localList)
        }

        // update list when database is changed
        viewModel.readFavourites.observe(this) { favList ->
            println("Favourites List: $favList")
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
        }
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTask)
    }

    fun listingTask() {
        //println("continues...")
        val marvelList = MarvelAPIController.dataWrapper.data.results

        if (listSize != MarvelAPIController.listSize) {
            // requested data fully fetched and the list is updated

            adapter.setOnlineList(marvelList)
            listSize = MarvelAPIController.listSize
            // println("dataset changed")
        }
    }

    fun switchLayout(view: View) {
        if (layoutManager.spanCount == 1) {
            // switch list to grid layout
            layoutManager.spanCount = gridCols
            binding.layoutButton.setImageResource(R.drawable.view_grid)
        }
        else {
            // switch grid to list layout
            layoutManager.spanCount = 1
            binding.layoutButton.setImageResource(R.drawable.view_list)
        }
        onScrollListener.switchLayout()
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    private fun calculateGridColumnCount() : Int {
        val gridCardWidthPixels = displayMetrics.density * 125
        return (displayMetrics.widthPixels / gridCardWidthPixels).roundToInt()
    }
}