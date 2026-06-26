package com.example.brainboost

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MazeGameActivity : AppCompatActivity() {

    private lateinit var mazeView: MazeView
    private lateinit var tvTimer: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvScore: TextView

    private var seconds = 0
    private var level = 1
    private var score = 0

    private val handler = Handler(Looper.getMainLooper())

    private val timerRunnable = object : Runnable {
        override fun run() {
            seconds++
            tvTimer.text = "Time: $seconds s"
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maze_game)

        mazeView = findViewById(R.id.mazeView)
        tvTimer = findViewById(R.id.tvTimer)
        tvLevel = findViewById(R.id.tvLevel)
        tvScore = findViewById(R.id.tvScore)

        updateUI()
        handler.post(timerRunnable)
    }

    private fun updateUI() {
        tvLevel.text = "Level: $level"
        tvScore.text = "Score: $score"
    }
    private fun playWinSound() {
        val mp = android.media.MediaPlayer.create(this, R.raw.win)
        mp.setOnCompletionListener { it.release() }
        mp.start()
    }


    fun onLevelCompleted() {
        playWinSound()
        handler.removeCallbacks(timerRunnable)

        val earned = 10
        score += earned

        AlertDialog.Builder(this)
            .setTitle("LEVEL COMPLETED")
            .setMessage("Your Total Score: $score")
            .setCancelable(false)
            .setPositiveButton("Next Level") { _, _ ->
                level++
                seconds = 0
                mazeView.nextLevel(level)
                updateUI()
                handler.post(timerRunnable)
            }
            .show()
    }

    fun resetGame() {
        handler.removeCallbacks(timerRunnable)

        seconds = 0
        level = 1
        score = 0

        mazeView.resetGame()
        updateUI()

        handler.post(timerRunnable)
    }

    fun onMazeGameOver() {
        handler.removeCallbacks(timerRunnable)

        AlertDialog.Builder(this)
            .setTitle("OUT!")
            .setMessage("You reached a dead end")
            .setCancelable(false)
            .setPositiveButton("Retry") { _, _ ->
                resetGame()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
    }
}