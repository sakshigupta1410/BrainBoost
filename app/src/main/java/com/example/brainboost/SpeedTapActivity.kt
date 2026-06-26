package com.example.brainboost

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt
import kotlin.random.Random

class SpeedTapActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvNumber: TextView
    private lateinit var tapCircle: View
    private lateinit var gameArea: FrameLayout

    private lateinit var hearts: List<ImageView>

    private lateinit var tvFinalScore: TextView
    private lateinit var resultLayout: FrameLayout
    private lateinit var btnRestart: Button
    private lateinit var btnExit: Button

    private var score = 0
    private var lives = 3
    private var tappedThisRound = false

    private var gameTimer: CountDownTimer? = null
    private var numberTimer: CountDownTimer? = null

    private val totalTime = 60_000L

    enum class MissionType { EVEN, ODD, MULTIPLE_OF_3, PRIME }

    private var currentMissionIndex = 0
    private val missionCycle = MissionType.values().toList().shuffled()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_tap)

        tvTimer = findViewById(R.id.tvTimer)
        tvScore = findViewById(R.id.tvScore)
        tvQuestion = findViewById(R.id.tvQuestion)
        tvNumber = findViewById(R.id.tvNumber)
        tapCircle = findViewById(R.id.tapCircle)
        gameArea = findViewById(R.id.gameArea)

        resultLayout = findViewById(R.id.resultLayout)
        tvFinalScore = findViewById(R.id.tvFinalScore)
        btnRestart = findViewById(R.id.btnRestart)
        btnExit = findViewById(R.id.btnExit)

        hearts = listOf(
            findViewById(R.id.heart1),
            findViewById(R.id.heart2),
            findViewById(R.id.heart3)
        )

        tapCircle.setOnClickListener { handleTap() }

        startGame()
    }

    private fun startGame() {
        gameTimer?.cancel()
        numberTimer?.cancel()
        score = 0
        lives = 3
        currentMissionIndex = 0

        tvScore.text = "Score: 0"
        updateLivesUI()

        startGameTimer()
        startNewRound()
    }

    private fun startGameTimer() {
        gameTimer?.cancel()
        gameTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(ms: Long) {
                val sec = ms / 1000
                tvTimer.text = "${sec}s"
            }

            override fun onFinish() {
                endGame()
            }
        }.start()
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
    private fun handleTap() {
        tappedThisRound = true
        val num = tvNumber.text.toString().toInt()

        val correct = when (missionCycle[currentMissionIndex]) {
            MissionType.EVEN -> num % 2 == 0
            MissionType.ODD -> num % 2 != 0
            MissionType.MULTIPLE_OF_3 -> num % 3 == 0
            MissionType.PRIME -> isPrime(num)
        }

        if (correct) {
            playWinSound()
            score++
            tvScore.text = "Score: $score"
            animateTap()
            correctFeedback()
        } else {
            playFailSound()
            lives--
            updateLivesUI()
            wrongFeedback()

            if (lives <= 0) {
                endGame()
                return
            }
        }

        startNewRound()
    }

    private fun startNewRound(fromTimer: Boolean = false) {

        if (fromTimer && !tappedThisRound) {
            val num = tvNumber.text.toString().toInt()
            val shouldTap = when (missionCycle[currentMissionIndex]) {
                MissionType.EVEN -> num % 2 == 0
                MissionType.ODD -> num % 2 != 0
                MissionType.MULTIPLE_OF_3 -> num % 3 == 0
                MissionType.PRIME -> isPrime(num)
            }
            if (!shouldTap) skipFeedback()
        }

        tappedThisRound = false
        numberTimer?.cancel()

        currentMissionIndex = (currentMissionIndex + 1) % missionCycle.size
        showMission()
        showNumber()
        moveCircleRandom()

        val speed = getCurrentSpeed()
        numberTimer = object : CountDownTimer(speed, speed) {
            override fun onTick(ms: Long) {}
            override fun onFinish() {
                startNewRound(true)
            }
        }.start()
    }

    private fun showMission() {
        tvQuestion.animate().alpha(0f).setDuration(80).withEndAction {
            tvQuestion.alpha = 1f
        }.start()
        tvQuestion.text = when (missionCycle[currentMissionIndex]) {
            MissionType.EVEN -> "Tap EVEN numbers"
            MissionType.ODD -> "Tap ODD numbers"
            MissionType.MULTIPLE_OF_3 -> "Tap MULTIPLES OF 3"
            MissionType.PRIME -> "Tap PRIME numbers"
        }
    }

    private fun showNumber() {
        tvNumber.text = Random.nextInt(1, 50).toString()
        tvNumber.setTypeface(null, Typeface.BOLD)
        tvNumber.scaleX = 0.8f
        tvNumber.scaleY = 0.8f
        tvNumber.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
    }

    private fun moveCircleRandom() {
        gameArea.post {
            val maxX = gameArea.width - tapCircle.width
            val maxY = gameArea.height - tapCircle.height
            if (maxX > 0 && maxY > 0) {
                tapCircle.animate()
                    .x(Random.nextInt(0, maxX).toFloat())
                    .y(Random.nextInt(0, maxY).toFloat())
                    .setDuration(220)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
        }
    }

    private fun getCurrentSpeed(): Long =
        when {
            score > 25 -> 900L
            score > 15 -> 1200L
            score > 5 -> 1500L
            else -> 2000L
        }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        for (i in 2..sqrt(n.toDouble()).toInt())
            if (n % i == 0) return false
        return true
    }

    private fun updateLivesUI() {
        for (i in hearts.indices) {
            hearts[i].setImageResource(
                if (i < lives) R.drawable.ic_heart_full
                else R.drawable.ic_heart_empty
            )
        }
    }

    private fun correctFeedback() {
        tapCircle.setBackgroundResource(R.drawable.circle_correct)
        tapCircle.postDelayed({
            tapCircle.setBackgroundResource(R.drawable.circle_bg)
        }, 150)
    }

    private fun wrongFeedback() {
        tapCircle.setBackgroundResource(R.drawable.circle_wrong)
        tapCircle.postDelayed({
            tapCircle.setBackgroundResource(R.drawable.circle_bg)
        }, 150)
    }

    private fun skipFeedback() {
        tapCircle.setBackgroundResource(R.drawable.circle_skip)
        tapCircle.postDelayed({
            tapCircle.setBackgroundResource(R.drawable.circle_bg)
        }, 120)
    }

    private fun animateTap() {
        ObjectAnimator.ofFloat(tapCircle, View.SCALE_X, 1f, 0.9f, 1f).apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun endGame() {

        if (isFinishing || isDestroyed) return

        gameTimer?.cancel()
        numberTimer?.cancel()

        hearts.forEach { it.visibility = View.GONE }

        // 👇 game freeze
        gameArea.animate().alpha(0.3f).setDuration(250).start()
        tapCircle.visibility = View.GONE

        // 👇 premium result entry (Science Detective style)
        resultLayout.apply {
            visibility = View.VISIBLE
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        tvFinalScore.text = "Score: $score"

        btnRestart.setOnClickListener {

            // 👇 reset animation
            resultLayout.animate().alpha(0f).setDuration(150).withEndAction {

                startGame()

                resultLayout.visibility = View.GONE
                gameArea.visibility = View.VISIBLE
                tapCircle.visibility = View.VISIBLE

                gameArea.alpha = 1f
                hearts.forEach { it.visibility = View.VISIBLE }
            }.start()
        }

        btnExit.setOnClickListener { finish() }
    }
}
