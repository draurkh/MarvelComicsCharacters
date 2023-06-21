package com.talhakumru.marvelcomicsapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.gson.Gson
import com.talhakumru.marvelcomicsapp.marvel_data.Character

class RecyclerListAdapter(private val characters : ArrayList<Character>, private val mode : Int) : RecyclerView.Adapter<RecyclerListAdapter.CharacterCardVH>() {


    class CharacterCardVH(itemView : View, mode : Int) : RecyclerView.ViewHolder(itemView) {
        val nameTextView : TextView
        val seriesTextView : TextView
        val imageView : ImageView
        val starView : ImageView

        init {
            when (mode) {
                R.drawable.grid_layout -> {
                    nameTextView = itemView.findViewById(R.id.nameGridTextView)
                    seriesTextView = itemView.findViewById(R.id.seriesGridTextView)
                    imageView = itemView.findViewById(R.id.imageGridView)
                    starView = itemView.findViewById(R.id.starGridView)
                }
                else -> {
                    nameTextView = itemView.findViewById(R.id.nameTextView)
                    seriesTextView = itemView.findViewById(R.id.seriesTextView)
                    imageView = itemView.findViewById(R.id.imageView)
                    starView = itemView.findViewById(R.id.starView)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterCardVH {
        val itemView : View
        if(mode == R.drawable.list_layout) {
            //println("changed to list layout")
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.character_list_card, parent, false)
        } else {
            //println("changed to grid layout")
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.character_grid_card, parent, false)
        }

        return CharacterCardVH(itemView, mode)
    }

    override fun getItemCount(): Int {
        return characters.size
    }

    override fun onBindViewHolder(holder: CharacterCardVH, position: Int) {
        //println("View Position: ${position}")

        holder.nameTextView.text = characters[position].name
        holder.seriesTextView.text = "${characters[position].series.available} Series"

        /*
        val isFavourite = favouriteDao.isFavourite(characters[position].id)
        if (isFavourite == 1) holder.starView.setImageResource(R.drawable.star_filled)
        else holder.starView.setImageResource(R.drawable.star_hollow)
*/
        /*
        // check if the character is favourite
        if (characters[position].isFavourite) holder.starView.setImageResource(R.drawable.star_filled)
        else {
            try {
                val fDatabase : SQLiteDatabase = openOrCreateDatabase("/data/data/com.talhakumru.marvelcomics/databases", null)
                fDatabase.execSQL("CREATE TABLE IF NOT EXISTS favourites (id INTEGER PRIMARY KEY ASC, is_favourite INT);")
                val cursor : Cursor = fDatabase.rawQuery("SELECT is_favourite FROM favourites WHERE id IS ?", arrayOf(characters[position].id.toString()))
                cursor.moveToFirst()
                characters[position].isFavourite = !cursor.isNull(0) && cursor.getInt(0) == 1
                cursor.close()
                if (characters[position].isFavourite) holder.starView.setImageResource(R.drawable.star_filled)
                else holder.starView.setImageResource(R.drawable.star_hollow)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
*/

        val image = characters[position].thumbnail
        holder.imageView.load("${image.path}.${image.extension}") { this.crossfade(true) }


        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,DetailsActivity::class.java)
            val gson = Gson()
            val json = gson.toJson(characters[position])
            intent.putExtra("characterData", json)

            holder.itemView.context.startActivity(intent)
        }
    }
}