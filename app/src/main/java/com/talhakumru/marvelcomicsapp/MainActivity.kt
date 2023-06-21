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
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.talhakumru.marvelcomicsapp.databinding.ActivityMainBinding
import com.talhakumru.marvelcomicsapp.local_data.CharacterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var appBar : ActionBar? = null

    //private var selectedLayout = R.drawable.view_list

    val deviceMetrics = Resources.getSystem().displayMetrics

    lateinit var linearLayoutManager : LinearLayoutManager
    lateinit var gridLayoutManager : GridLayoutManager
    private val adapter = RecyclerListAdapter()

    // create a MarvelAPIController to fetch data
    val marvelAPIController = MarvelAPIController()

    //lateinit var recyclerListAdapter : RecyclerListAdapter
    //lateinit var recyclerGridAdapter : RecyclerListAdapter

    lateinit var onScrollListener : RecyclerScrollListener

    var listSize : Int = 0

    lateinit var mainHandler : Handler
    private val updateTask = object : Runnable {
        override fun run() {
            listingTask()
            mainHandler.postDelayed(this, 3000)
        }
    }

    //private lateinit var characterViewModel : CharacterViewModel
    //private var localCharacters = ArrayList<Character>()

    override fun onCreate(savedInstanceState: Bundle?) {
        println("MainActivity OnCreate is called.")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // set toolbar as appbar of the activity
        setSupportActionBar(findViewById(R.id.appToolbar))
        appBar = supportActionBar

        // RecyclerView Layout Managers
        // Linear(list) and Grid layouts
        val gridCols = calculateGridColumnCount()
        linearLayoutManager = LinearLayoutManager(this)
        gridLayoutManager = GridLayoutManager(this, gridCols)
        binding.recyclerView.layoutManager = linearLayoutManager

        // RecyclerView Adapter
        binding.recyclerView.adapter = adapter

        // Character View Model
        // Local characters are shown at the top of RecyclerView

        val characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel.readAll.observe(this, Observer { characters ->
            adapter.setList(characters)
        })


        val filters = "?limit=100&"
        getCharacters(filters)


        //recyclerListAdapter = RecyclerListAdapter(R.drawable.view_list)
        //recyclerGridAdapter = RecyclerListAdapter(R.drawable.view_grid)
        //binding.recyclerView.adapter = recyclerListAdapter

        onScrollListener = RecyclerScrollListener(marvelAPIController, deviceMetrics)
        binding.recyclerView.addOnScrollListener(onScrollListener)

        mainHandler = Handler(Looper.getMainLooper())

        println("onCreate exited")
    }

    fun getCharacters(filter : String) {
        println("getCharacters entered")
        marvelAPIController.getGson(filter)
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
                marvelAPIController.dataWrapper.data.results.clear()
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
        if (marvelAPIController.dataWrapper.data.results.size != listSize) {
            println("dataset changed")
            //adapter.addToList(marvelAPIController.dataWrapper.data.results)
            adapter.notifyDataSetChanged()
            listSize = marvelAPIController.dataWrapper.data.results.size
            println("size: ${adapter.itemCount}")
            println("Size: ${marvelAPIController.dataWrapper.data.results.size}")
        }
    }

    fun toggleLayout(view: View) {
        when (adapter.mode) {
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
        }
        binding.layoutButton.setImageResource(adapter.mode)

/*
        binding.recyclerView.removeOnScrollListener(onScrollListener)
        onScrollListener = RecyclerScrollListener(marvelAPIController, deviceMetrics, selectedLayout)
        binding.recyclerView.addOnScrollListener(onScrollListener)*/
    }

    fun calculateGridColumnCount() : Int {
        val gridCardWidthPixels = deviceMetrics.density * 125
        return (deviceMetrics.widthPixels / gridCardWidthPixels).roundToInt()
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