package com.example.brainboost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PuzzleMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle_menu)

        val btn8Puzzle = findViewById<Button>(R.id.btn8Puzzle)
        val btn15Puzzle = findViewById<Button>(R.id.btn15Puzzle)

        btn8Puzzle.setOnClickListener {
            startActivity(Intent(this, PuzzleGameActivity::class.java))
        }

        btn15Puzzle.setOnClickListener {
            startActivity(Intent(this, Puzzle15Activity::class.java))
        }
    }
}
