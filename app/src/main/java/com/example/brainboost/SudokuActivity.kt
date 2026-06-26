package com.example.brainboost

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SudokuActivity : AppCompatActivity() {

    private lateinit var sudokuView: SudokuView
    private lateinit var tvTimer: TextView

    private lateinit var resultLayout: LinearLayout
    private lateinit var tvFinalTime: TextView
    private lateinit var btnRestart: Button
    private lateinit var btnExit: Button
    private lateinit var clearButton: Button

    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())

    private var timerRunning = false
    private var timerStarted = false
    private var gameFinished = false

    private val numberButtons = mutableListOf<Button>()

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!timerRunning) return

            seconds++
            val min = seconds / 60
            val sec = seconds % 60
            tvTimer.text = String.format("⏱ %02d:%02d", min, sec)

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)

        tvTimer = findViewById(R.id.tvTimer)
        sudokuView = findViewById(R.id.sudokuView)
        sudokuView.setOnWinListener {
            showWinDialog()
        }
        resultLayout = findViewById(R.id.resultLayout)
        tvFinalTime = findViewById(R.id.tvFinalTime)
        btnRestart = findViewById(R.id.btnRestart)
        btnExit = findViewById(R.id.btnExit)

        setupNumberButtons()
        setupClearButton()
    }

    // ▶️ START TIMER (only once)
    private fun startTimer() {
        if (timerRunning || gameFinished) return

        timerRunning = true
        handler.removeCallbacks(timerRunnable)
        handler.post(timerRunnable)
    }

    // ⏸ STOP TIMER
    private fun stopTimer() {
        timerRunning = false
        handler.removeCallbacks(timerRunnable)
    }

    override fun onResume() {
        super.onResume()
        if (timerStarted && !gameFinished) {
            startTimer()
        }
    }

    // 🔢 NUMBER PAD
    private fun setupNumberButtons() {
        val ids = listOf(
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9
        )

        ids.forEachIndexed { index, id ->
            val button = findViewById<Button>(id)
            numberButtons.add(button)

            button.setOnClickListener {
                if (gameFinished) return@setOnClickListener

                // ▶️ Start timer on FIRST move
                if (!timerStarted) {
                    timerStarted = true
                    startTimer()
                }

                sudokuView.setNumber(index + 1)
            }
        }
    }

    // ❌ CLEAR BUTTON
    private fun setupClearButton() {
        clearButton = findViewById(R.id.btnClear)
        clearButton.setOnClickListener {
            if (!gameFinished) {
                sudokuView.clearCell()
            }
        }
    }
    private fun playWinSound() {
        val mp = android.media.MediaPlayer.create(this, R.raw.win)
        mp.setOnCompletionListener { it.release() }
        mp.start()
    }
    // 🎉 WIN DIALOG (CRASH SAFE)
    private fun showWinDialog() {

        if (gameFinished) return

        gameFinished = true
        playWinSound()
        stopTimer()
        disableControls()
        sudokuView.isEnabled = false

        resultLayout.visibility = View.VISIBLE
        sudokuView.visibility = View.GONE

        tvFinalTime.text = "Time: %02d:%02d".format(
            seconds / 60,
            seconds % 60
        )

        btnRestart.setOnClickListener {
            recreate()
        }

        btnExit.setOnClickListener {
            finish()
        }
    }

    private fun disableControls() {
        numberButtons.forEach { it.isEnabled = false }
        clearButton.isEnabled = false
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}
