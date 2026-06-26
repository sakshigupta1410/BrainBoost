package com.example.brainboost

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Collections

class Puzzle15Activity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var restartButton: Button
    private lateinit var tvTimer: TextView
    private lateinit var tvBestTime: TextView

    private val gridSize = 4   // ⭐ 4x4 puzzle
    private val numbers = MutableList(gridSize * gridSize){ it+1 }.apply{ this[lastIndex]=0 }
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

        gridLayout = findViewById(R.id.gridLayout)
        restartButton = findViewById(R.id.btnRestart)
        tvTimer = findViewById(R.id.tvTimer)
        tvBestTime = findViewById(R.id.tvBestTime)

        gridLayout.rowCount = gridSize
        gridLayout.columnCount = gridSize

        val prefs = getSharedPreferences("brainboost_puzzle15", MODE_PRIVATE)
        bestTime = prefs.getInt("BEST_TIME", Int.MAX_VALUE)
        updateBestTimeUI()

        startGame()

        restartButton.setOnClickListener { startGame() }
    }

    // ---------------- GAME START ----------------

    private fun startGame(){

        stopTimer()
        seconds = 0
        tvTimer.text = "Time: 00:00"

        gridLayout.removeAllViews()
        tiles.clear()

        do{
            numbers.shuffle()
        }while(!isSolvable(numbers) || isSolved())

        for(i in numbers.indices){

            val tile = TextView(this)
            tile.textSize = 18f
            tile.setTextColor(Color.WHITE)
            tile.gravity = android.view.Gravity.CENTER
            tile.setBackgroundColor(Color.parseColor("#3F51B5"))

            val params = GridLayout.LayoutParams().apply{
                width = 0
                height = 0
                rowSpec = GridLayout.spec(i/gridSize,1f)
                columnSpec = GridLayout.spec(i%gridSize,1f)
                setMargins(8,8,8,8)
            }

            tile.layoutParams = params

            val value = numbers[i]

            if(value==0){
                tile.text=""
                tile.setBackgroundColor(Color.TRANSPARENT)
            }else{
                tile.text=value.toString()
            }

            tile.setOnClickListener { moveTile(i) }

            tiles.add(tile)
            gridLayout.addView(tile)
        }
    }

    // ---------------- TIMER ----------------

    private fun startTimer(){
        if(isTimerRunning) return
        isTimerRunning=true

        timerRunnable = object:Runnable{
            override fun run(){
                seconds++
                val min=seconds/60
                val sec=seconds%60
                tvTimer.text=String.format("Time: %02d:%02d",min,sec)
                handler.postDelayed(this,1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun stopTimer(){
        timerRunnable?.let{handler.removeCallbacks(it)}
        isTimerRunning=false
    }

    // ---------------- LOGIC ----------------

    private fun moveTile(position:Int){

        if(!isTimerRunning) startTimer()

        val emptyIndex = numbers.indexOf(0)

        if(isAdjacent(position,emptyIndex)){
            Collections.swap(numbers,position,emptyIndex)
            updateTiles()

            if(isSolved()){
                playWinSound()
                stopTimer()

                if(seconds < bestTime){
                    bestTime = seconds
                    val prefs=getSharedPreferences("brainboost_puzzle15",MODE_PRIVATE)
                    prefs.edit().putInt("BEST_TIME",bestTime).apply()
                    updateBestTimeUI()
                }

                tiles.forEach{
                    it.animate().rotationBy(360f).setDuration(300).start()
                }

                Toast.makeText(
                    this,
                    "15 Puzzle Complete 🧠✨\n${tvTimer.text}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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
    private fun updateTiles(){
        for(i in numbers.indices){
            val value=numbers[i]
            val tile=tiles[i]

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

        val min=bestTime/60
        val sec=bestTime%60
        tvBestTime.text=String.format("Best: %02d:%02d",min,sec)
    }

    private fun isAdjacent(p1:Int,p2:Int):Boolean{
        val r1=p1/gridSize
        val c1=p1%gridSize
        val r2=p2/gridSize
        val c2=p2%gridSize
        return Math.abs(r1-r2)+Math.abs(c1-c2)==1
    }

    private fun isSolved():Boolean{
        val solved = MutableList(gridSize*gridSize){it+1}.apply{ this[lastIndex]=0 }
        return numbers==solved
    }

    // ⭐ 15 Puzzle solvable rule (different from 8 puzzle)
    private fun isSolvable(list: List<Int>):Boolean{

        var inversions=0
        for(i in list.indices){
            for(j in i+1 until list.size){
                if(list[i]!=0 && list[j]!=0 && list[i]>list[j]){
                    inversions++
                }
            }
        }

        val emptyRow = list.indexOf(0)/gridSize
        return (inversions + emptyRow) % 2 == 0
    }
}