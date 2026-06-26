package com.example.brainboost

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlin.math.abs
import kotlin.random.Random
import android.util.AttributeSet

class Game2048View @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val gridSize = 4
    private val board = Array(gridSize) { IntArray(gridSize) }
    private var cellSize = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var startX = 0f
    private var startY = 0f
    private var score = 0

    private var scoreListener: ((Int) -> Unit)? = null
    private var moveSound: (() -> Unit)? = null

    // ⭐ spawn animation helper
    private var lastSpawn: Pair<Int, Int>? = null

    init {
        resetGame()
    }

    fun setScoreListener(listener: (Int) -> Unit) {
        scoreListener = listener
    }

    fun setMoveSoundListener(listener: () -> Unit) {
        moveSound = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cellSize = width / gridSize.toFloat()

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                drawCell(canvas, r, c)
            }
        }
    }

    private fun drawCell(canvas: Canvas, row: Int, col: Int) {
        val value = board[row][col]

        paint.color = when (value) {
            0 -> Color.parseColor("#CDC1B4")
            2 -> Color.parseColor("#EEE4DA")
            4 -> Color.parseColor("#EDE0C8")
            8 -> Color.parseColor("#F2B179")
            16 -> Color.parseColor("#F59563")
            32 -> Color.parseColor("#F67C5F")
            64 -> Color.parseColor("#F65E3B")
            128 -> Color.parseColor("#EDCF72")
            256 -> Color.parseColor("#EDCC61")
            512 -> Color.parseColor("#EDC850")
            1024 -> Color.parseColor("#EDC53F")
            else -> Color.parseColor("#EDC22E")
        }

        val left = col * cellSize
        val top = row * cellSize

        // ⭐ pop animation on spawn
        if (lastSpawn == row to col) {
            canvas.save()
            canvas.scale(1.1f, 1.1f, left + cellSize / 2, top + cellSize / 2)
        }

        canvas.drawRect(left, top, left + cellSize - 8, top + cellSize - 8, paint)

        if (value != 0) {
            paint.color = Color.BLACK
            paint.textSize = cellSize / 3
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                value.toString(),
                left + cellSize / 2,
                top + cellSize / 1.6f,
                paint
            )
        }

        if (lastSpawn == row to col) {
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }

            MotionEvent.ACTION_UP -> {

                val dx = event.x - startX
                val dy = event.y - startY

                val moved = if (abs(dx) > abs(dy)) {
                    if (dx > 0) moveRight() else moveLeft()
                } else {
                    if (dy > 0) moveDown() else moveUp()
                }

                if (moved) {
                    moveSound?.invoke()
                    invalidate()
                }

                checkGameOver()
            }
        }
        return true
    }

    // ---------------- MOVES ----------------

    private fun moveLeft(): Boolean {
        var moved = false
        for (r in 0 until gridSize) {
            val old = board[r].copyOf()
            merge(board[r])
            if (!old.contentEquals(board[r])) moved = true
        }
        if (moved) addRandomTile()
        return moved
    }

    private fun moveRight(): Boolean {
        var moved = false
        for (r in 0 until gridSize) {
            val row = board[r].reversedArray()
            val old = row.copyOf()
            merge(row)
            if (!old.contentEquals(row)) moved = true
            board[r] = row.reversedArray()
        }
        if (moved) addRandomTile()
        return moved
    }

    private fun moveUp(): Boolean {
        var moved = false
        for (c in 0 until gridSize) {
            val col = IntArray(gridSize) { board[it][c] }
            val old = col.copyOf()
            merge(col)
            if (!old.contentEquals(col)) moved = true
            for (r in 0 until gridSize) board[r][c] = col[r]
        }
        if (moved) addRandomTile()
        return moved
    }

    private fun moveDown(): Boolean {
        var moved = false
        for (c in 0 until gridSize) {
            val col = IntArray(gridSize) { board[it][c] }.reversedArray()
            val old = col.copyOf()
            merge(col)
            if (!old.contentEquals(col)) moved = true
            val finalCol = col.reversedArray()
            for (r in 0 until gridSize) board[r][c] = finalCol[r]
        }
        if (moved) addRandomTile()
        return moved
    }

    // ---------------- MERGE ----------------

    private fun merge(line: IntArray) {
        val list = line.filter { it != 0 }.toMutableList()

        var i = 0
        while (i < list.size - 1) {
            if (list[i] == list[i + 1]) {
                list[i] *= 2

                // ⭐ WIN detection
                if (list[i] == 2048) showWinDialog()

                score += list[i]
                scoreListener?.invoke(score)

                list.removeAt(i + 1)
            }
            i++
        }

        for (i in line.indices) {
            line[i] = if (i < list.size) list[i] else 0
        }
    }

    // ---------------- RANDOM TILE ----------------

    private fun addRandomTile() {
        val empty = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until gridSize)
            for (c in 0 until gridSize)
                if (board[r][c] == 0) empty.add(r to c)

        if (empty.isNotEmpty()) {
            val (r, c) = empty.random()
            board[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
            lastSpawn = r to c
        }
    }

    // ---------------- GAME STATES ----------------

    private fun checkGameOver() {
        for (r in 0 until gridSize)
            for (c in 0 until gridSize)
                if (board[r][c] == 0) return

        AlertDialog.Builder(context)
            .setTitle("Game Over")
            .setMessage("Score: $score")
            .setPositiveButton("Restart") { _, _ -> resetGame() }
            .setCancelable(false)
            .show()
    }

    private fun showWinDialog() {
        AlertDialog.Builder(context)
            .setTitle("You Win! 🎉")
            .setMessage("Score: $score")
            .setPositiveButton("Continue", null)
            .show()
    }

    fun resetGame() {
        for (r in 0 until gridSize)
            for (c in 0 until gridSize)
                board[r][c] = 0

        score = 0
        scoreListener?.invoke(score)

        addRandomTile()
        addRandomTile()
        invalidate()
    }
}