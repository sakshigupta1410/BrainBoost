package com.example.brainboost

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Collections

class PuzzleGameActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var restartButton: Button
    private lateinit var tvTimer: TextView
    private lateinit var tvBestTime: TextView
    private lateinit var soundPool: android.media.SoundPool
    private var winSound = 0
    private val numbers = mutableListOf(1,2,3,4,5,6,7,8,0)
    private val tiles = mutableListOf<TextView>()

    // ⏱ TIMER
    private var seconds = 0
    private var isTimerRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    // 🏆 BEST TIME
    private var bestTime = Int.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle_game)
        val audioAttr = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_GAME)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = android.media.SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttr)
            .build()

        winSound = soundPool.load(this, R.raw.win, 1)
        gridLayout = findViewById(R.id.gridLayout)
        restartButton = findViewById(R.id.btnRestart)
        tvTimer = findViewById(R.id.tvTimer)
        tvBestTime = findViewById(R.id.tvBestTime)

        val prefs = getSharedPreferences("brainboost_puzzle", MODE_PRIVATE)
        bestTime = prefs.getInt("BEST_TIME", Int.MAX_VALUE)
        updateBestTimeUI()

        startGame()

        restartButton.setOnClickListener {
            startGame()
        }
    }
    private fun playWinSound() {
        soundPool.play(winSound,1f,1f,1,0,1f)
    }
    // ---------------- GAME START ----------------

    private fun startGame() {

        stopTimer()
        seconds = 0
        tvTimer.text = "Time: 00:00"

        gridLayout.removeAllViews()
        tiles.clear()

        do {
            numbers.shuffle()
        } while (!isSolvable(numbers) || isSolved())

        for (i in numbers.indices) {

            val tile = TextView(this)
            tile.textSize = 22f
            tile.setTextColor(Color.WHITE)
            tile.gravity = android.view.Gravity.CENTER
            tile.setBackgroundColor(Color.parseColor("#3F51B5"))

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 0
            params.rowSpec = GridLayout.spec(i/3,1f)
            params.columnSpec = GridLayout.spec(i%3,1f)
            params.setMargins(8,8,8,8)
            tile.layoutParams = params

            val value = numbers[i]

            if(value==0){
                tile.text=""
                tile.setBackgroundColor(Color.TRANSPARENT)
            }else{
                tile.text=value.toString()
            }

            tile.setOnClickListener {
                moveTile(i)
            }

            tiles.add(tile)
            gridLayout.addView(tile)
        }
    }

    // ---------------- TIMER ----------------

    private fun startTimer(){
        if(isTimerRunning) return

        isTimerRunning = true

        timerRunnable = object : Runnable{
            override fun run() {
                seconds++
                val min = seconds/60
                val sec = seconds%60
                tvTimer.text = String.format("Time: %02d:%02d",min,sec)
                handler.postDelayed(this,1000)
            }
        }

        handler.post(timerRunnable!!)
    }

    private fun stopTimer(){
        timerRunnable?.let { handler.removeCallbacks(it) }
        isTimerRunning = false
    }

    // ---------------- LOGIC ----------------

    private fun isSolvable(list: List<Int>): Boolean {
        var inversions = 0
        for(i in 0 until list.size){
            for(j in i+1 until list.size){
                if(list[i]!=0 && list[j]!=0 && list[i]>list[j]){
                    inversions++
                }
            }
        }
        return inversions%2==0
    }

    private fun moveTile(position:Int){

        if(!isTimerRunning){
            startTimer()   // 👈 first move start timer
        }

        val emptyIndex = numbers.indexOf(0)

        if(isAdjacent(position,emptyIndex)){
            Collections.swap(numbers,position,emptyIndex)
            updateTiles()

            tiles[position].animate()
                .scaleX(0.8f).scaleY(0.8f).setDuration(80)
                .withEndAction{
                    tiles[position].animate().scaleX(1f).scaleY(1f).duration=80
                }

            if(isSolved()){
                playWinSound()
                stopTimer()

                // 🏆 BEST TIME CHECK
                if(seconds < bestTime){
                    bestTime = seconds
                    val prefs = getSharedPreferences("brainboost_puzzle",MODE_PRIVATE)
                    prefs.edit().putInt("BEST_TIME",bestTime).apply()
                    updateBestTimeUI()
                }

                tiles.forEach{
                    it.animate().rotationBy(360f).setDuration(300).start()
                }

                Toast.makeText(
                    this,
                    "BrainBoost Complete 🧠✨\n${tvTimer.text}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateTiles(){
        for(i in numbers.indices){
            val value = numbers[i]
            val tile = tiles[i]

            if(value==0){
                tile.alpha=0.3f
                tile.text=""
                tile.setBackgroundColor(Color.TRANSPARENT)
            }else{
                tile.alpha=1f
                tile.text=value.toString()
                tile.setBackgroundResource(R.drawable.bg_option_default)
            }
        }
    }

    private fun updateBestTimeUI(){

        if(bestTime==Int.MAX_VALUE){
            tvBestTime.text="Best: --:--"
            return
        }

        val min = bestTime/60
        val sec = bestTime%60
        tvBestTime.text = String.format("Best: %02d:%02d",min,sec)
    }

    private fun isAdjacent(p1:Int,p2:Int):Boolean{
        val r1=p1/3
        val c1=p1%3
        val r2=p2/3
        val c2=p2%3
        return Math.abs(r1-r2)+Math.abs(c1-c2)==1
    }

    private fun isSolved():Boolean{
        return numbers == listOf(1,2,3,4,5,6,7,8,0)
    }
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}