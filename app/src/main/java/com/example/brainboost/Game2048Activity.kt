package com.example.brainboost

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Game2048Activity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var gameView: Game2048View

    private var bestScore = 0

    // 🔊 sounds (BrainBoost polish)
    private var winPlayer: MediaPlayer? = null
    private var movePlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_2048)

        tvScore = findViewById(R.id.tvScore)
        gameView = findViewById(R.id.gameView)

        // ⭐ sound init
        winPlayer = MediaPlayer.create(this, R.raw.win)
        movePlayer = MediaPlayer.create(this, R.raw.fail) // use soft tap sound here

        setupGame()
    }

    private fun setupGame() {

        gameView.setScoreListener { score ->

            tvScore.text = "Score: $score"

            // ⭐ score pulse animation (premium feel)
            tvScore.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(80)
                .withEndAction {
                    tvScore.animate().scaleX(1f).scaleY(1f).duration = 80
                }

            // ⭐ best score tracking
            if (score > bestScore) {
                bestScore = score
            }

            // 🔊 subtle move sound
            movePlayer?.start()
        }

        // ⭐ WIN listener (agar tumhare Game2048View me available ho)

    }

    override fun onDestroy() {
        super.onDestroy()
        winPlayer?.release()
        movePlayer?.release()
    }
}