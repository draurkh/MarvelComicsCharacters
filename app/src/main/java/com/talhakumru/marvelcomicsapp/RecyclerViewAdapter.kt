package com.talhakumru.marvelcomicsapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.gson.Gson
import com.talhakumru.marvelcomicsapp.local_data.tables.Character
import com.talhakumru.marvelcomicsapp.local_data.CharacterViewModel
import com.talhakumru.marvelcomicsapp.local_data.tables.Favourite

class RecyclerViewAdapter(private val layoutManager : GridLayoutManager, private val viewModel : CharacterViewModel) : RecyclerView.Adapter<RecyclerViewAdapter.CharacterCardVH>() {

    private var localList = emptyList<Character>()         // first shown list
    private var onlineList = emptyList<Character>()        // last shown list

    class CharacterCardVH(itemView : View, spanCount : Int) : RecyclerView.ViewHolder(itemView) {
        val nameTextView : TextView
        val seriesTextView : TextView
        val imageView : ImageView
        val starView : ImageView

        init {
            when (spanCount) {
                1 -> {
                    // listview
                    nameTextView = itemView.findViewById(R.id.nameTextView)
                    seriesTextView = itemView.findViewById(R.id.seriesTextView)
                    imageView = itemView.findViewById(R.id.imageView)
                    starView = itemView.findViewById(R.id.starView)
                }
                else -> {
                    // gridview
                    nameTextView = itemView.findViewById(R.id.nameGridTextView)
                    seriesTextView = itemView.findViewById(R.id.seriesGridTextView)
                    imageView = itemView.findViewById(R.id.imageGridView)
                    starView = itemView.findViewById(R.id.starGridView)
                }
            }
        }
        fun getContext() : Context {
            return itemView.context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterCardVH {
        val itemView : View = if(layoutManager.spanCount == 1) {
            //println("changed to list layout")
            LayoutInflater.from(parent.context).inflate(R.layout.character_list_card, parent, false)
        } else {
            //println("changed to grid layout")
            LayoutInflater.from(parent.context).inflate(R.layout.character_grid_card, parent, false)
        }

        return CharacterCardVH(itemView, layoutManager.spanCount)
    }

    override fun getItemCount(): Int {
        return onlineList.size + localList.size
    }

    override fun onBindViewHolder(holder: CharacterCardVH, position: Int) {
        //println("View Position: ${position}")
        //println("View Local Size: ${localList.size}")
        val character : Character
        val imageURL : String
        val numOfSeries : Int
        val onlinePos = position - localList.size

        if (position < localList.size) {
            // character is locally stored
            character = localList[position]
            numOfSeries = character.numOfSeries
            imageURL = character.imageURL
            // local data is colored different then online data
            holder.itemView.setBackgroundColor(holder.getContext().getColor(R.color.localData))
        } else {
            // character is online
            character = onlineList[onlinePos]
            numOfSeries = character.series.available
            imageURL = "${character.thumbnail.path}/standard_fantastic.${character.thumbnail.extension}"
            holder.itemView.setBackgroundColor(holder.getContext().getColor(R.color.onlineData))
        }

        holder.nameTextView.text = character.name
        holder.imageView.load(imageURL) { this.crossfade(true) }
        holder.seriesTextView.text = holder.getContext().getString(R.string.series_num, numOfSeries)

        val favData = viewModel.isFavourite(character.id)

        val isFavourite = favData != null && favData.isFavourite == 1

        // change the image of star view
        if (isFavourite) holder.starView.setImageResource(R.drawable.star_filled)
        else holder.starView.setImageResource(R.drawable.star_hollow)

        // navigate to details activity on clicked
        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, DetailsActivity::class.java)
            val gson = Gson()
            val favJson : String = if (favData != null) gson.toJson(favData)
            else gson.toJson(Favourite(character.id, 0, 0))

            intent.putExtra("favData", favJson)
            intent.putExtra("position", position - localList.size)
            intent.putExtra("id", character.id)

            view.context.startActivity(intent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return layoutManager.spanCount
    }

    fun setLocalList(characters : List<Character>) {
        this.localList = characters
        notifyDataSetChanged()
    }

    fun setOnlineList(characters : List<Character>) {
        this.onlineList = characters
        notifyDataSetChanged()
    }

}