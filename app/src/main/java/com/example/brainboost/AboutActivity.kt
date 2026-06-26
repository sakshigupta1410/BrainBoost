package com.example.brainboost

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.brainboost.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // No need to set text — XML already has full content
    }
}
