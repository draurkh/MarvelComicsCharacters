package com.talhakumru.marvelcomicsapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.SpannedString
import android.util.JsonReader
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
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
    private var apiController = MarvelAPIController()
    // viewModel for accessing database
    private val viewModel : CharacterViewModel by viewModels()
    lateinit var mainHandler : Handler
    // runs every second
    private val updateTask = object : Runnable {
        override fun run() {
            printingTask()
            mainHandler.postDelayed(this, 1000)
        }
    }
    // how many items can be fetched, maximum of 50
    var seriesLimit : Int = 0
    var storiesLimit : Int = 0
    var eventsLimit : Int = 0
    var comicsLimit : Int = 0

    // printingTask must fetch each type of media only one time
    // after executed once, one of this array's items become false
    var isMediaInit = Array(4) { true }
    lateinit var favData : Favourite    // from intent

    override fun onCreate(savedInstanceState: Bundle?) {
        // println("entered DetailsActivity onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // set toolbar as appbar of the activity
        setSupportActionBar(findViewById(R.id.detailsToolbar))

        initHeaders()

        val intent = intent
        val gson = Gson()
        favData = gson.fromJson(intent.getStringExtra("favData"), Favourite::class.java)
        val position = intent.getIntExtra("position", -1)
        val id = intent.getIntExtra("id", -1)

        if (id == -1) throw IOException("Invalid id type passed to Details activity")

        var detailedImageUrl = ""

        if (favData.isLocal == 1) {
            // println("character is local")
            character = viewModel.getCharacter(id)!!
        }
        else {
            // println("character is not local")
            if (position == -1) throw IOException("Invalid position type passed to Details activity")
            character = MarvelAPIController.dataWrapper.data.results[position]
            detailedImageUrl = "${character.thumbnail.path}/detail.${character.thumbnail.extension}"
        }

        // println("Character ID: $id")

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

        mainHandler = Handler(Looper.getMainLooper())

        binding.detailsNameView.text = character.name

        if (favData.isLocal == 1) binding.detailsImageView.load(character.imageURL)
        else binding.detailsImageView.load(detailedImageUrl)
        // println("exited DetailsActivity onCreate")
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateTask)
        // clicking headers make lists visible or not visible
        val visibilityListener = View.OnClickListener {  headerView ->
            var baseText = ""
            val selectedView : View? = when (headerView.id) {
                binding.seriesHeaderView.id -> {
                    baseText = "SERIES"
                    binding.seriesTextView
                }
                binding.storiesHeaderView.id -> {
                    baseText = "STORIES"
                    binding.storiesTextView
                }
                binding.eventsHeaderView.id -> {
                    baseText = "EVENTS"
                    binding.eventsTextView
                }
                binding.comicsHeaderView.id -> {
                    baseText = "COMICS"
                    binding.comicsTextView
                }
                else -> { null }
            }
            val header = findViewById<TextView>(headerView.id)
            if (selectedView != null) {
                when (selectedView.visibility) {
                    View.VISIBLE -> {
                        // println("view is visible")
                        header.text = getString(R.string.view_gone, baseText)
                        selectedView.visibility = View.GONE
                    }
                    View.GONE -> {
                        // println("view is gone")
                        header.text = getString(R.string.view_visible, baseText)
                        selectedView.visibility = View.VISIBLE
                    }
                    else -> {
                        // println("view is invisible")
                    }
                }
            }
        }

        binding.seriesHeaderView.setOnClickListener(visibilityListener)
        binding.storiesHeaderView.setOnClickListener(visibilityListener)
        binding.eventsHeaderView.setOnClickListener(visibilityListener)
        binding.comicsHeaderView.setOnClickListener(visibilityListener)
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTask)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_bar_menu, menu)
        menu?.let { barMenu ->
            val favItem = barMenu.findItem(R.id.favButton)
            if (favData.isFavourite == 0) {
                favItem.title = getString(R.string.add_fav)
            } else {
                favItem.title = getString(R.string.rm_fav)
            }

            val localItem = barMenu.findItem(R.id.localButton)
            if (favData.isLocal == 0) {
                localItem.title = getString(R.string.add_local)
            } else {
                localItem.title = getString(R.string.rm_local)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favButton -> {
                // println("Favourite context item is chosen")
                if (favData.isFavourite == 0) {
                    // if not fav then add to fav list
                    favData.isFavourite = 1
                    viewModel.addFavourite(Favourite(character.id, favData.isFavourite, favData.isLocal))
                    item.title = getString(R.string.rm_fav)
                    Toast.makeText(this,getString(R.string.toast_fav_added, character.name),Toast.LENGTH_SHORT).show()
                    return false
                }
                // if fav then remove from fav list
                favData.isFavourite = 0
                if (favData.isLocal == 1)
                    // only change data
                    viewModel.addFavourite(Favourite(character.id, favData.isFavourite, favData.isLocal))
                else
                    // delete row
                    viewModel.deleteFavourite(character.id)
                item.title = getString(R.string.add_fav)
                Toast.makeText(this,getString(R.string.toast_fav_removed, character.name),Toast.LENGTH_SHORT).show()
            }
            R.id.localButton -> {
                // println("Local context item is chosen")
                if (favData.isLocal == 0) {
                    // if not local, then add to local list
                    val gson = Gson()
                    character.seriesJson = gson.toJson(character.series)
                    character.storiesJson = gson.toJson(character.stories)
                    character.eventsJson = gson.toJson(character.events)
                    character.comicsJson = gson.toJson(character.comics)
                    character.imageURL = "${character.thumbnail.path}/standard_fantastic.${character.thumbnail.extension}"
                    character.numOfSeries = character.series.available
                    viewModel.addCharacter(character)

                    favData.isLocal = 1
                    viewModel.addFavourite(Favourite(character.id, favData.isFavourite, favData.isLocal))
                    item.title = getString(R.string.rm_local)
                    Toast.makeText(this,getString(R.string.toast_local_added, character.name), Toast.LENGTH_SHORT).show()
                    return false
                }
                // if local, then remove from local list
                viewModel.removeCharacter(character)
                item.title = getString(R.string.add_local)
                favData.isLocal = 0
                if (favData.isFavourite == 1)
                    // only change data
                    viewModel.addFavourite(Favourite(character.id, favData.isFavourite, favData.isLocal))
                else
                    // delete row
                    viewModel.deleteFavourite(character.id)
                Toast.makeText(this,getString(R.string.toast_local_removed, character.name), Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }

    private fun initHeaders() {
        binding.seriesHeaderView.text = getString(R.string.view_gone, "SERIES")
        binding.storiesHeaderView.text = getString(R.string.view_gone, "STORIES")
        binding.eventsHeaderView.text = getString(R.string.view_gone, "EVENTS")
        binding.comicsHeaderView.text = getString(R.string.view_gone, "COMICS")
    }

    private fun printingTask() {
        if (isMediaInit[0] && character.series.elements.size >= seriesLimit) {
            binding.seriesTextView.text = printResultsOnScreen(character.series.elements)
            isMediaInit[0] = false
        }
        if (isMediaInit[1] && character.stories.elements.size >= storiesLimit) {
            binding.storiesTextView.text = printResultsOnScreen(character.stories.elements)
            isMediaInit[1] = false
        }
        if (isMediaInit[2] && character.events.elements.size >= eventsLimit) {
            binding.eventsTextView.text = printResultsOnScreen(character.events.elements)
            isMediaInit[2] = false
        }
        if (isMediaInit[3] && character.comics.elements.size >= comicsLimit) {
            binding.comicsTextView.text = printResultsOnScreen(character.comics.elements)
            isMediaInit[3] = false
        }
    }

    private fun printResultsOnScreen(list : List<String>) : SpannedString {
        var text = ""
        if (list.isEmpty()) text += getString(R.string.no_media)
        for (title in list) {
            text += "<ul>${title}"
        }
        val printText = Html.fromHtml(text, Html.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING)
        return SpannedString(printText)
    }

    private fun getLocalMedia() {
        val gson = Gson()
        character.series = gson.fromJson(character.seriesJson, SeriesList::class.java)
        character.stories = gson.fromJson(character.storiesJson, StoryList::class.java)
        character.events = gson.fromJson(character.eventsJson, EventList::class.java)
        character.comics = gson.fromJson(character.comicsJson, ComicList::class.java)
    }

    private fun getMedia(list : AbstractMediaList, mediaType : String) : Int {
        val limit = if (list.available > 50) 50 else list.available
        if (limit != 0)
            fetchMedia(list.elements, mediaType, limit)
        return limit
    }

    private fun fetchMedia(list : ArrayList<String>, mediaType : String, limit : Int) {
        val url = apiController.createRequestURL("/${character.id}/${mediaType}?limit=${limit}&")

        apiController.asyncGet(url, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Connection cannot be established. Please reconnect and restart the app")
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
                                    // println("-----${mediaType} added-----")
                                    while (reader.hasNext()) {
                                        reader.beginObject()
                                        while (reader.hasNext()) {
                                            val nameInResults = reader.nextName()
                                            if (nameInResults.equals("title")) {
                                                list.add(reader.nextString())
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
