package com.talhakumru.marvelcomicsapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.SpannedString
import android.util.JsonReader
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.gson.Gson
import com.talhakumru.marvelcomicsapp.databinding.ActivityDetailsBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException
import com.talhakumru.marvelcomicsapp.local_data.Character

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    private lateinit var character : Character
    private var position : Int = 0
    private val apiController = MarvelAPIController()
    lateinit var mainHandler : Handler
    private val updateTask = object : Runnable {
        override fun run() {
            listingTask()
            mainHandler.postDelayed(this, 100)
        }
    }
    var runOnce = true
    var seriesLimit = 0
    var storiesLimit = 0
    var eventsLimit = 0
    var comicsLimit = 0
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
        //val gson = Gson()
        //character = gson.fromJson(intent.getStringExtra("characterData"), Character::class.java)
        position = intent.getIntExtra("position", -1)
        if (position == -1) throw java.io.IOException("Invalid position passed to Details activity")
        character = MarvelAPIController.dataWrapper.data.results[position]

        seriesLimit = if (character.series.available > 100) 100 else character.series.available
        storiesLimit = if (character.stories.available > 100) 100 else character.stories.available
        eventsLimit = if (character.events.available > 100) 100 else character.events.available
        comicsLimit = if (character.comics.available > 100) 100 else character.comics.available

        getMedia(character.series.list, "series", seriesLimit)
        getMedia(character.stories.list, "stories", storiesLimit)
        getMedia(character.events.list, "events", eventsLimit)
        getMedia(character.comics.list, "comics", comicsLimit)

        mainHandler = Handler(Looper.getMainLooper())

        binding.detailsNameView.text = character.name
        binding.detailsImageView.load("${character.thumbnail.path}.${character.thumbnail.extension}")
        println("${character.id}: ${character.isFavourite}")
        println("DetailsActivity onCreate is exited")
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favoriButonu -> {
                println("FAVORI CONTEXT ITEM SECILDI")
                /*try {
                    val fDatabase : SQLiteDatabase = openOrCreateDatabase("Favourites", MODE_PRIVATE, null)
                    fDatabase.execSQL("CREATE TABLE IF NOT EXISTS favourites (id INTEGER PRIMARY KEY ASC, is_favourite INT);")
                    val isFavourite : Int = if (character.isFavourite) 1 else 0
                    when (isFavourite) {
                        // character is not favourite, then add to favourites
                        0 -> {
                            character.isFavourite = true
                            fDatabase.execSQL("INSERT OR REPLACE INTO favourites VALUES (?, ?);", arrayOf(character.id, isFavourite))
                            val cursor : Cursor = fDatabase.rawQuery("SELECT * FROM favourites;", null)
                            cursor.moveToFirst()
                            while (!cursor.isAfterLast) {
                                val rowID = cursor.getInt(0)
                                val rowFav = cursor.getInt(1)
                                println("$rowID: $rowFav")
                                cursor.moveToNext()
                            }
                            cursor.close()
                            item.title = getString(R.string.rm_fav)
                        }
                        // character is favourite, then remove from favourites
                        1 -> {
                            character.isFavourite = false
                            fDatabase.execSQL("DELETE FROM favourites WHERE id IS ?", arrayOf(character.id))
                            val cursor : Cursor = fDatabase.rawQuery("SELECT * FROM favourites;", null)
                            cursor.moveToFirst()
                            while (!cursor.isAfterLast) {
                                val rowID = cursor.getInt(0)
                                val rowFav = cursor.getInt(1)
                                println("$rowID: $rowFav")
                                cursor.moveToNext()
                            }
                            cursor.close()
                            item.title = getString(R.string.add_fav)
                        }
                    }
                    fDatabase.close()
                } catch (e : Exception) {
                    e.printStackTrace()
                }*/
            }
            R.id.yerelButonu -> {
                println("YEREL CONTEXT ITEM SECILDI")
            }
        }
        return super.onContextItemSelected(item)
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
        if (runOnce && character.series.list.size == seriesLimit &&
                    character.stories.list.size == storiesLimit &&
                    character.events.list.size == eventsLimit &&
                    character.comics.list.size == comicsLimit)
        {
            println("*************entered listingTask")
            binding.detailsTextView.text = printResultsOnScreen()
            runOnce = false
        }
    }

    fun createFormattedText(heading : String, list : ArrayList<String>) : String {
        var text = "<h2><b>${heading}</b></h2>"
        if (list.isEmpty()) text = text.plus("<p>This character is not included in any series.</p>")
        //println(text)
        //println("there are ${character.series.list.size} items in series")
        for (title in list) {
            text = text.plus("<ul>${title}<ul>")
        }
        return text
    }

    fun printResultsOnScreen() : SpannedString {
        val seriesText = createFormattedText("Series", character.series.list)
        val storiesText = createFormattedText("Stories", character.stories.list)
        val eventsText = createFormattedText("Events", character.events.list)
        val comicsText = createFormattedText("Comics", character.comics.list)

        val listText = Html.fromHtml(seriesText + storiesText + eventsText + comicsText, Html.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING)
        return SpannedString(listText)
    }

    fun getMedia(list : ArrayList<String>, mediaType : String, limit : Int) {
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
                                    println("-----added-----")
                                    while (reader.hasNext()) {
                                        reader.beginObject()
                                        while (reader.hasNext()) {
                                            val nameInResults = reader.nextName()
                                            if (nameInResults.equals("title")) {
                                                val title = reader.nextString()
                                                println(title)
                                                list.add(title)
                                            } else reader.skipValue()
                                        }
                                        reader.endObject()
                                    }
                                    reader.endArray()
                                } else reader.skipValue()
                            }
                            reader.endObject()
                        } else reader.skipValue()
                    }
                    reader.endObject()
                    reader.close()
                }
            }

        })
    }

}
