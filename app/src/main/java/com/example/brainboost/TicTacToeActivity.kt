package com.example.brainboost

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TicTacToeActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var resetBtn: Button

    private lateinit var buttons: Array<Array<Button>>

    private var playerXTurn = true
    private var gameActive = true

    private var board = Array(3) { Array(3) { "" } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tic_tac_toe)

        statusText = findViewById(R.id.tvStatus)
        resetBtn = findViewById(R.id.btnReset)

        buttons = arrayOf(
            arrayOf(
                findViewById(R.id.btn00),
                findViewById(R.id.btn01),
                findViewById(R.id.btn02)
            ),
            arrayOf(
                findViewById(R.id.btn10),
                findViewById(R.id.btn11),
                findViewById(R.id.btn12)
            ),
            arrayOf(
                findViewById(R.id.btn20),
                findViewById(R.id.btn21),
                findViewById(R.id.btn22)
            )
        )

        for (i in 0..2) {
            for (j in 0..2) {
                buttons[i][j].setOnClickListener { onButtonClick(it, i, j) }
            }
        }

        resetBtn.setOnClickListener { resetGame() }

        updateStatus()
    }

    private fun onButtonClick(view: View, row: Int, col: Int) {
        if (!gameActive) return
        if (!view.isEnabled) return

        view.isEnabled = false
        if (board[row][col].isNotEmpty()) return

        board[row][col] = if (playerXTurn) "X" else "O"
        (view as Button).text = board[row][col]
        view.setTextColor(if (playerXTurn) Color.parseColor("#FF5252") else Color.parseColor("#2196F3"))
        view.textSize = 30f
        view.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(70)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(70).start()
            }
            .start()

        if (checkWin()) {
            playWinSound()
            statusText.text = "${board[row][col]} Wins! 🎉"
            gameActive = false
            return
        }

        if (isBoardFull()) {
            playFailSound()
            statusText.text = "It's a Draw 😐"
            gameActive = false
            return
        }

        playerXTurn = !playerXTurn
        updateStatus()
    }
    private fun playWinSound() {
        val mp = android.media.MediaPlayer.create(this, R.raw.win)
        mp.setOnCompletionListener { it.release() }
        mp.start()
    }

    private fun playFailSound() {
        val mp = android.media.MediaPlayer.create(this, R.raw.fail)
        mp.setOnCompletionListener { it.release() }
        mp.start()
    }
    private fun updateStatus() {

        statusText.text = if (playerXTurn) "X's Turn" else "O's Turn"

        statusText.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(120)
            .withEndAction {
                statusText.animate().scaleX(1f).scaleY(1f).duration = 120
            }
    }

    private fun checkWin(): Boolean {
        val winPatterns = arrayOf(
            // Horizontal
            arrayOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
            arrayOf(Pair(1, 0), Pair(1, 1), Pair(1, 2)),
            arrayOf(Pair(2, 0), Pair(2, 1), Pair(2, 2)),
            // Vertical
            arrayOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
            arrayOf(Pair(0, 1), Pair(1, 1), Pair(2, 1)),
            arrayOf(Pair(0, 2), Pair(1, 2), Pair(2, 2)),
            // Diagonal
            arrayOf(Pair(0, 0), Pair(1, 1), Pair(2, 2)),
            arrayOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
        )

        for (pattern in winPatterns) {
            val (a1, b1) = pattern[0]
            val (a2, b2) = pattern[1]
            val (a3, b3) = pattern[2]

            if (board[a1][b1].isNotEmpty() &&
                board[a1][b1] == board[a2][b2] &&
                board[a1][b1] == board[a3][b3]
            ) {
                highlightWinningButtons(pattern)
                return true
            }
        }
        return false
    }

    private fun highlightWinningButtons(pattern: Array<Pair<Int, Int>>) {
        for ((r, c) in pattern) {
            buttons[r][c].setBackgroundColor(Color.parseColor("#A5D6A7"))
        }
    }

    private fun isBoardFull(): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j].isEmpty()) return false
            }
        }
        return true
    }

    private fun resetGame() {

        gameActive = true
        playerXTurn = true
        board = Array(3) { Array(3) { "" } }

        for (i in 0..2) {
            for (j in 0..2) {
                buttons[i][j].isEnabled = true
                buttons[i][j].text = ""
                buttons[i][j].setBackgroundColor(Color.WHITE)
            }
        }

        updateStatus()
    }
}
