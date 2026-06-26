package com.example.brainboost

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.widget.ImageView
import android.widget.LinearLayout

data class MathQuestion(
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val correct: Int
)

class MathBrainQuizActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var tvScore: TextView
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btnNext: Button
    private lateinit var hearts: List<ImageView>

    private lateinit var questionBox: LinearLayout
    private lateinit var resultLayout: LinearLayout
    private lateinit var tvFinalScore: TextView
    private lateinit var tvPerformance: TextView
    private lateinit var btnRestart: Button
    private lateinit var btnExit: Button

    private var lives = 3
    private var index = 0
    private var score = 0
    private var answered = false

    private val defaultColor = "#7E57C2"   // purple
    private val correctColor = "#4CAF50"   // green
    private val wrongColor = "#F44336"     // red

    private val questions = mutableListOf<MathQuestion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math_brain_quiz)

        tvQuestion = findViewById(R.id.tvQuestion)
        tvScore = findViewById(R.id.tvScore)
        btn1 = findViewById(R.id.btnOption1)
        btn2 = findViewById(R.id.btnOption2)
        btn3 = findViewById(R.id.btnOption3)
        btnNext = findViewById(R.id.btnNext)
        hearts = listOf(
            findViewById(R.id.heart1),
            findViewById(R.id.heart2),
            findViewById(R.id.heart3)
        )
        resultLayout = findViewById(R.id.resultLayout)
        questionBox = findViewById(R.id.questionBox)
        tvFinalScore = findViewById(R.id.tvFinalScore)
        tvPerformance = findViewById(R.id.tvPerformance)
        btnRestart = findViewById(R.id.btnRestart)
        btnExit = findViewById(R.id.btnExit)

        updateLivesUI()
        generateMathQuestions()
        loadQuestion()
        resetButtons()

        btn1.setOnClickListener { checkAnswer(1) }
        btn2.setOnClickListener { checkAnswer(2) }
        btn3.setOnClickListener { checkAnswer(3) }

        btnNext.setOnClickListener {
            if (!answered) return@setOnClickListener

            index++
            if (index < questions.size) {
                resetButtons()
                loadQuestion()
            } else {
                showResultCard()
                tvScore.text = "Final Score: $score / ${questions.size}"
                btnNext.isEnabled = false
            }
        }
    }

    // ---------------- LOGIC ----------------

    private fun generateMathQuestions() {
        repeat(20) {
            val a = Random.nextInt(5, 30)
            val b = Random.nextInt(5, 30)
            val op = Random.nextInt(3)

            val correctAnswer: Int
            val questionText: String

            when (op) {
                0 -> {
                    correctAnswer = a + b
                    questionText = "$a + $b = ?"
                }
                1 -> {
                    correctAnswer = a - b
                    questionText = "$a - $b = ?"
                }
                else -> {
                    correctAnswer = a * b
                    questionText = "$a × $b = ?"
                }
            }

            val options = mutableListOf(
                correctAnswer,
                correctAnswer + Random.nextInt(1, 6),
                correctAnswer - Random.nextInt(1, 6)
            ).shuffled()

            questions.add(
                MathQuestion(
                    questionText,
                    options[0].toString(),
                    options[1].toString(),
                    options[2].toString(),
                    options.indexOf(correctAnswer) + 1
                )
            )
        }
    }

    private fun loadQuestion() {
        val q = questions[index]
        tvQuestion.text = q.question
        btn1.text = q.option1
        btn2.text = q.option2
        btn3.text = q.option3
        tvScore.text = "Score: $score"
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
    private fun checkAnswer(selected: Int) {
        if (answered) return
        answered = true

        val q = questions[index]
        val selectedBtn = getButton(selected)
        val correctBtn = getButton(q.correct)

        if (selected == q.correct) {
            playWinSound()
            score++
            selectedBtn.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(correctColor))
        } else {
            playFailSound()
            lives--
            updateLivesUI()

            selectedBtn.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(wrongColor))

            correctBtn.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(correctColor))

            if (lives <= 0) {
                showResultCard()
                btn1.isEnabled = false
                btn2.isEnabled = false
                btn3.isEnabled = false
                btnNext.isEnabled = false
                return
            }
        }

        tvScore.text = "Score: $score"
    }

    private fun getButton(n: Int): Button {
        return when (n) {
            1 -> btn1
            2 -> btn2
            else -> btn3
        }
    }

    private fun resetButtons() {
        val defaultTint =
            ColorStateList.valueOf(Color.parseColor(defaultColor))

        listOf(btn1, btn2, btn3).forEach {
            it.backgroundTintList = defaultTint
            it.setTextColor(Color.WHITE)
        }
        answered = false
    }
    private fun updateLivesUI() {
        for (i in hearts.indices) {
            hearts[i].setImageResource(
                if (i < lives) R.drawable.ic_heart_full
                else R.drawable.ic_heart_empty
            )
        }
    }
    private fun showResultCard() {

        resultLayout.visibility = View.VISIBLE
        questionBox.visibility = View.GONE
        tvQuestion.visibility = View.GONE
        btn1.visibility = View.GONE
        btn2.visibility = View.GONE
        btn3.visibility = View.GONE
        btnNext.visibility = View.GONE

        tvFinalScore.text = "Score: $score / ${questions.size}"

        val percent = (score * 100) / questions.size
        tvPerformance.text = when {
            percent >= 80 -> "Excellent 🚀"
            percent >= 50 -> "Good 👍"
            else -> "Keep Practicing 💡"
        }

        btnRestart.setOnClickListener {

            lives = 3
            index = 0
            score = 0
            answered = false

            questions.clear()
            generateMathQuestions()

            updateLivesUI()
            resetButtons()
            loadQuestion()

            resultLayout.visibility = View.GONE
            questionBox.visibility = View.VISIBLE
            tvQuestion.visibility = View.VISIBLE
            btn1.visibility = View.VISIBLE
            btn2.visibility = View.VISIBLE
            btn3.visibility = View.VISIBLE
            btnNext.visibility = View.VISIBLE
            hearts.forEach { it.visibility = View.GONE }
        }
        btnExit.setOnClickListener { finish() }
    }
}
