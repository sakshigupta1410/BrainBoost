package com.example.brainboost

import android.graphics.Color
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.random.Random
import android.os.CountDownTimer

class WordSearchActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var txtTitle: TextView
    private lateinit var txtScore: TextView
    private lateinit var txtWords: TextView

    private lateinit var grid: Array<CharArray>
    private lateinit var soundPool: SoundPool
    private var correctSound = 0
    private var soundLock = false

    private val cellViews = mutableListOf<TextView>()
    private val selected = mutableListOf<Int>()
    private val lockedCells = mutableSetOf<Int>()
    private val foundWords = mutableSetOf<String>()

    private lateinit var txtTimer: TextView
    private var timer: CountDownTimer? = null
    private var timeLeft = 0L

    private var direction: Pair<Int, Int>? = null
    private var gridSize = 8
    private var score = 0
    private var failSound = 0
    private lateinit var level: String
    private lateinit var words: List<String>

    private val wordColors = listOf(
        Color.parseColor("#BBDEFB"),
        Color.parseColor("#C8E6C9"),
        Color.parseColor("#F8BBD0"),
        Color.parseColor("#D1C4E9")
    )

    private var colorIndex = 0

    val easyWordPool = listOf(
        "OTTER","ZEBRA","RABIT","SNAIL","KOALA",
        "CAMEL","PANDA","HORSE","FALCON","MONKEY"
    )

    val mediumWordPool = listOf(
        "PYTHON","COYOTE","JAGUAR","BADGER","DOLPHIN",
        "RAVENS","PARROT","WALRUS","FERRET","OCELOT"
    )

    val hardWordPool = listOf(
        "CHIMERA","IBEXES","NARWHL","AXOLOT","CORMOR",
        "MANATEE","CARACAL","GAVIAL","OKAPIS","TARANT"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_search)

        level = intent.getStringExtra("LEVEL") ?: "Easy"

        txtTitle = findViewById(R.id.txtTitle)
        txtScore = findViewById(R.id.txtScore)
        txtTimer = findViewById(R.id.txtTimer)

        txtWords = findViewById(R.id.txtWords)
        gridLayout = findViewById(R.id.gridLayout)
        soundPool = SoundPool.Builder().setMaxStreams(5).build()

        failSound = soundPool.load(this, R.raw.fail, 1)
        correctSound = soundPool.load(this, R.raw.win, 1)
        setupLevel()     // pehle time set hoga
        startTimer()     // phir timer start hoga
        setupTouch()

        gridLayout.post { generateGrid() }
    }
    private fun startTimer() {

        if (timeLeft <= 0L) return

        timer?.cancel()

        timer = object : CountDownTimer(timeLeft, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished

                val sec = millisUntilFinished / 1000
                val min = sec / 60
                val s = sec % 60

                txtTimer.text = String.format("Time : %02d:%02d", min, s)
            }

            override fun onFinish() {
                txtTimer.text = "Time : 00:00"
                showTimeUpDialog()
            }

        }.start()   // ← YE LINE missing ho to timer freeze
    }

    private fun showTimeUpDialog() {

        android.app.AlertDialog.Builder(this)
            .setTitle("⏰ Time Up")
            .setMessage("Your time finished!\nScore: $score")
            .setCancelable(false)
            .setPositiveButton("Retry") { _, _ ->
                recreate()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .show()
    }
    private fun playFailSound() {
        soundPool.play(failSound,1f,1f,1,0,1f)
    }

    private fun updateWordList() {
        val fullText = words.joinToString(", ")
        val spannable = android.text.SpannableString(fullText)

        words.forEach { word ->
            val start = fullText.indexOf(word)
            if (start >= 0 && word in foundWords) {
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(Color.GRAY),
                    start,
                    start + word.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    android.text.style.StrikethroughSpan(),
                    start,
                    start + word.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        txtWords.text = spannable
    }

    private fun setupLevel() {
        when (level) {

            "Easy" -> {
                gridSize = 7
                words = easyWordPool.filter { it.length in 5..6 }.shuffled().take(5)

                timeLeft = 0L        // Easy me timer OFF
                txtTimer.visibility = android.view.View.GONE
            }

            "Medium" -> {
                gridSize = 9
                words = mediumWordPool.filter { it.length in 5..7 }.shuffled().take(7)

                timeLeft = 90000L    // 90 sec
                txtTimer.visibility = android.view.View.VISIBLE
            }

            else -> {
                gridSize = 11
                words = hardWordPool.filter { it.length in 6..7 }.shuffled().take(8)

                timeLeft = 60000L    // 60 sec
                txtTimer.visibility = android.view.View.VISIBLE
            }
        }

        txtTitle.text = "WORD SEARCH • $level"
        txtScore.text = "Score : $score"
        txtWords.text = "Find: ${words.joinToString(", ")}"

        gridLayout.rowCount = gridSize
        gridLayout.columnCount = gridSize
        updateWordList()
    }

    // ---------- GRID ----------

    private fun generateGrid() {
        while (true) {
            grid = Array(gridSize) { CharArray(gridSize) { ' ' } }

            var allPlaced = true
            for (word in words) {
                if (!placeWordSafe(word)) {
                    allPlaced = false
                    break
                }
            }

            if (allPlaced) {
                fillRandom()
                renderGrid()
                return
            }
        }
    }

    private fun placeWordSafe(word: String): Boolean {
        val dirs = if (level == "Hard")
            listOf(Pair(0,1), Pair(1,0), Pair(1,1), Pair(1,-1))
        else
            listOf(Pair(0,1), Pair(1,0))

        repeat(200) {
            val d = dirs.random()
            val r = Random.nextInt(gridSize)
            val c = Random.nextInt(gridSize)

            if (canPlace(word, r, c, d)) {
                for (i in word.indices) {
                    grid[r + i * d.first][c + i * d.second] = word[i]
                }
                return true
            }
        }
        return false
    }

    private fun canPlace(w: String, r: Int, c: Int, d: Pair<Int,Int>): Boolean {
        for (i in w.indices) {
            val nr = r + i * d.first
            val nc = c + i * d.second
            if (nr !in 0 until gridSize || nc !in 0 until gridSize) return false
            if (grid[nr][nc] != ' ') return false
        }
        return true
    }

    private fun fillRandom() {
        val letters = listOf('A','E','O','R','S','T','L','N')
        for (r in 0 until gridSize)
            for (c in 0 until gridSize)
                if (grid[r][c] == ' ')
                    grid[r][c] = letters.random()
    }

    private fun renderGrid() {
        gridLayout.removeAllViews()
        cellViews.clear()

        val size =
            (gridLayout.width - gridLayout.paddingLeft - gridLayout.paddingRight) / gridSize

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val tv = TextView(this).apply {
                    text = grid[r][c].toString()
                    textSize = 16f
                    gravity = android.view.Gravity.CENTER
                    setTextColor(Color.BLACK)
                    background = ContextCompat.getDrawable(context, R.drawable.cell_normal)
                }
                tv.layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                }
                cellViews.add(tv)
                gridLayout.addView(tv)
            }
        }
    }

    // ---------- TOUCH ----------

    private fun setupTouch() {
        gridLayout.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> resetSelection()
                MotionEvent.ACTION_MOVE -> handleMove(e)
                MotionEvent.ACTION_UP -> checkWord()
            }
            true
        }
    }

    private fun handleMove(e: MotionEvent) {
        val idx = getIndex(e.x, e.y)
        if (idx == -1 || idx in selected || idx in lockedCells) return

        if (selected.isNotEmpty()) {
            val last = selected.last()
            val dr = idx / gridSize - last / gridSize
            val dc = idx % gridSize - last % gridSize

            val sdr = if (dr == 0) 0 else dr / abs(dr)
            val sdc = if (dc == 0) 0 else dc / abs(dc)

            if (direction == null) {
                if (!(dr == 0 && dc == 0) && abs(dr) <= 1 && abs(dc) <= 1) {
                    direction = Pair(sdr, sdc)
                } else return
            } else {
                if (Pair(sdr, sdc) != direction) return
            }
        }

        selected.add(idx)
        cellViews[idx].background =
            ContextCompat.getDrawable(this, R.drawable.cell_selected)
    }

    private fun getIndex(x: Float, y: Float): Int {
        for (i in cellViews.indices) {
            val v = cellViews[i]
            if (x >= v.left && x <= v.right && y >= v.top && y <= v.bottom)
                return i
        }
        return -1
    }

    private fun checkWord() {
        val w = selected.joinToString("") { cellViews[it].text.toString() }
        val reversed = w.reversed()

        val finalWord = when {
            w in words -> w
            reversed in words -> reversed
            else -> null
        }

        if (finalWord != null && finalWord !in foundWords) {

            foundWords.add(finalWord)
            score += 10
            txtScore.text = "Score : $score"

            if (!soundLock) {
                soundLock = true
                soundPool.play(correctSound,1f,1f,1,0,1f)
                Handler(Looper.getMainLooper()).postDelayed({
                    soundLock = false
                },150)
            }

            val color = wordColors[colorIndex++ % wordColors.size]

            selected.forEach {
                lockedCells.add(it)
                val d = ContextCompat
                    .getDrawable(this, R.drawable.bg_word_found)!!
                    .mutate()
                d.setTint(color)
                cellViews[it].background = d
            }

        } else {
            playFailSound()
            selected.forEach {
                if (it !in lockedCells)
                    cellViews[it].background =
                        ContextCompat.getDrawable(this, R.drawable.cell_normal)
            }
        }

        resetSelection()
        updateWordList()

        if (foundWords.size == words.size) onLevelComplete()
    }

    private fun onLevelComplete() {
        soundPool.play(correctSound,1f,1f,1,0,1f)
        val next = getNextLevel()
        timer?.cancel()
        android.app.AlertDialog.Builder(this)
            .setTitle("Level Complete 🎉")
            .setMessage("You found all words!\nScore: $score")
            .setCancelable(false)
            .setPositiveButton(
                if (next != null) "Next Level" else "Finish"
            ) { _, _ ->

                if (next != null) {
                    val intent = intent
                    intent.putExtra("LEVEL", next)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else {
                    android.widget.Toast
                        .makeText(this, "🧠 Word Search Master!", android.widget.Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
            }
            .show()
    }

    private fun resetSelection() {
        selected.clear()
        direction = null
    }

    private fun getNextLevel(): String? {
        return when (level) {
            "Easy" -> "Medium"
            "Medium" -> "Hard"
            else -> null
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
        soundPool.release()
    }
}