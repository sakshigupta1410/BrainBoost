package com.example.brainboost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WordSearchMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_search_menu)

        findViewById<Button>(R.id.btnEasy).setOnClickListener {
            openGame(6, "Easy")
        }

        findViewById<Button>(R.id.btnMedium).setOnClickListener {
            openGame(8, "Medium")
        }

        findViewById<Button>(R.id.btnHard).setOnClickListener {
            openGame(10, "Hard")
        }
    }

    private fun openGame(size: Int, level: String) {
        val intent = Intent(this, WordSearchActivity::class.java)
        intent.putExtra("GRID_SIZE", size)
        intent.putExtra("LEVEL", level)
        startActivity(intent)
    }
}
