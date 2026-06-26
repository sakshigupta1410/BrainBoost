package com.example.brainboost

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MemoryCardFlipActivity : AppCompatActivity(), CardAdapter.CardFlipListener {

    private lateinit var recycler: RecyclerView
    private lateinit var tvMoves: TextView
    private lateinit var tvMatches: TextView
    private lateinit var btnRestart: Button

    private lateinit var adapter: CardAdapter

    private var inputLocked = false
    private var moves = 0
    private var matches = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_card_flip)

        recycler = findViewById(R.id.recyclerCards)
        tvMoves = findViewById(R.id.tvMoves)
        tvMatches = findViewById(R.id.tvMatches)
        btnRestart = findViewById(R.id.btnRestart)

        adapter = CardAdapter(buildCards(), this)

        recycler.layoutManager = GridLayoutManager(this, 3) // 3 columns => 12 cards
        recycler.adapter = adapter

        updateStats()

        btnRestart.setOnClickListener {
            restartGame()
        }
    }

    private fun buildCards(): MutableList<CardModel> {
        // We assume drawable images: ic_emoji_1 .. ic_emoji_6 (6 unique images)
        val images = listOf(
            R.drawable.ic_emoji_1,
            R.drawable.ic_emoji_2,
            R.drawable.ic_emoji_3,
            R.drawable.ic_emoji_4,
            R.drawable.ic_emoji_5,
            R.drawable.ic_emoji_6,
            R.drawable.ic_emoji_7,
            R.drawable.ic_emoji_8,
            R.drawable.ic_emoji_9
        )

        val cards = mutableListOf<CardModel>()
        images.forEachIndexed { index, img ->
            // create two copies for the pair
            cards.add(CardModel(id = index * 2, imageRes = img))
            cards.add(CardModel(id = index * 2 + 1, imageRes = img))
        }

        cards.shuffle(java.util.Random(System.nanoTime()))

        return cards
    }

    private fun restartGame() {
        recycler.alpha = 0f
        recycler.animate().alpha(1f).setDuration(250).start()
        moves = 0
        matches = 0
        adapter.resetCards(buildCards())
        updateStats()
    }

    private fun updateStats() {
        tvMoves.text = "Moves: $moves"
        tvMatches.text = "Matches: $matches / 9"
    }

    // CardAdapter.CardFlipListener callbacks
    override fun onCardFlipped(position: Int, card: CardModel) {
        if (inputLocked) return
        // called when user flips a card; adapter handles flip visuals.
        val state = adapter.onUserFlip(position)

        when (state) {
            CardAdapter.FlipResult.NO_ACTION -> {
                // nothing (e.g., flipping already matched card)
            }
            CardAdapter.FlipResult.FLIPPED_ONE -> {
                // waiting for second card
            }
            CardAdapter.FlipResult.FLIPPED_TWO -> {
                moves++
                tvMoves.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(80)
                    .withEndAction {
                        tvMoves.animate().scaleX(1f).scaleY(1f).duration = 80
                    }
                updateStats()

                // check for match
                if (adapter.isLastTwoMatch()) {
                    recycler.animate()
                        .scaleX(1.03f)
                        .scaleY(1.03f)
                        .setDuration(120)
                        .withEndAction {
                            recycler.animate().scaleX(1f).scaleY(1f).duration = 120
                        }
                    // matched
                    matches++
                    updateStats()
                    adapter.markLastTwoAsMatched()

                    // check win
                    if (matches >= 9) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            showWinDialog()
                        }, 400)
                    }
                } else {
                    // not matched -> flip back after delay
                    inputLocked = true

                    Handler(Looper.getMainLooper()).postDelayed({
                        adapter.hideLastTwo()
                        inputLocked = false
                    }, 700)
                }
            }
        }
    }

    private fun showWinDialog() {

        val dialog = AlertDialog.Builder(this)
            .setTitle("🧠 Memory Master!")
            .setMessage("Completed in $moves moves")
            .setPositiveButton("Play Again") { d, _ ->
                restartGame()
                d.dismiss()
            }
            .setNegativeButton("Close") { d, _ -> d.dismiss() }
            .create()

        dialog.show()

        dialog.window?.decorView?.animate()
            ?.alpha(0f)?.setDuration(0)?.withEndAction {
                dialog.window?.decorView?.animate()?.alpha(1f)?.duration = 250
            }
    }
}
