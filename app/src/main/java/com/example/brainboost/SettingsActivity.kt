package com.example.brainboost

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brainboost.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Notification click
        binding.layoutNotification.setOnClickListener {
            Toast.makeText(this, "Coming soon 🔔", Toast.LENGTH_SHORT).show()
        }
    }
}
