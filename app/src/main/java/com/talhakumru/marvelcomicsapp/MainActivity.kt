package com.talhakumru.marvelcomicsapp

import android.graphics.drawable.Icon
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.app.ActionBar
import androidx.core.text.italic
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.talhakumru.marvelcomicsapp.databinding.ActivityMainBinding
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val characterList = ArrayList<String>()
    private val pictureList = ArrayList<Int>()
    private var appBar : ActionBar? = null
    private var selectedLayout : Int = R.drawable.list_layout
    val linearLayoutManager = LinearLayoutManager(this)
    val gridLayoutManager = GridLayoutManager(this, 3)
    lateinit var recyclerListAdapter : RecyclerListAdapter
    lateinit var recyclerGridAdapter : RecyclerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // set toolbar as appbar of the activity
        setSupportActionBar(findViewById(R.id.appToolbar))
        appBar = supportActionBar

        characterList.add("Batman")
        characterList.add("Spider-man")
        characterList.add("Superman")
        characterList.add("Ironman")
        characterList.add("Captain America")
        characterList.add("Doctor Strange")
        characterList.add("Batman")
        characterList.add("Spider-man")
        characterList.add("Superman")
        characterList.add("Ironman")
        characterList.add("Captain America")
        characterList.add("Doctor Strange")
        characterList.add("Batman")
        characterList.add("Spider-man")
        characterList.add("Superman")
        characterList.add("Ironman")
        characterList.add("Captain America")
        characterList.add("Doctor Strange")
        characterList.add("Batman")
        characterList.add("Spider-man")
        characterList.add("Superman")
        characterList.add("Ironman")
        characterList.add("Captain America")
        characterList.add("Doctor Strange")
        characterList.add("Batman")
        characterList.add("Spider-man")
        characterList.add("Superman")
        characterList.add("Ironman")
        characterList.add("Captain America")
        characterList.add("Doctor Strange")

        pictureList.add(R.drawable.batman)
        pictureList.add(R.drawable.spiderman)
        pictureList.add(R.drawable.superman)
        pictureList.add(R.drawable.ironman)
        pictureList.add(R.drawable.captainamerica)
        pictureList.add(R.drawable.drstrange)
        pictureList.add(R.drawable.batman)
        pictureList.add(R.drawable.spiderman)
        pictureList.add(R.drawable.superman)
        pictureList.add(R.drawable.ironman)
        pictureList.add(R.drawable.captainamerica)
        pictureList.add(R.drawable.drstrange)
        pictureList.add(R.drawable.batman)
        pictureList.add(R.drawable.spiderman)
        pictureList.add(R.drawable.superman)
        pictureList.add(R.drawable.ironman)
        pictureList.add(R.drawable.captainamerica)
        pictureList.add(R.drawable.drstrange)
        pictureList.add(R.drawable.batman)
        pictureList.add(R.drawable.spiderman)
        pictureList.add(R.drawable.superman)
        pictureList.add(R.drawable.ironman)
        pictureList.add(R.drawable.captainamerica)
        pictureList.add(R.drawable.drstrange)
        pictureList.add(R.drawable.batman)
        pictureList.add(R.drawable.spiderman)
        pictureList.add(R.drawable.superman)
        pictureList.add(R.drawable.ironman)
        pictureList.add(R.drawable.captainamerica)
        pictureList.add(R.drawable.drstrange)


        binding.recyclerView.layoutManager = linearLayoutManager

        recyclerListAdapter = RecyclerListAdapter(characterList, pictureList, 0)
        recyclerGridAdapter = RecyclerListAdapter(characterList, pictureList, 1)
        binding.recyclerView.adapter = recyclerListAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)

        val searchItem = menu?.findItem(R.id.appbarSearch)
        val searchView = searchItem?.actionView as SearchView

        val listener = object : OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        }
        searchView.setOnQueryTextListener(listener)

        return super.onCreateOptionsMenu(menu)
    }

    fun toggleLayout(view: View) {
        when (selectedLayout) {
            R.drawable.list_layout -> {
                selectedLayout = R.drawable.grid_layout
                binding.recyclerView.layoutManager = gridLayoutManager
                binding.recyclerView.adapter = recyclerGridAdapter
            }

            R.drawable.grid_layout -> {
                selectedLayout = R.drawable.list_layout
                binding.recyclerView.layoutManager = linearLayoutManager
                binding.recyclerView.adapter = recyclerListAdapter
            }
        }
        binding.layoutButton.setImageIcon(Icon.createWithResource(view.context,selectedLayout))
    }

}