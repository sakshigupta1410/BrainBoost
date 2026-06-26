package com.example.brainboost

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class MazeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var rows = 6
    private var cols = 6

    private lateinit var grid: Array<Array<Cell>>
    private var cellSize = 0f

    private var playerRow = 0
    private var playerCol = 0

    private var goalRow = rows - 1
    private var goalCol = cols - 1

    private var startX = 0f
    private var startY = 0f
    private val swipeThreshold = 50

    private val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 6f
    }

    private val bgPaint = Paint().apply { color = Color.BLACK }

    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
    }

    private val goalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
    }

    data class Cell(
        var top: Boolean = true,
        var right: Boolean = true,
        var bottom: Boolean = true,
        var left: Boolean = true,
        var visited: Boolean = false
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cellSize = (w.coerceAtMost(h)) / cols.toFloat()
        generateMaze()
    }

    fun nextLevel(currentLevel: Int) {
        rows = 6 + currentLevel
        cols = 6 + currentLevel
        goalRow = rows - 1
        goalCol = cols - 1
        cellSize = width.coerceAtMost(height) / cols.toFloat()
        generateMaze()
    }

    fun resetGame() {
        rows = 6
        cols = 6
        goalRow = rows - 1
        goalCol = cols - 1
        cellSize = width.coerceAtMost(height) / cols.toFloat()
        generateMaze()
    }

    private fun generateMaze() {
        grid = Array(rows) { Array(cols) { Cell() } }
        dfs(0, 0)
        playerRow = 0
        playerCol = 0
        invalidate()
    }

    private fun dfs(r: Int, c: Int) {
        grid[r][c].visited = true
        val dirs = listOf(0, 1, 2, 3).shuffled()

        for (d in dirs) {
            val nr = r + if (d == 0) -1 else if (d == 2) 1 else 0
            val nc = c + if (d == 1) 1 else if (d == 3) -1 else 0

            if (nr in 0 until rows && nc in 0 until cols && !grid[nr][nc].visited) {
                when (d) {
                    0 -> { grid[r][c].top = false; grid[nr][nc].bottom = false }
                    1 -> { grid[r][c].right = false; grid[nr][nc].left = false }
                    2 -> { grid[r][c].bottom = false; grid[nr][nc].top = false }
                    3 -> { grid[r][c].left = false; grid[nr][nc].right = false }
                }
                dfs(nr, nc)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = c * cellSize
                val y = r * cellSize
                val cell = grid[r][c]

                if (cell.top) canvas.drawLine(x, y, x + cellSize, y, wallPaint)
                if (cell.right) canvas.drawLine(x + cellSize, y, x + cellSize, y + cellSize, wallPaint)
                if (cell.bottom) canvas.drawLine(x, y + cellSize, x + cellSize, y + cellSize, wallPaint)
                if (cell.left) canvas.drawLine(x, y, x, y + cellSize, wallPaint)
            }
        }

        canvas.drawCircle(
            goalCol * cellSize + cellSize / 2,
            goalRow * cellSize + cellSize / 2,
            cellSize / 4,
            goalPaint
        )

        canvas.drawCircle(
            playerCol * cellSize + cellSize / 2,
            playerRow * cellSize + cellSize / 2,
            cellSize / 4,
            playerPaint
        )
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

                if (abs(dx) > abs(dy)) {
                    if (dx > swipeThreshold) moveRight()
                    else if (dx < -swipeThreshold) moveLeft()
                } else {
                    if (dy > swipeThreshold) moveDown()
                    else if (dy < -swipeThreshold) moveUp()
                }
            }
        }
        return true
    }

    private fun moveUp() {
        if (!grid[playerRow][playerCol].top && playerRow > 0) playerRow--
        checkWin()
        invalidate()
    }

    private fun moveDown() {
        if (!grid[playerRow][playerCol].bottom && playerRow < rows - 1) playerRow++
        checkWin()
        invalidate()
    }

    private fun moveLeft() {
        if (!grid[playerRow][playerCol].left && playerCol > 0) playerCol--
        checkWin()
        invalidate()
    }

    private fun moveRight() {
        if (!grid[playerRow][playerCol].right && playerCol < cols - 1) playerCol++
        checkWin()
        invalidate()
    }

    private fun checkWin() {
        if (playerRow == goalRow && playerCol == goalCol) {
            (context as? MazeGameActivity)?.onLevelCompleted()
        }
    }
}