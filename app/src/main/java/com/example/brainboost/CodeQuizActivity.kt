package com.example.brainboost

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout


class CodeQuizActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvExplanation: TextView

    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btn4: Button
    private lateinit var btnNext: Button

    private lateinit var resultLayout: LinearLayout
    private lateinit var tvFinalScore: TextView
    private lateinit var tvPerformance: TextView
    private lateinit var btnRestart: Button
    private lateinit var btnExit: Button
    private lateinit var questionBox: LinearLayout
    private lateinit var tvTimer: TextView
    private lateinit var soundPool: android.media.SoundPool
    private var winSound = 0
    private var timeLeft = 10
    private var timer: android.os.CountDownTimer? = null

    private var index = 0
    private var currentCorrectIndex = 0
    private var score = 0
    private var answered = false

    private val defaultColor = Color.parseColor("#7E57C2")
    private val correctColor = Color.parseColor("#4CAF50")
    private val wrongColor = Color.parseColor("#F44336")

    private val questions = listOf(
        CodeQuestion(
            "Which keyword is used to inherit a class in Java?",
            listOf("this", "super", "extends", "implements"),
            2,
            "'extends' keyword is used for inheritance in Java."
        ),
        CodeQuestion(
            "Which method is the entry point of a Java program?",
            listOf("start()", "run()", "main()", "init()"),
            2,
            "Execution of a Java program starts from main() method."
        ),
        CodeQuestion(
            "Which symbol is used for single-line comments in Kotlin?",
            listOf("/* */", "//", "#", "<!-- -->"),
            1,
            "// is used for single-line comments."
        ),
        CodeQuestion(
            "Which keyword is used to create an object in Java?",
            listOf("make", "create", "new", "object"),
            2,
            "'new' keyword is used to create objects in Java."
        ),

        CodeQuestion(
            "Which data type is used to store true or false values in Java?",
            listOf("bool", "boolean", "bit", "truefalse"),
            1,
            "'boolean' data type stores true or false values."
        ),

        CodeQuestion(
            "Which loop is guaranteed to execute at least once?",
            listOf("for loop", "while loop", "do-while loop", "foreach loop"),
            2,
            "The do-while loop executes at least once before checking the condition."
        ),

        CodeQuestion(
            "Which keyword is used to define a constant in Kotlin?",
            listOf("val", "var", "const", "final"),
            2,
            "'const' is used to declare compile-time constants in Kotlin."
        ),

        CodeQuestion(
            "Which operator is used for equality comparison in Kotlin?",
            listOf("=", "==", "===", "!="),
            1,
            "'==' checks structural equality in Kotlin."
        ),

        CodeQuestion(
            "Which access modifier makes a member accessible only within the same class?",
            listOf("public", "protected", "private", "internal"),
            2,
            "'private' restricts access to within the same class."
        ),

        CodeQuestion(
            "Which collection does NOT allow duplicate elements in Java?",
            listOf("List", "ArrayList", "Set", "Vector"),
            2,
            "Set does not allow duplicate elements."
        ),

        CodeQuestion(
            "Which function is used to print output in Kotlin?",
            listOf("printLine()", "echo()", "println()", "write()"),
            2,
            "'println()' is used to print output with a new line in Kotlin."
        ),

        CodeQuestion(
            "Which keyword is used to handle exceptions in Java?",
            listOf("error", "try", "catch", "throw"),
            1,
            "'try' block is used to handle exceptions."
        ),

        CodeQuestion(
            "Which keyword is used to stop a loop immediately?",
            listOf("stop", "exit", "continue", "break"),
            3,
            "'break' is used to immediately terminate a loop."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_quiz)
        val audioAttr = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_GAME)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = android.media.SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttr)
            .build()

        winSound = soundPool.load(this, R.raw.win, 1)
        tvQuestion = findViewById(R.id.tvQuestion)
        questionBox = findViewById(R.id.questionBox)
        tvScore = findViewById(R.id.tvScore)
        tvExplanation = findViewById(R.id.tvExplanation)
        tvTimer = findViewById(R.id.tvTimer)
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        btn4 = findViewById(R.id.btn4)
        btnNext = findViewById(R.id.btnNext)

        resultLayout = findViewById(R.id.resultLayout)
        tvFinalScore = findViewById(R.id.tvFinalScore)
        tvPerformance = findViewById(R.id.tvPerformance)
        btnRestart = findViewById(R.id.btnRestart)
        btnExit = findViewById(R.id.btnExit)

        loadQuestion()
        resetButtons()

        btn1.setOnClickListener { checkAnswer(0) }
        btn2.setOnClickListener { checkAnswer(1) }
        btn3.setOnClickListener { checkAnswer(2) }
        btn4.setOnClickListener { checkAnswer(3) }

        btnNext.setOnClickListener {
            if (!answered) return@setOnClickListener
            index++
            if (index < questions.size) {
                resetButtons()
                loadQuestion()
            } else {
                showResult()
            }
        }

        btnRestart.setOnClickListener { restartQuiz() }
        btnExit.setOnClickListener { finish() }
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
    private fun startTimer() {

        timer?.cancel()
        timeLeft = 10
        tvTimer.text = "Time: $timeLeft"

        timer = object : android.os.CountDownTimer(10000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLeft--
                tvTimer.text = "Time: $timeLeft"
            }

            override fun onFinish() {
                tvTimer.text = "Time: 0"

                if (!answered) {
                    answered = true
                    val q = questions[index]
                    val correctBtn = getButton(currentCorrectIndex)
                    correctBtn.backgroundTintList =
                        ColorStateList.valueOf(correctColor)

                    tvExplanation.text = "⏰ Time up! ${q.explanation}"
                }
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
    }
    private fun loadQuestion() {

        tvScore.visibility = View.VISIBLE
        questionBox.visibility = View.VISIBLE
        tvExplanation.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE

        val q = questions[index]

        // shuffle options
        val optionsWithIndex = q.options.mapIndexed { i, option -> Pair(option, i) }.shuffled()

        val shuffledOptions = optionsWithIndex.map { it.first }
        val newCorrectIndex = optionsWithIndex.indexOfFirst { it.second == q.correctIndex }

        tvQuestion.text = q.question

        btn1.text = shuffledOptions[0]
        btn2.text = shuffledOptions[1]
        btn3.text = shuffledOptions[2]
        btn4.text = shuffledOptions[3]

        // store new correct index
        currentCorrectIndex = newCorrectIndex

        tvScore.text = "Score: $score"
        tvExplanation.text = ""
        answered = false

        startTimer()
    }



    private fun checkAnswer(selected: Int) {
        if (answered) return
        answered = true
        stopTimer()
        val q = questions[index]
        val selectedBtn = getButton(selected)
        val correctBtn = getButton(currentCorrectIndex)
        if (selected == currentCorrectIndex){
            playWinSound()
            score++
            selectedBtn.backgroundTintList =
                ColorStateList.valueOf(correctColor)
            tvExplanation.text = "Correct! ${q.explanation}"
        } else {
            playFailSound()
            selectedBtn.backgroundTintList =
                ColorStateList.valueOf(wrongColor)
            correctBtn.backgroundTintList =
                ColorStateList.valueOf(correctColor)
            tvExplanation.text = "Wrong! ${q.explanation}"
        }

        tvScore.text = "Score: $score"
    }

    private fun showResult() {
        playWinSound()
        // quiz UI hide
        questionBox.visibility = View.GONE
        tvExplanation.visibility = View.GONE
        tvScore.visibility = View.GONE          // 👈 THIS
        tvTimer.visibility = View.GONE
        stopTimer()

        listOf(btn1, btn2, btn3, btn4, btnNext).forEach {
            it.visibility = View.GONE
        }

        // result UI show
        resultLayout.visibility = View.VISIBLE

        tvFinalScore.text = "Score: $score / ${questions.size}"

        tvPerformance.text =
            when {
                score == questions.size ->
                    "Outstanding! Perfect score 💯"
                score >= questions.size * 0.7 ->
                    "Excellent work 🔥"
                score >= questions.size * 0.4 ->
                    "Good job 👍"
                else ->
                    "Needs practice 📘"
            }
    }


    private fun restartQuiz() {

        index = 0
        score = 0
        answered = false

        // result hide
        resultLayout.visibility = View.GONE

        // quiz UI back
        tvScore.visibility = View.VISIBLE       // 👈 BACK
        questionBox.visibility = View.VISIBLE
        tvExplanation.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        tvTimer.visibility = View.VISIBLE
        stopTimer()
        timeLeft = 10
        tvTimer.text = "Time: 10"
        listOf(btn1, btn2, btn3, btn4).forEach {
            it.visibility = View.VISIBLE
        }

        resetButtons()
        loadQuestion()
    }


    private fun resetButtons() {
        val tint = ColorStateList.valueOf(defaultColor)
        listOf(btn1, btn2, btn3, btn4).forEach {
            it.backgroundTintList = tint
            it.setTextColor(Color.WHITE)
            it.isEnabled = true
        }
        answered = false
    }

    private fun getButton(i: Int): Button =
        when (i) {
            0 -> btn1
            1 -> btn2
            2 -> btn3
            else -> btn4
        }
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
