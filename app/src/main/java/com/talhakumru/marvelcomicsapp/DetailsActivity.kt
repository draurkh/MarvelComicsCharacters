package com.talhakumru.marvelcomicsapp

import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.gson.Gson
import com.talhakumru.marvelcomicsapp.marvel_data.Character
import com.talhakumru.marvelcomicsapp.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    private lateinit var character : Character
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // set toolbar as appbar of the activity
        setSupportActionBar(findViewById(R.id.detailsToolbar))
        val appBar = supportActionBar

        val intent = intent
        val gson = Gson()
        character = gson.fromJson(intent.getStringExtra("characterData"),Character::class.java)



        var seriesList = "<h2><b>Series</b></h2>"
        var storiesList = "<h2><b>Stories</b></h2>"
        var eventsList = "<h2><b>Events</b></h2>"
        var comicsList = "<h2><b>Comics</b></h2>"
/*
        if (character.series.items.isEmpty()) seriesList = seriesList.plus("<p>This character is not included in any series.</p>")
        if (character.stories.items.isEmpty()) storiesList = storiesList.plus("<p>This character is not included in any stories.</p>")
        if (character.events.items.isEmpty()) eventsList = eventsList.plus("<p>This character is not included in any events.</p>")
        if (character.comics.items.isEmpty()) comicsList = comicsList.plus("<p>This character is not included in any comics.</p>")

        println(seriesList)
        println("there are ${character.series.items.size} items in series")
        for (series in character.series.items) {
            seriesList = seriesList.plus("<ul>${series.name}<ul>")
        }
        for(story in character.stories.items) {
            storiesList = storiesList.plus("<ul>${story.name}<ul>")
        }
        for(event in character.events.items) {
            eventsList = eventsList.plus("<ul>${event.name}<ul>")
        }
        for(comic in character.comics.items) {
            comicsList = comicsList.plus("<ul>${comic.name}<ul>")
        }

        val listText = Html.fromHtml(seriesList+storiesList+eventsList+comicsList, Html.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING)
*/
        binding.detailsNameView.text = character.name
        binding.detailsImageView.load("${character.thumbnail.path}.${character.thumbnail.extension}")
        //binding.detailsTextView.text = listText
        println("${character.id}: ${character.isFavourite}")
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

    fun getMedia() {

    }
}