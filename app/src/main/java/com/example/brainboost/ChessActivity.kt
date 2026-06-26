package com.example.brainboost

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ChessActivity : AppCompatActivity() {

    private lateinit var tvTurn: TextView
    private lateinit var board: ChessBoardView
    private lateinit var topCaptured: LinearLayout
    private lateinit var bottomCaptured: LinearLayout

    private var gameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess)

        board = findViewById(R.id.chessBoardView)
        tvTurn = findViewById(R.id.tvTurn)
        topCaptured = findViewById(R.id.topCaptured)
        bottomCaptured = findViewById(R.id.bottomCaptured)

        startNewGame()

        board.onPawnPromotion = { r, c ->
            showPromotionDialog(r, c)
        }

        board.onMovePlayed = {
            updateTurn()
            updateCapturedUI()
            checkGameState()
        }

        findViewById<Button>(R.id.btnUndo).setOnClickListener {
            if (!gameOver) {
                ChessEngine.undo()
                board.invalidate()
                updateTurn()
                updateCapturedUI()
            }
        }

        findViewById<Button>(R.id.btnRestart).setOnClickListener {
            startNewGame()
        }
    }

    // ---------- NEW GAME ----------
    private fun startNewGame() {
        ChessEngine.initBoard()
        gameOver = false
        board.invalidate()
        updateTurn()
        updateCapturedUI()
    }

    // ---------- TURN INDICATOR ----------
    private fun updateTurn() {

        if (ChessEngine.whiteTurn) {

            tvTurn.text = "White's Turn"
            tvTurn.setBackgroundColor(Color.WHITE)
            tvTurn.setTextColor(Color.BLACK)

        } else {

            tvTurn.text = "Black's Turn"
            tvTurn.setBackgroundColor(Color.BLACK)
            tvTurn.setTextColor(Color.WHITE)

        }
    }

    // ---------- CAPTURED PIECES ----------
    private fun updateCapturedUI() {

        topCaptured.removeAllViews()
        bottomCaptured.removeAllViews()

        for (move in ChessEngine.getHistory()) {

            move.captured?.let { piece ->

                val img = ImageView(this)
                img.setImageResource(piece.drawable)

                val size = (28 * resources.displayMetrics.density).toInt()
                img.layoutParams = LinearLayout.LayoutParams(size, size)

                if (piece.color == PieceColor.WHITE)
                    bottomCaptured.addView(img)
                else
                    topCaptured.addView(img)
            }
        }
    }

    // ---------- GAME STATE CHECK ----------
    private fun checkGameState() {

        if (gameOver) return

        if (ChessEngine.isCheckmate(PieceColor.WHITE)) {

            gameOver = true
            
            showDialog("CHECKMATE ♚", "Black Wins!")
            return
        }

        if (ChessEngine.isCheckmate(PieceColor.BLACK)) {

            gameOver = true
            showDialog("CHECKMATE ♚", "White Wins!")
        }

    }

    // ---------- PAWN PROMOTION ----------
    private fun showPromotionDialog(r: Int, c: Int) {

        val items = arrayOf("Queen", "Rook", "Bishop", "Knight")

        AlertDialog.Builder(this)
            .setTitle("Promote Pawn ♟️")
            .setItems(items) { _, i ->

                val type = when (i) {
                    0 -> PieceType.QUEEN
                    1 -> PieceType.ROOK
                    2 -> PieceType.BISHOP
                    else -> PieceType.KNIGHT
                }

                ChessEngine.promotePawn(r, c, type)
                board.invalidate()
            }
            .setCancelable(false)
            .show()
    }

    // ---------- RESULT DIALOG ----------
    private fun showDialog(title: String, msg: String) {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("Restart") { _, _ ->
                startNewGame()
            }
            .setCancelable(false)
            .show()
    }
}
