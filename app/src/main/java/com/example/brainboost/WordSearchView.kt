package com.example.brainboost

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.random.Random

class WordSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ---------- PAINTS ----------
    private val cellPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val highlightPaint = Paint().apply {
        color = Color.parseColor("#AA81D4FA")
        style = Paint.Style.FILL
    }

    private val foundPaint = Paint().apply {
        color = Color.parseColor("#AA81C784")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 36f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    // ---------- GRID CONFIG ----------
    private var rows = 10
    private var cols = 10
    private var cellSize = 0f

    private var grid = Array(rows) { CharArray(cols) { ' ' } }

    // directions → ↓
    private var directions = listOf(
        Pair(0, 1),
        Pair(1, 0)
    )

    // ---------- STATE ----------
    private val selectedCells = mutableListOf<Pair<Int, Int>>()
    private val foundCells = mutableSetOf<Pair<Int, Int>>()
    private val placedWordCells = mutableMapOf<String, List<Pair<Int, Int>>>()

    private var words = listOf<String>()

    private var wordFoundListener: ((String, Boolean) -> Unit)? = null
    private var wordSelectListener: ((String) -> Unit)? = null

    // ---------- PUBLIC API ----------
    fun setupLevel(level: String) {
        // abhi same size rakha hai (safe)
        // baad me yahin difficulty badhegi
        grid = Array(rows) { CharArray(cols) { ' ' } }
    }

    fun setWords(words: List<String>) {
        this.words = words
        generateGrid()
        invalidate()
    }

    fun setOnWordFoundListener(listener: (String, Boolean) -> Unit) {
        wordFoundListener = listener
    }

    fun setOnWordSelectListener(listener: (String) -> Unit) {
        wordSelectListener = listener
    }

    // ---------- GRID GENERATION ----------
    private fun generateGrid() {


        for (r in 0 until rows)
            for (c in 0 until cols)
                grid[r][c] = ' '

        placedWordCells.clear()
        foundCells.clear()

        for (word in words) {
            placeWord(word)
        }

        // fill blanks
        for (r in 0 until rows)
            for (c in 0 until cols)
                if (grid[r][c] == ' ')
                    grid[r][c] = ('A'..'Z').random()
    }

    private fun placeWord(word: String) {
        repeat(100) {
            val dir = directions.random()
            val dr = dir.first
            val dc = dir.second

            val sr = Random.nextInt(rows)
            val sc = Random.nextInt(cols)

            val er = sr + dr * (word.length - 1)
            val ec = sc + dc * (word.length - 1)

            if (er !in 0 until rows || ec !in 0 until cols) return@repeat

            val cells = mutableListOf<Pair<Int, Int>>()
            var canPlace = true

            for (i in word.indices) {
                val r = sr + dr * i
                val c = sc + dc * i
                if (grid[r][c] != ' ' && grid[r][c] != word[i]) {
                    canPlace = false
                    break
                }
                cells.add(Pair(r, c))
            }

            if (canPlace) {
                for (i in word.indices) {
                    val (r, c) = cells[i]
                    grid[r][c] = word[i]
                }
                placedWordCells[word] = cells
                return
            }
        }
    }

    // ---------- MEASURE ----------
    override fun onMeasure(w: Int, h: Int) {
        val size = min(
            MeasureSpec.getSize(w),
            MeasureSpec.getSize(h)
        )
        setMeasuredDimension(size, size)
    }

    // ---------- DRAW ----------
    override fun onDraw(canvas: Canvas) {
        if (grid.isEmpty()) return
        super.onDraw(canvas)
        cellSize = width / cols.toFloat()

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = c * cellSize
                val top = r * cellSize
                val cell = Pair(r, c)

                when {
                    foundCells.contains(cell) ->
                        canvas.drawRect(left, top, left + cellSize, top + cellSize, foundPaint)
                    selectedCells.contains(cell) ->
                        canvas.drawRect(left, top, left + cellSize, top + cellSize, highlightPaint)
                }

                canvas.drawRect(left, top, left + cellSize, top + cellSize, cellPaint)

                canvas.drawText(
                    grid[r][c].toString(),
                    left + cellSize / 2,
                    top + cellSize * 0.7f,
                    textPaint
                )
            }
        }
    }

    // ---------- TOUCH ----------
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val row = (event.y / cellSize).toInt()
        val col = (event.x / cellSize).toInt()

        if (row !in 0 until rows || col !in 0 until cols) return true

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedCells.clear()
                selectedCells.add(Pair(row, col))
            }

            MotionEvent.ACTION_MOVE -> {
                val cell = Pair(row, col)
                if (!selectedCells.contains(cell))
                    selectedCells.add(cell)

                val word = selectedCells.joinToString("") {
                    grid[it.first][it.second].toString()
                }
                wordSelectListener?.invoke(word)
            }

            MotionEvent.ACTION_UP -> {
                val word = selectedCells.joinToString("") {
                    grid[it.first][it.second].toString()
                }

                if (placedWordCells.containsKey(word)) {
                    foundCells.addAll(placedWordCells[word]!!)
                    wordFoundListener?.invoke(
                        word,
                        foundCells.size >= placedWordCells.values.flatten().size
                    )
                }
                selectedCells.clear()
            }
        }

        invalidate()
        return true
    }
}
