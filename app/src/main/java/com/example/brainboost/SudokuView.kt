package com.example.brainboost

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.random.Random

class SudokuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ---------- WIN CALLBACK ----------
    private var onWin: (() -> Unit)? = null
    fun setOnWinListener(listener: () -> Unit) {
        onWin = listener
    }

    // ---------- PAINTS ----------
    private val thinPaint = Paint().apply {
        color = Color.parseColor("#D0D0D0")
        strokeWidth = 2f
    }

    private val thickPaint = Paint().apply {
        color = Color.parseColor("#9E9E9E")
        strokeWidth = 5f
    }

    private val selectedBgPaint = Paint().apply {
        color = Color.parseColor("#BBDEFB")
    }

    private val selectedBorderPaint = Paint().apply {
        color = Color.parseColor("#1976D2")
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val givenTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 42f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private val userTextPaint = Paint().apply {
        color = Color.parseColor("#5E35B1")
        textSize = 42f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val wrongTextPaint = Paint().apply {
        color = Color.RED
        textSize = 42f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    // ---------- PUZZLES ----------
    private val puzzles = listOf(
        arrayOf(
            intArrayOf(0,6,0,8,0,7,0,0,0),
            intArrayOf(1,0,0,0,0,6,2,0,0),
            intArrayOf(0,0,0,0,0,0,0,0,1),
            intArrayOf(8,0,0,0,5,0,0,0,3),
            intArrayOf(0,0,5,0,0,0,0,7,9),
            intArrayOf(0,0,4,0,0,0,0,1,0),
            intArrayOf(0,0,0,7,6,0,0,0,0),
            intArrayOf(0,0,0,0,0,9,0,0,0),
            intArrayOf(5,0,8,0,0,0,0,0,2)
        ),
        arrayOf(
            intArrayOf(0,0,0,2,6,0,7,0,1),
            intArrayOf(6,8,0,0,7,0,0,9,0),
            intArrayOf(1,9,0,0,0,4,5,0,0),
            intArrayOf(8,2,0,1,0,0,0,4,0),
            intArrayOf(0,0,4,6,0,2,9,0,0),
            intArrayOf(0,5,0,0,0,3,0,2,8),
            intArrayOf(0,0,9,3,0,0,0,7,4),
            intArrayOf(0,4,0,0,5,0,0,3,6),
            intArrayOf(7,0,3,0,1,8,0,0,0)
        )
    )

    // ---------- ACTIVE GAME ----------
    private lateinit var puzzle: Array<IntArray>
    private lateinit var board: Array<IntArray>
    private val wrongCells = mutableSetOf<Pair<Int, Int>>()

    private var cellSize = 0f
    private var selectedRow = -1
    private var selectedCol = -1

    init {
        startNewGame()
    }

    // ---------- NEW GAME ----------
    fun startNewGame() {
        puzzle = puzzles.random()
            .map { it.clone() }
            .toTypedArray()

        shuffleBoard(puzzle)   // 🔥 MAGIC LINE

        board = Array(9) { r -> puzzle[r].clone() }
        wrongCells.clear()
        selectedRow = -1
        selectedCol = -1
        invalidate()
    }
    private fun shuffleBoard(board: Array<IntArray>) {
        // shuffle rows within bands
        for (band in 0..2) {
            val start = band * 3
            board.shuffleRange(start, start + 3)
        }

        // shuffle columns within stacks
        for (stack in 0..2) {
            val start = stack * 3
            shuffleColumns(board, start, start + 3)
        }
    }

    private fun Array<IntArray>.shuffleRange(from: Int, to: Int) {
        for (i in from until to) {
            val j = Random.nextInt(from, to)
            val temp = this[i]
            this[i] = this[j]
            this[j] = temp
        }
    }

    private fun shuffleColumns(board: Array<IntArray>, from: Int, to: Int) {
        for (i in from until to) {
            val j = Random.nextInt(from, to)
            for (r in 0..8) {
                val temp = board[r][i]
                board[r][i] = board[r][j]
                board[r][j] = temp
            }
        }
    }


    // ---------- MEASURE ----------
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }

    // ---------- DRAW ----------
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cellSize = width / 9f

        if (selectedRow != -1 && selectedCol != -1) {
            canvas.drawRect(
                0f,
                selectedRow * cellSize,
                width.toFloat(),
                (selectedRow + 1) * cellSize,
                selectedBgPaint
            )
            canvas.drawRect(
                selectedCol * cellSize,
                0f,
                (selectedCol + 1) * cellSize,
                height.toFloat(),
                selectedBgPaint
            )
        }

        for (i in 0..9) {
            val paint = if (i % 3 == 0) thickPaint else thinPaint
            canvas.drawLine(0f, i * cellSize, width.toFloat(), i * cellSize, paint)
            canvas.drawLine(i * cellSize, 0f, i * cellSize, height.toFloat(), paint)
        }

        if (selectedRow != -1 && selectedCol != -1) {
            canvas.drawRect(
                selectedCol * cellSize,
                selectedRow * cellSize,
                (selectedCol + 1) * cellSize,
                (selectedRow + 1) * cellSize,
                selectedBorderPaint
            )
        }

        for (r in 0..8) {
            for (c in 0..8) {
                val value = board[r][c]
                if (value != 0) {
                    val paint = when {
                        puzzle[r][c] != 0 -> givenTextPaint
                        wrongCells.contains(r to c) -> wrongTextPaint
                        else -> userTextPaint
                    }
                    canvas.drawText(
                        value.toString(),
                        c * cellSize + cellSize / 2,
                        r * cellSize + cellSize * 0.7f,
                        paint
                    )
                }
            }
        }
    }

    // ---------- TOUCH ----------
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            selectedRow = (event.y / cellSize).toInt()
            selectedCol = (event.x / cellSize).toInt()
            invalidate()
            return true
        }
        return false
    }

    // ---------- GAME ----------
    fun setNumber(num: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        if (puzzle[selectedRow][selectedCol] != 0) return

        board[selectedRow][selectedCol] = num

        if (isValid(selectedRow, selectedCol, num)) {
            wrongCells.remove(selectedRow to selectedCol)
        } else {
            wrongCells.add(selectedRow to selectedCol)
        }

        if (isBoardComplete() && wrongCells.isEmpty()) {
            onWin?.invoke()
        }

        invalidate()
    }

    fun clearCell() {
        if (selectedRow == -1 || selectedCol == -1) return
        if (puzzle[selectedRow][selectedCol] != 0) return

        board[selectedRow][selectedCol] = 0
        wrongCells.remove(selectedRow to selectedCol)
        invalidate()
    }

    private fun isValid(row: Int, col: Int, num: Int): Boolean {
        for (c in 0..8) if (c != col && board[row][c] == num) return false
        for (r in 0..8) if (r != row && board[r][col] == num) return false

        val boxRow = row / 3 * 3
        val boxCol = col / 3 * 3
        for (r in boxRow until boxRow + 3)
            for (c in boxCol until boxCol + 3)
                if ((r != row || c != col) && board[r][c] == num)
                    return false

        return true
    }

    private fun isBoardComplete(): Boolean {
        for (r in 0..8)
            for (c in 0..8)
                if (board[r][c] == 0) return false
        return true
    }
}
