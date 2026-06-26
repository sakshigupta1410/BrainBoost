package com.example.brainboost

import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class FoodChainActivity : AppCompatActivity() {

    private lateinit var slot1: TextView
    private lateinit var slot2: TextView
    private lateinit var slot3: TextView
    private lateinit var slot4: TextView

    private lateinit var drag1: TextView
    private lateinit var drag2: TextView
    private lateinit var drag3: TextView
    private lateinit var drag4: TextView

    private val levels = listOf(

        // 🌱 BASIC / EASY
        listOf("🌱 Grass","🐭 Mouse","🐍 Snake","🦅 Eagle"),
        listOf("🌾 Wheat","🦗 Grasshopper","🐸 Frog","🦅 Hawk"),
        listOf("🍃 Leaves","🐛 Caterpillar","🐦 Bird","🦊 Fox"),
        listOf("🌱 Plants","🐰 Rabbit","🦊 Fox","🦠 Bacteria"),
        listOf("🌱 Grass","🐄 Cow","👨 Human","🦠 Bacteria"),

        // 🌊 AQUATIC
        listOf("🟢 Algae","🐟 Fish","🦭 Seal","🦈 Shark"),
        listOf("🌊 Phytoplankton","🦐 Zooplankton","🐠 Small Fish","🦈 Shark"),
        listOf("🌿 Seaweed","🦀 Crab","🐙 Octopus","🦈 Shark"),
        listOf("🌊 Plankton","🐟 Fish","🐧 Penguin","🦭 Seal"),
        listOf("🌿 Aquatic Plants","🐌 Snail","🐟 Fish","🦆 Duck"),

        // 🌳 FOREST / LAND
        listOf("🌳 Trees","🦌 Deer","🐅 Tiger","🦠 Bacteria"),
        listOf("🍎 Fruits","🐒 Monkey","🐆 Leopard","🦠 Bacteria"),
        listOf("🌱 Grass","🐐 Goat","🐅 Tiger","🦠 Bacteria"),
        listOf("🌿 Leaves","🐜 Insect","🦎 Lizard","🦅 Eagle"),
        listOf("🌱 Plants","🐭 Mouse","🦉 Owl","🦠 Bacteria"),

        // 🏜️ DESERT
        listOf("🌵 Cactus","🐜 Insect","🦎 Lizard","🦅 Hawk"),
        listOf("🌾 Shrubs","🐇 Rabbit","🦊 Fox","🦠 Bacteria"),

        // ❄️ COLD / POLAR
        listOf("🌊 Phytoplankton","🦐 Krill","🐟 Fish","🐻‍❄️ Polar Bear"),
        listOf("🌊 Algae","🐟 Fish","🦭 Seal","🐻‍❄️ Polar Bear"),

        // 🔥 LONGER / HARD
        listOf("🌱 Grass","🦗 Grasshopper","🐸 Frog","🐍 Snake","🦅 Eagle"),
        listOf("🌿 Leaves","🐛 Caterpillar","🐦 Bird","🐍 Snake","🦅 Hawk"),
        listOf("🌊 Phytoplankton","🦐 Zooplankton","🐠 Small Fish","🐟 Big Fish","🦈 Shark"),
        listOf("🌱 Plants","🐜 Insect","🐸 Frog","🐦 Bird","🐍 Snake","🦅 Eagle")
    )


    private var currentLevel = 0
    private lateinit var correctOrder: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_chain)

        slot1 = findViewById(R.id.slot1)
        slot2 = findViewById(R.id.slot2)
        slot3 = findViewById(R.id.slot3)
        slot4 = findViewById(R.id.slot4)

        drag1 = findViewById(R.id.itemGrass)
        drag2 = findViewById(R.id.itemMouse)
        drag3 = findViewById(R.id.itemSnake)
        drag4 = findViewById(R.id.itemEagle)

        // 🔥 Slot indexes (dynamic logic)
        slot1.tag = 0
        slot2.tag = 1
        slot3.tag = 2
        slot4.tag = 3

        enableDrag(drag1)
        enableDrag(drag2)
        enableDrag(drag3)
        enableDrag(drag4)

        enableDrop(slot1)
        enableDrop(slot2)
        enableDrop(slot3)
        enableDrop(slot4)

        findViewById<Button>(R.id.btnCheck).setOnClickListener {
            checkAnswer()
        }

        loadLevel()
    }

    // ================= DRAG =================

    private fun enableDrag(tv: TextView) {
        tv.setOnLongClickListener {
            val data = ClipData.newPlainText("", tv.text)
            val shadow = View.DragShadowBuilder(tv)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tv.startDragAndDrop(data, shadow, tv, 0)
            } else {
                tv.startDrag(data, shadow, tv, 0)
            }
            true
        }
    }

    // ================= DROP =================

    private fun enableDrop(slot: TextView) {
        slot.setOnDragListener { view, event ->
            when (event.action) {

                DragEvent.ACTION_DROP -> {

                    val dragged = event.localState as TextView
                    val droppedText = dragged.text.toString()

                    slot.text = droppedText
                    slot.setBackgroundResource(R.drawable.slot_filled)
                }
            }
            true
        }
    }


    // ================= LEVEL LOAD =================

    private fun loadLevel() {

        correctOrder = levels[currentLevel]
        val shuffled = correctOrder.shuffled()

        drag1.text = shuffled[0]
        drag2.text = shuffled[1]
        drag3.text = shuffled[2]
        drag4.text = shuffled[3]

        resetSlots()
    }

    // ================= RESET =================

    private fun resetSlots() {
        slot1.text = "Producer"
        slot2.text = "Primary Consumer"
        slot3.text = "Secondary Consumer"
        slot4.text = "Tertiary Consumer"

        slot1.setBackgroundResource(R.drawable.slot_empty)
        slot2.setBackgroundResource(R.drawable.slot_empty)
        slot3.setBackgroundResource(R.drawable.slot_empty)
        slot4.setBackgroundResource(R.drawable.slot_empty)
    }

    // ================= CHECK ANSWER =================

    private fun checkAnswer() {

        val userOrder = listOf(
            slot1.text.toString(),
            slot2.text.toString(),
            slot3.text.toString(),
            slot4.text.toString()
        )

        if (userOrder == correctOrder) {
            playWinSound()
            AlertDialog.Builder(this)

                .setTitle("✔ Correct!")
                .setMessage("Great! Moving to next level 🙂")
                .setPositiveButton("Next") { _, _ ->
                    currentLevel++
                    if (currentLevel >= levels.size) {
                        currentLevel = 0
                        Toast.makeText(this, "Restarting 🎉", Toast.LENGTH_LONG).show()
                    }
                    loadLevel()
                }
                .show()

        } else {
            playFailSound()
            AlertDialog.Builder(this)
                .setTitle("❌ Wrong")
                .setMessage("Try again 🙂")
                .setPositiveButton("Retry") { _, _ ->
                    resetSlots()
                }
                .show()
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
}
