package com.talhakumru.marvelcomicsapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerListAdapter(private val characterList : ArrayList<String>, private val pictureList : ArrayList<Int>, private val mode : Int) : RecyclerView.Adapter<RecyclerListAdapter.CharacterCardVH>() {

    class CharacterCardVH(itemView : View, mode: Int) : RecyclerView.ViewHolder(itemView) {
        val nameTextView : TextView
        val seriesTextView : TextView
        val imageView : ImageView

        init {
            when (mode) {
                0 -> {
                    nameTextView = itemView.findViewById(R.id.nameTextView)
                    seriesTextView = itemView.findViewById(R.id.seriesTextView)
                    imageView = itemView.findViewById(R.id.imageView)
                }
                else -> {
                    nameTextView = itemView.findViewById(R.id.nameGridTextView)
                    seriesTextView = itemView.findViewById(R.id.seriesGridTextView)
                    imageView = itemView.findViewById(R.id.imageGridView)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterCardVH {
        val itemView : View
        if(mode == 0) {
            //println("changed to list layout")
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.character_list_card, parent, false)
        } else {
            //println("changed to grid layout")
            itemView = LayoutInflater.from(parent.context).inflate(R.layout.character_grid_card, parent, false)
        }
        return CharacterCardVH(itemView, mode)
    }

    override fun getItemCount(): Int {
        return characterList.size
    }

    override fun onBindViewHolder(holder: CharacterCardVH, position: Int) {
        holder.nameTextView.text = characterList[position]
        holder.seriesTextView.text = "Dizi sayisi: ${position}"
        holder.imageView.setImageResource(pictureList[position])
        holder.itemView.setOnClickListener {
            //Toast.makeText(holder.itemView.context,"${characterList[position]} tiklandi!", Toast.LENGTH_SHORT).show()
            val intent = Intent(holder.itemView.context,DetailsActivity::class.java)
            intent.putExtra("heroName", characterList[position])
            intent.putExtra("heroFigure", pictureList[position])
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun onViewRecycled(holder: CharacterCardVH) {
        println("view changed to: ${holder.itemView}")
        super.onViewRecycled(holder)
    }
}