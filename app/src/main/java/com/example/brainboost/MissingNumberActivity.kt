package com.example.brainboost

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MissingNumberActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvLevel: TextView

    private lateinit var gridCells: List<TextView>
    private lateinit var optionButtons: List<Button>

    private var score = 0
    private var level = 1
    private var correctAnswer = 0

    private var streak = 0
    private var lives = 3
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var tvStreak: TextView
    private lateinit var soundPool: android.media.SoundPool
    private var winSound = 0
    private var failSound = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missing_number)
        val audioAttr = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_GAME)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = android.media.SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttr)
            .build()

        winSound = soundPool.load(this, R.raw.win, 1)
        failSound = soundPool.load(this, R.raw.fail, 1)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)
        tvScore = findViewById(R.id.tvScore)
        tvResult = findViewById(R.id.tvResult)
        tvLevel = findViewById(R.id.tvLevel)
        tvStreak = findViewById(R.id.tvStreak)
        gridCells = listOf(
            findViewById(R.id.cell1),
            findViewById(R.id.cell2),
            findViewById(R.id.cell3),
            findViewById(R.id.cell4),
            findViewById(R.id.cell5),
            findViewById(R.id.cell6),
            findViewById(R.id.cell7),
            findViewById(R.id.cell8),
            findViewById(R.id.cell9)
        )

        optionButtons = listOf(
            findViewById(R.id.btnOpt1),
            findViewById(R.id.btnOpt2),
            findViewById(R.id.btnOpt3),
            findViewById(R.id.btnOpt4)
        )

        loadPuzzle()
        updateHearts()
        optionButtons.forEach { button ->
            button.setOnClickListener {
                checkAnswer(button.text.toString().toInt())
            }
        }
    }
    private fun playWinSound() {
        soundPool.play(winSound,1f,1f,1,0,1f)
    }

    private fun playFailSound() {
        soundPool.play(failSound,1f,1f,1,0,1f)
    }
    // 🔁 Load puzzle based on difficulty
    private fun loadPuzzle() {
        tvResult.text = ""
        tvLevel.text = "Level: $level"

        val puzzle = when {
            level <= 3 -> generateEasyPuzzle()
            level <= 6 -> generateMediumPuzzle()
            else -> generateHardPuzzle()
        }

        correctAnswer = puzzle.answer

        gridCells.forEachIndexed { index, textView ->
            textView.text = puzzle.grid[index]
        }

        val options = puzzle.options.shuffled()
        optionButtons.forEachIndexed { index, button ->
            button.text = options[index].toString()
        }
        if (level > 4 && Random.nextBoolean()) {
            gridCells.shuffled().forEachIndexed { index, view ->
                view.text = puzzle.grid[index]
            }
        }
        if (level % 5 == 0) {
            tvResult.text = "⚡ BOSS LEVEL!"
            tvResult.setTextColor(Color.parseColor("#BB86FC"))
        }
    }

    // ✅ EASY: 11, 22, 33...
    private fun generateEasyPuzzle(): Puzzle {
        val numbers = listOf(11, 22, 33, 44, 55, 66, 77, 88, 99).shuffled()
        val missingIndex = Random.nextInt(9)
        val answer = numbers[missingIndex]

        val grid = numbers.mapIndexed { index, num ->
            if (index == missingIndex) "?" else num.toString()
        }

        val options = mutableSetOf(answer)
        while (options.size < 4) {
            options.add((1..9).random() * 11)
        }

        return Puzzle(grid, answer, options.toList())
    }

    // ✅ MEDIUM: +2 or +3 pattern
    private fun generateMediumPuzzle(): Puzzle {
        val start = Random.nextInt(2, 10)
        val step = listOf(2, 3).random()

        val numbers = List(9) { start + it * step }
        val missingIndex = Random.nextInt(9)
        val answer = numbers[missingIndex]

        val grid = numbers.mapIndexed { index, num ->
            if (index == missingIndex) "?" else num.toString()
        }

        val options = mutableSetOf(answer)
        while (options.size < 4) {
            options.add(answer + Random.nextInt(-5, 6))
        }

        return Puzzle(grid, answer, options.toList())
    }

    // 🔥 HARD: row logic
    private fun generateHardPuzzle(): Puzzle {
        val a = Random.nextInt(2, 10)
        val gridNums = listOf(
            a, a * 2, a * 3,
            a + 1, (a + 1) * 2, (a + 1) * 3,
            a + 2, (a + 2) * 2, (a + 2) * 3
        )

        val missingIndex = Random.nextInt(9)
        val answer = gridNums[missingIndex]

        val grid = gridNums.mapIndexed { index, num ->
            if (index == missingIndex) "?" else num.toString()
        }

        val options = mutableSetOf(answer)
        while (options.size < 4) {
            options.add(answer + Random.nextInt(-10, 10))
        }

        return Puzzle(grid, answer, options.toList())
    }
    private fun updateHearts() {

        val hearts = listOf(heart1, heart2, heart3)

        hearts.forEachIndexed { index, imageView ->
            if (index < lives) {
                imageView.setImageResource(R.drawable.ic_heart_full)
            } else {
                imageView.setImageResource(R.drawable.ic_heart_empty)
            }
        }
    }
    private fun checkAnswer(selected: Int) {
        if (selected == correctAnswer) {
            playWinSound()
            streak++
            tvStreak.text = "🔥 x$streak"
            score++
            level++
            tvResult.text = "✅ Correct!"
            tvResult.setTextColor(Color.GREEN)
            gridCells.forEach {
                it.animate().rotationBy(360f).setDuration(250).start()
            }
        } else {
            playFailSound()
            streak = 0
            tvStreak.text = ""
            lives--
            updateHearts()

            tvResult.text = "❌ Wrong! Answer: $correctAnswer"
            tvResult.setTextColor(Color.RED)

            if (lives <= 0) {
                gameOver()
                return
            }
        }
        tvScore.text = "Score: $score"

        val delay = if (level % 3 == 0) 700L else 1200L   // 👈 L important (Long type)

        tvResult.postDelayed({
            loadPuzzle()
        }, delay)
    }
    private fun gameOver() {
        playFailSound()
        tvResult.text = "💀 Game Over!"
        tvResult.setTextColor(Color.RED)

        optionButtons.forEach { it.isEnabled = false }

        tvResult.postDelayed({
            lives = 3
            score = 0
            level = 1
            streak = 0

            tvScore.text = "Score: 0"
            tvStreak.text = ""

            optionButtons.forEach { it.isEnabled = true }

            updateHearts()
            loadPuzzle()

        }, 2000)
    }
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}

// 🧩 Puzzle Model
data class Puzzle(
    val grid: List<String>,
    val answer: Int,
    val options: List<Int>
)
