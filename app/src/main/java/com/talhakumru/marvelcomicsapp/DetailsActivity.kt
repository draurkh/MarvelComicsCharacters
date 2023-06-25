package com.talhakumru.marvelcomicsapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.SpannedString
import android.util.JsonReader
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.gson.Gson
import com.talhakumru.marvelcomicsapp.databinding.ActivityDetailsBinding
import com.talhakumru.marvelcomicsapp.local_data.CharacterViewModel
import com.talhakumru.marvelcomicsapp.local_data.tables.Character
import com.talhakumru.marvelcomicsapp.local_data.tables.Favourite
import com.talhakumru.marvelcomicsapp.marvel_data.lists.AbstractMediaList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.ComicList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.EventList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.SeriesList
import com.talhakumru.marvelcomicsapp.marvel_data.lists.StoryList
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    private lateinit var character : Character
    private var position : Int = 0
    private val apiController = MarvelAPIController()
    private val viewModel : CharacterViewModel by viewModels()
    lateinit var mainHandler : Handler
    private val updateTask = object : Runnable {
        override fun run() {
            listingTask()
            mainHandler.postDelayed(this, 1000)
        }
    }
    var seriesLimit : Int = 0
    var storiesLimit : Int = 0
    var eventsLimit : Int = 0
    var comicsLimit : Int = 0

    var limits = Array<Boolean>(4) { true }
    lateinit var favData : Favourite

    override fun onCreate(savedInstanceState: Bundle?) {
        println("DetailsActivity onCreate is entered")
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // set toolbar as appbar of the activity
        setSupportActionBar(findViewById(R.id.detailsToolbar))
        val appBar = supportActionBar

        val intent = intent
        val gson = Gson()
        favData = gson.fromJson(intent.getStringExtra("favData"), Favourite::class.java)
        val localSize = intent.getIntExtra("localSize",-1)
        position = intent.getIntExtra("position", -1)
        val id = intent.getIntExtra("id", -1)

        if (localSize == -1) throw java.io.IOException("Invalid localSize passed to Details activity")
        if (position == -1) throw java.io.IOException("Invalid position passed to Details activity")
        if (id == -1) throw java.io.IOException("Invalid id passed to Details activity")

        if (favData.isLocal == 1) {
            println("character is local")
            character = viewModel.getCharacter(id)!!
        }
        else {
            println("character is not local")
            character = MarvelAPIController.dataWrapper.data.results[position - localSize]
            character.imageURL = "${character.thumbnail.path}.${character.thumbnail.extension}"
        }

        mainHandler = Handler(Looper.getMainLooper())
        println("Character ID: ${id}")



        if (character.series.elements.isEmpty() &&
            character.stories.elements.isEmpty() &&
            character.events.elements.isEmpty() &&
            character.comics.elements.isEmpty())
        {
            // character media has not yet initialized
            if (favData.isLocal == 0) {
                // data is online
                seriesLimit  = getMedia(character.series,"series")
                storiesLimit = getMedia(character.stories,"stories")
                eventsLimit  = getMedia(character.events,"events")
                comicsLimit  = getMedia(character.comics,"comics")
            } else {
                // data is local
                getLocalMedia()
            }
        }

        //runOnce = arrayOf(true, true, true, true)


        binding.detailsNameView.text = character.name
        binding.detailsImageView.load(character.imageURL)
        //println("${character.id}: ${character.isFavourite}")
        println("DetailsActivity onCreate is exited")
    }

    fun getMedia(list : AbstractMediaList, mediaType : String) : Int {
        val limit = if (list.available > 100) 100 else list.available
        if (limit != 0)
            fetchData(list.elements, mediaType, limit)
        return limit
    }
    
    fun getLocalMedia() {
        val gson = Gson()
        character.series = gson.fromJson(character.seriesJson, SeriesList::class.java)
        character.stories = gson.fromJson(character.storiesJson, StoryList::class.java)
        character.events = gson.fromJson(character.eventsJson, EventList::class.java)
        character.comics = gson.fromJson(character.comicsJson, ComicList::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_bar_menu, menu)
        menu?.let {
            val favItem = it.findItem(R.id.favoriButonu)
            if (favData.isFavourite == 0) {
                favItem.title = getString(R.string.add_fav)
            } else {
                favItem.title = getString(R.string.rm_fav)
            }

            val localItem = it.findItem(R.id.yerelButonu)
            if (favData.isLocal == 0) {
                localItem.title = getString(R.string.add_local)
            } else {
                localItem.title = getString(R.string.rm_local)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favoriButonu -> {
                println("FAVORI CONTEXT ITEM SECILDI")
                if (favData.isFavourite == 0) {
                    // not fav
                    viewModel.addFavourite(Favourite(character.id, 1, favData.isLocal))
                    favData.isFavourite = 1
                    item.title = getString(R.string.rm_fav)
                } else if (favData.isFavourite == 1 && favData.isLocal == 1) {
                    // fav and local
                    viewModel.addFavourite(Favourite(character.id, 0, favData.isLocal))
                    favData.isFavourite = 0
                    item.title = getString(R.string.add_fav)
                } else {
                    // fav but not local
                    viewModel.deleteFavourite(character.id)
                    favData.isFavourite = 0
                    item.title = getString(R.string.add_fav)
                }
            }
            R.id.yerelButonu -> {
                println("YEREL CONTEXT ITEM SECILDI")
                if (favData.isLocal == 0) {
                    // not local
                    viewModel.addFavourite(Favourite(character.id, favData.isFavourite, 1))
                    favData.isLocal = 1
                    item.title = getString(R.string.rm_fav)
                    val gson = Gson()
                    character.seriesJson = gson.toJson(character.series)
                    character.storiesJson = gson.toJson(character.stories)
                    character.eventsJson = gson.toJson(character.events)
                    character.comicsJson = gson.toJson(character.comics)
                    character.imageURL = "${character.thumbnail.path}.${character.thumbnail.extension}"
                    character.numOfSeries = character.series.available
                    viewModel.addCharacter(character)
                } else if (favData.isLocal == 1 && favData.isFavourite == 1) {
                    // local and fav
                    viewModel.addFavourite(Favourite(character.id, favData.isFavourite, 0))
                    favData.isLocal = 0
                    item.title = getString(R.string.add_fav)
                    viewModel.removeCharacter(character)
                } else {
                    // local but not fav
                    viewModel.deleteFavourite(character.id)
                    favData.isLocal = 0
                    item.title = getString(R.string.add_fav)
                    viewModel.removeCharacter(character)
                }
            }
        }
        return false
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
        if (limits[0] && character.series.elements.size >= seriesLimit) {
            binding.seriesTextView.text = printResultsOnScreen(character.series.elements)
            limits[0] = false
        }
        if (limits[1] && character.stories.elements.size >= storiesLimit) {
            binding.storiesTextView.text = printResultsOnScreen(character.stories.elements)
            limits[1] = false
        }
        if (limits[2] && character.events.elements.size >= eventsLimit) {
            binding.eventsTextView.text = printResultsOnScreen(character.events.elements)
            limits[2] = false
        }
        if (limits[3] && character.comics.elements.size >= comicsLimit) {
            binding.comicsTextView.text = printResultsOnScreen(character.comics.elements)
            limits[3] = false
        }
    }

    fun printResultsOnScreen(list : List<String>) : SpannedString {
        //var text = "<h2><b>${heading}</b></h2>"
        var text : String = ""
        if (list.isEmpty()) text = text.plus("<p>This character is not included in any series.</p>")
        //println(text)
        //println("there are ${character.series.list.size} items in series")
        for (title in list) {
            text = text.plus("<ul>${title}<ul>")
        }
        val printText = Html.fromHtml(text, Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)
        return SpannedString(printText)
    }

    fun fetchData(list : ArrayList<String>, mediaType : String, limit : Int) {
        val URL = apiController.createRequestURL("/${character.id}/${mediaType}?limit=${limit}&")

        apiController.asyncGet(URL, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    val reader = JsonReader(res.body.source().inputStream().reader())

                    reader.beginObject()
                    while (reader.hasNext()) {
                        val nameInBody = reader.nextName()
                        if (nameInBody.equals("data")) {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                val nameInData = reader.nextName()
                                if (nameInData.equals("results")) {
                                    reader.beginArray()
                                    println("-----${mediaType} added-----")
                                    while (reader.hasNext()) {
                                        reader.beginObject()
                                        while (reader.hasNext()) {
                                            val nameInResults = reader.nextName()
                                            if (nameInResults.equals("title")) {
                                                val title = reader.nextString()
                                                //println(title)
                                                list.add(title)
                                            } else reader.skipValue()
                                        }
                                        reader.endObject()
                                    }
                                    reader.close()
                                    return
                                } else reader.skipValue()
                            }
                            reader.endObject()

                        } else reader.skipValue()
                    }
                    reader.endObject()
                }
            }

        })
    }

}
