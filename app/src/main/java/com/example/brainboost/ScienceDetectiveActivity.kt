package com.example.brainboost

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

data class ScienceQuestion(
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val correct: Int,
    val explanation: String
)

data class ShuffledQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

class ScienceDetectiveActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvExplanation: TextView
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btnNext: Button

    private lateinit var resultLayout: LinearLayout
    private lateinit var tvFinalScore: TextView
    private lateinit var tvPerformance: TextView
    private lateinit var btnRestart: Button
    private lateinit var btnExit: Button
    private lateinit var gameContainer: LinearLayout
    private lateinit var questionBox: LinearLayout

    private var index = 0
    private var score = 0
    private var answered = false

    private lateinit var currentQuestion: ShuffledQuestion

    private val defaultColor = "#7E57C2"
    private val correctColor = "#4CAF50"
    private val wrongColor = "#F44336"

    private var questions = listOf(
        ScienceQuestion(
            "A plant kept in dark room stops growing. Why?",
            "No sunlight", "No water", "No soil",
            1,
            "Plants need sunlight to make food. This is called photosynthesis."
        ),
        ScienceQuestion(
            "Why do we see lightning before thunder?",
            "Light travels faster", "Sound travels faster", "Air is heavy",
            1,
            "Light travels much faster than sound, so we see lightning before hearing thunder."
        ),
        ScienceQuestion(
            "Which gas do humans breathe in for survival?",
            "Oxygen", "Carbon dioxide", "Nitrogen",
            1,
            "Humans need oxygen for respiration to release energy from food."
        ),
        ScienceQuestion(
            "Why do metals feel cold to touch?",
            "They absorb heat quickly", "They produce cold", "They contain water",
            1,
            "Metals conduct heat away from your skin quickly, making them feel cold."
        ),
        ScienceQuestion(
            "What happens when ice melts?",
            "Solid changes to liquid", "Liquid changes to gas", "Gas changes to solid",
            1,
            "Melting is the process where a solid becomes a liquid due to heat."
        ),
        ScienceQuestion(
            "Why do we sweat during exercise?",
            "To cool the body", "To gain energy", "To store water",
            1,
            "Sweat evaporates and removes heat, helping regulate body temperature."
        ),
        ScienceQuestion(
            "What is the main source of energy for Earth?",
            "Sun", "Moon", "Wind",
            1,
            "The Sun provides heat and light energy that supports life on Earth."
        ),
        ScienceQuestion(
            "Why do objects fall to the ground?",
            "Gravity", "Magnetism", "Friction",
            1,
            "Gravity pulls objects toward the center of the Earth."
        ),
        ScienceQuestion(
            "What do plants release during photosynthesis?",
            "Oxygen", "Carbon dioxide", "Nitrogen",
            1,
            "Plants release oxygen as a by-product of photosynthesis."
        ),
        ScienceQuestion(
            "Why does a shadow form?",
            "Light is blocked", "Air is blocked", "Sound is blocked",
            1,
            "A shadow forms when an object blocks light from reaching a surface."
        ),
        ScienceQuestion(
            "What happens when water boils?",
            "Liquid turns into gas", "Gas turns into liquid", "Solid turns into gas",
            1,
            "Boiling changes water into steam due to heat energy."
        ),
        ScienceQuestion(
            "Which organ pumps blood in our body?",
            "Heart", "Lungs", "Brain",
            1,
            "The heart pumps blood throughout the body via the circulatory system."
        ),
        ScienceQuestion(
            "Why do we see the Moon at night?",
            "It reflects sunlight", "It makes its own light", "It stores energy",
            1,
            "The Moon shines because it reflects light from the Sun."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_science_detective)
        questions = questions.shuffled()
        tvQuestion = findViewById(R.id.tvQuestion)
        tvScore = findViewById(R.id.tvScore)
        tvExplanation = findViewById(R.id.tvExplanation)
        btn1 = findViewById(R.id.btnOption1)
        btn2 = findViewById(R.id.btnOption2)
        btn3 = findViewById(R.id.btnOption3)
        btnNext = findViewById(R.id.btnNext)

        questionBox = findViewById(R.id.questionBox)
        resultLayout = findViewById(R.id.resultLayout)
        tvFinalScore = findViewById(R.id.tvFinalScore)
        tvPerformance = findViewById(R.id.tvPerformance)
        btnRestart = findViewById(R.id.btnRestart)
        btnExit = findViewById(R.id.btnExit)
        gameContainer = findViewById(R.id.gameContainer)

        btnRestart.setOnClickListener { restartQuiz() }
        btnExit.setOnClickListener {
            finish()
        }
        loadQuestion()
        resetButtons()

        btn1.setOnClickListener { checkAnswer(1) }
        btn2.setOnClickListener { checkAnswer(2) }
        btn3.setOnClickListener { checkAnswer(3) }

        btnNext.setOnClickListener {
            index++
            if (index < questions.size) {
                resetButtons()
                loadQuestion()
            } else {
                showResult()
            }
        }
    }

    private fun loadQuestion() {

        val q = questions[index]

        val options = mutableListOf(q.option1, q.option2, q.option3)

        val correctAnswer = options[q.correct - 1]

        options.shuffle()

        val newCorrectIndex = options.indexOf(correctAnswer) + 1

        currentQuestion = ShuffledQuestion(
            q.question,
            options,
            newCorrectIndex,
            q.explanation
        )

        tvQuestion.text = currentQuestion.question
        btn1.text = currentQuestion.options[0]
        btn2.text = currentQuestion.options[1]
        btn3.text = currentQuestion.options[2]

        tvScore.text = "Score: $score"
    }

    private fun checkAnswer(selected: Int) {
        if (answered) return
        answered = true

        val q = currentQuestion

        val selectedBtn = getButton(selected)
        val correctBtn = getButton(q.correctIndex)

        if (selected == q.correctIndex) {
            playWinSound()
            score++
            selectedBtn.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(correctColor))
            tvExplanation.text = "Correct! ${q.explanation}"
        } else {
            playFailSound()
            selectedBtn.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(wrongColor))
            correctBtn.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(correctColor))
            tvExplanation.text = "Oops! ${q.explanation}"
        }

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

        tvExplanation.text = ""
        answered = false
    }

    private fun showResult() {
playWinSound()
        questionBox.visibility = View.GONE
        tvExplanation.visibility = View.GONE
        btnNext.visibility = View.GONE
        tvScore.visibility = View.GONE

        listOf(btn1, btn2, btn3).forEach {
            it.visibility = View.GONE
        }

        resultLayout.visibility = View.VISIBLE

        tvFinalScore.text = "Score: $score / ${questions.size}"

        tvPerformance =
            when {
                score == questions.size ->
                    "Outstanding! Perfect score 💯"
                score >= questions.size * 0.7 ->
                    "Excellent work 🔥"
                score >= questions.size * 0.4 ->
                    "Good job 👍"
                else ->
                    "Needs practice 📘"
            }.let { result ->
                tvPerformance.text = result
                tvPerformance
            }
    }

    private fun restartQuiz() {

        index = 0
        score = 0
        answered = false
        questions = questions.shuffled()
        resultLayout.visibility = View.GONE
        gameContainer.visibility = View.VISIBLE

        listOf(btn1, btn2, btn3).forEach {
            it.visibility = View.VISIBLE
        }

        tvExplanation.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        tvScore.visibility = View.VISIBLE
        questionBox.visibility = View.VISIBLE

        resetButtons()
        loadQuestion()
    }
}