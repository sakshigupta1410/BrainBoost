package com.example.brainboost

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class WordScrambleActivity : AppCompatActivity() {

    private lateinit var txtScore: TextView
    private lateinit var answerLayout: LinearLayout
    private lateinit var letterGrid: GridLayout
    private lateinit var btnClear: Button
    private lateinit var txtHint: TextView
    private lateinit var txtTimer: TextView
    private var countDownTimer: android.os.CountDownTimer? = null
    private var timeLeft = 20

    private var score = 0
    private lateinit var correctWord: String

    private val selectedButtons = mutableListOf<Button>()
    private val answerSlots = mutableListOf<TextView>()

    private val wordPool = listOf(
        "ANDROID","KOTLIN","OBJECT","VARIABLE","FUNCTION",
        "CLASS","STRING","INTEGER","BOOLEAN","ARRAY",
        "PUZZLE","SCRAMBLE","BRAIN","FOCUS","THINK",
        "APPLE","BANANA","ORANGE","MANGO","GRAPES",
        "PROGRAMMING","DEVELOPMENT","APPLICATION"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_scramble)

        txtScore = findViewById(R.id.txtScore)
        txtHint = findViewById(R.id.txtHint)
        answerLayout = findViewById(R.id.answerLayout)
        letterGrid = findViewById(R.id.letterGrid)
        btnClear = findViewById(R.id.btnClear)
        txtTimer = findViewById(R.id.txtTimer)

        btnClear.setOnClickListener { resetSelection() }

        letterGrid.post { startNewWord() }
    }

    // ---------------- GAME FLOW ----------------

    private fun startNewWord() {
        resetSelection()
        correctWord = wordPool.random()

        txtHint.text = "Hint: Starts with '${correctWord[0]}'"
        createAnswerSlots(correctWord.length)
        createScrambledLetters(correctWord)
        startTimer()   // 👈 ADD THIS LINE
    }

    // ---------------- ANSWER UI ----------------

    private fun createAnswerSlots(count: Int) {
        answerLayout.removeAllViews()
        answerSlots.clear()

        repeat(count) {
            val tv = TextView(this).apply {
                text = "_"


                textSize = 22f
                setTextColor(Color.parseColor("#37474F"))
                gravity = Gravity.CENTER
                setPadding(8,6,8,6)
                textSize = if (count > 10) 16f else 20f
                setBackgroundColor(Color.parseColor("#E3F2FD"))
            }

            val params = LinearLayout.LayoutParams(
                0,
                resources.getDimensionPixelSize(R.dimen.answer_slot_height),
                1f
            )

            params.setMargins(8, 8, 8, 8) // ✅ WORKS ONLY if params is LinearLayout.LayoutParams

            tv.layoutParams = params

            answerSlots.add(tv)
            answerLayout.addView(tv)
        }
    }

    // ---------------- LETTER GRID ----------------

    private fun createScrambledLetters(word: String) {

        letterGrid.removeAllViews()

        val letters = word.toList().shuffled()
        val columns = minOf(6, letters.size)
        letterGrid.columnCount = columns

        val availableWidth =
            letterGrid.width - letterGrid.paddingLeft - letterGrid.paddingRight

        val marginPx = 24
        val size = (availableWidth - marginPx * columns) / columns

        letters.forEach { ch ->

            val btn = Button(this).apply {
                text = ch.toString()
                textSize = 18f
                minWidth = 0
                minHeight = 0
                setPadding(0,0,0,0)

                setTextColor(Color.parseColor("#37474F"))
                setBackgroundColor(Color.parseColor("#F3E5F5"))

                alpha = 0f
                translationY = 60f

                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(Random.nextLong(0,150))
                    .setDuration(250)
                    .start()

                setOnClickListener { onLetterClicked(this) }
            }

            val params = GridLayout.LayoutParams().apply {
                width = size
                height = size
                setMargins(12,12,12,12)
            }

            btn.layoutParams = params
            letterGrid.addView(btn)
        }
    }

    // ---------------- INTERACTION ----------------
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
    private fun onLetterClicked(btn: Button) {

        if (selectedButtons.contains(btn)) return

        btn.alpha = 0.4f
        btn.setBackgroundColor(Color.parseColor("#E8F5E9"))
        btn.animate()
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(80)
            .withEndAction {
                btn.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }.start()

        val index = answerSlots.indexOfFirst { it.text == "_" }
        if (index == -1) return

        answerSlots[index].text = btn.text
        selectedButtons.add(btn)
        btn.isEnabled = false

        if (selectedButtons.size == correctWord.length) {
            checkAnswer()
        }
    }
    private fun startTimer() {

        countDownTimer?.cancel()
        timeLeft = 20
        txtTimer.text = "Time : $timeLeft"

        countDownTimer = object : android.os.CountDownTimer(20000,1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft--
                txtTimer.text = "Time : $timeLeft"
            }

            override fun onFinish() {
                Toast.makeText(this@WordScrambleActivity,"⏰ Time Up!",Toast.LENGTH_SHORT).show()
                startNewWord()
            }
        }.start()
    }

    private fun checkAnswer() {

        selectedButtons.forEach {
            it.setBackgroundColor(Color.parseColor("#C8E6C9"))
        }

        val userWord = answerSlots.joinToString("") { it.text.toString() }

        if (userWord == correctWord) {
            playWinSound()
            countDownTimer?.cancel()
            score += 10
            txtScore.text = "Score : $score"

            answerSlots.forEach { tv ->
                tv.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(120)
                    .withEndAction {
                        tv.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                    }.start()
            }

            Toast.makeText(this, "Correct! 🎉", Toast.LENGTH_SHORT).show()

            answerLayout.postDelayed({
                startNewWord()
            }, 400)

        } else {
            playFailSound()
            answerLayout.animate()
                .translationX(25f)
                .setDuration(60)
                .withEndAction {
                    answerLayout.animate().translationX(-25f).setDuration(60)
                        .withEndAction {
                            answerLayout.animate().translationX(0f).setDuration(60).start()
                        }.start()
                }.start()

            Toast.makeText(this, "Wrong! Try again", Toast.LENGTH_SHORT).show()
            resetSelection()
        }
    }

    private fun resetSelection() {
        selectedButtons.forEach {
            it.alpha = 1f
            it.isEnabled = true
            it.setBackgroundColor(Color.parseColor("#F3E5F5"))
        }
        selectedButtons.clear()
        answerSlots.forEach { it.text = "_" }
    }
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}