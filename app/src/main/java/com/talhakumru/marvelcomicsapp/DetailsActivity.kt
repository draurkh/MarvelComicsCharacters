package com.talhakumru.marvelcomicsapp

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import com.talhakumru.marvelcomicsapp.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // set toolbar as appbar of the activity
        setSupportActionBar(findViewById(R.id.detailsToolbar))
        val appBar = supportActionBar

        val intent = intent
        val heroName = intent.getStringExtra("heroName")
        val heroFigure = intent.getIntExtra("heroFigure",R.drawable.batman)

        binding.detailsNameView.text = heroName
        binding.detailsImageView.setImageResource(heroFigure)
        binding.detailsTextView.text = heroName.plus(" detay sayfasıdır. Bu kahramanın özellikleri, bu sayfada yer alacaktır. Bu veriler Marvel API ile internetten çekilecektir.")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}