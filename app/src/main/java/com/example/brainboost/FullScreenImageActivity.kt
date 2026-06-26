package com.example.brainboost

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        val fullImageView = findViewById<ImageView>(R.id.fullImageView)

        val imageUri = intent.getStringExtra("imageUri")
        if (imageUri != null) {
            Picasso.get()
                .load(Uri.parse(imageUri))
                .fit()
                .centerInside()
                .into(fullImageView)
        }

        fullImageView.setOnClickListener {
            finish() // close on tap
        }
    }
}
