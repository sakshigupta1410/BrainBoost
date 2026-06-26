package com.example.brainboost

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.ClipData
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MindOfMoleculeActivity : AppCompatActivity() {

    private var level = 1
    private lateinit var soundPool: SoundPool
    private var winSound = 0
    private var failSound = 0
    private var runningAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .build()

        winSound = soundPool.load(this, R.raw.win, 1)
        failSound = soundPool.load(this, R.raw.fail, 1)
        loadLevel()
    }

    // ================= LEVEL ROUTER =================

    private fun loadLevel() {
        when (level) {
            1 -> levelMotion()
            2 -> levelDrag()
            3 -> levelSequence()
            4 -> levelStateChange()
            5 -> levelBondBuilder()
            6 -> levelCollision()
            else -> showGameComplete()
        }
    }

    private fun nextLevel(msg: String) {

        // 🔊 PLAY WIN SOUND
        soundPool.play(
            winSound,
            1f, 1f,   // left & right volume
            1,        // priority
            0,        // no loop
            1f        // normal speed
        )

        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.star_big_on)
            .setTitle("⭐ Level $level Complete")
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("Next") { _, _ ->
                level++
                loadLevel()
            }
            .setNegativeButton("Play Again") { _, _ ->
                loadLevel()
            }
            .show()
    }

    private fun showGameComplete() {
        AlertDialog.Builder(this)
            .setTitle("🎉 Game Finished")
            .setMessage("You successfully thought like a molecule!")
            .setCancelable(false)
            .setPositiveButton("Play Again") { _, _ ->
                level = 1
                loadLevel()
            }
            .setNegativeButton("Close") { _, _ -> finish() }
            .show()
    }

    // ================= COMMON HELPERS =================

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun startSafeDrag(view: View, label: String) {
        val data = ClipData.newPlainText(label, label)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(
                data,
                View.DragShadowBuilder(view),
                null,
                0
            )
        } else {
            @Suppress("DEPRECATION")
            view.startDrag(
                data,
                View.DragShadowBuilder(view),
                null,
                0
            )
        }
    }
    private fun enableDrag(tv: TextView) {
        tv.setOnLongClickListener {
            startSafeDrag(it, tv.text.toString())
            true
        }
    }

    private fun pulse(view: View) {
        view.animate().scaleX(1.15f).scaleY(1.15f).setDuration(120)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).duration = 120
            }
    }
    private fun playFailSound() {
        soundPool.play(
            failSound,
            1f, 1f,
            1,
            0,
            1f
        )
    }
    // ================= LEVEL 1 =================

    private fun levelMotion() {

        setContentView(R.layout.activity_motion_game)

        findViewById<Button>(R.id.btnFast).setOnClickListener {
            nextLevel("Correct! Heat increases molecular motion.")
        }

        findViewById<Button>(R.id.btnStay).setOnClickListener {
            playFailSound()
            toast("Wrong! Molecules move faster on heating.")
        }

        findViewById<Button>(R.id.btnSlow).setOnClickListener {
            playFailSound()
            toast("Wrong! Think about heat energy.")
        }
    }

    // ================= LEVEL 2 =================

    private fun levelDrag() {

        setContentView(R.layout.activity_drag_game)

        val molecule = findViewById<ImageView>(R.id.molecule)
        val waterZone = findViewById<TextView>(R.id.waterZone)

        var done = false

        molecule.setOnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_MOVE && !done) {
                v.x = e.rawX - v.width / 2
                v.y = e.rawY - v.height / 2

                if (v.y > waterZone.y) {
                    done = true
                    pulse(v)
                    nextLevel("Correct! CO₂ dissolves in water.")
                }
            }
            true
        }
    }

    // ================= LEVEL 3 =================

    private fun levelSequence() {

        setContentView(R.layout.activity_sequence_game)

        val step1 = findViewById<TextView>(R.id.step1)
        val step2 = findViewById<TextView>(R.id.step2)
        val step3 = findViewById<TextView>(R.id.step3)

        val slot1 = findViewById<TextView>(R.id.slot1)
        val slot2 = findViewById<TextView>(R.id.slot2)
        val slot3 = findViewById<TextView>(R.id.slot3)

        val slots = listOf(slot1, slot2, slot3)

        listOf(step1, step2, step3).forEach { enableDrag(it) }

        val correct = listOf(
            "CO₂ enters leaf",
            "Sunlight absorbed",
            "Food formed"
        )

        val dropListener = View.OnDragListener { v, e ->

            if (e.action == DragEvent.ACTION_DROP) {

                val slot = v as TextView
                val text = e.clipData.getItemAt(0).text.toString()

                if (slot.text != "Step 1" &&
                    slot.text != "Step 2" &&
                    slot.text != "Step 3"
                ) {
                    toast("Slot already filled")
                    return@OnDragListener true
                }

                slot.text = text
                slot.setBackgroundResource(R.color.green)
                pulse(slot)

                val current = listOf(
                    slot1.text.toString(),
                    slot2.text.toString(),
                    slot3.text.toString()
                )

// Check if all slots filled
                if (!current.contains("Step 1") &&
                    !current.contains("Step 2") &&
                    !current.contains("Step 3")
                ) {

                    if (current == correct) {

                        slots.forEach {
                            it.animate().rotationBy(360f).setDuration(300).start()
                        }

                        nextLevel("Perfect! Photosynthesis completed.")

                    } else {

                        toast("Wrong sequence! Try again.")

                        // Restart sequence after small delay
                        slot1.postDelayed({
                            resetSequence(slot1, slot2, slot3)
                        }, 800)
                    }
                }
            }
            true
        }

        slot1.setOnDragListener(dropListener)
        slot2.setOnDragListener(dropListener)
        slot3.setOnDragListener(dropListener)
    }
    private fun resetSequence(
        slot1: TextView,
        slot2: TextView,
        slot3: TextView
    ) {
        slot1.text = "Step 1"
        slot2.text = "Step 2"
        slot3.text = "Step 3"

        slot1.setBackgroundResource(R.color.purple_200)
        slot2.setBackgroundResource(R.color.purple_200)
        slot3.setBackgroundResource(R.color.purple_200)
    }
    // ================= LEVEL 4 =================

    private fun levelStateChange() {

        setContentView(R.layout.activity_state_change)

        val gas = findViewById<TextView>(R.id.gasBox)
        val molecule = findViewById<TextView>(R.id.moleculeItem)

        molecule.setOnLongClickListener {
            val data = ClipData.newPlainText("", "gas")
            molecule.setOnLongClickListener {
                startSafeDrag(it, "gas")
                true
            }
            true
        }

        val dropListener = View.OnDragListener { v, e ->
            if (e.action == DragEvent.ACTION_DROP) {
                if (v.id == R.id.gasBox)
                    nextLevel("Correct! Heat turns molecules into gas.")
                else {
                    playFailSound()
                    toast("Wrong state! Think about heat.")
                }
            }
            true
        }

        findViewById<TextView>(R.id.solidBox).setOnDragListener(dropListener)
        findViewById<TextView>(R.id.liquidBox).setOnDragListener(dropListener)
        gas.setOnDragListener(dropListener)
    }

    // ================= LEVEL 5 =================

    private fun levelBondBuilder() {

        setContentView(R.layout.activity_bond_builder)

        val slot1 = findViewById<TextView>(R.id.slotA)
        val slot2 = findViewById<TextView>(R.id.slotB)
        val slot3 = findViewById<TextView>(R.id.slotC)

        val slots = listOf(slot1, slot2, slot3)

        listOf(
            findViewById<TextView>(R.id.atomH1),
            findViewById<TextView>(R.id.atomH2),
            findViewById<TextView>(R.id.atomO)
        ).forEach { enableDrag(it) }


        val drop = View.OnDragListener { v, e ->

            if (e.action == DragEvent.ACTION_DROP) {

                val slot = v as TextView
                val text = e.clipData.getItemAt(0).text.toString()

                if (slot.text != "_") {
                    toast("Slot already filled")
                    return@OnDragListener true
                }

                slot.text = text
                pulse(slot)

                val current = listOf(
                    slot1.text.toString(),
                    slot2.text.toString(),
                    slot3.text.toString()
                )

                if (!current.contains("_")) {

                    if (current == listOf("H", "O", "H")) {

                        slots.forEach {
                            it.animate().rotationBy(360f).setDuration(300).start()
                        }

                        nextLevel("Perfect! You built H₂O.")

                    } else {

                        playFailSound()
                        toast("Wrong structure!")

                        slot1.postDelayed({

                            slots.forEach {
                                it.text = "_"
                                it.animate().alpha(0.3f).setDuration(120)
                                    .withEndAction { it.alpha = 1f }
                            }

                        }, 700)
                    }
                }
            }
            true
        }

        slot1.setOnDragListener(drop)
        slot2.setOnDragListener(drop)
        slot3.setOnDragListener(drop)
    }

    // ================= LEVEL 6 =================

    private fun levelCollision() {

        setContentView(R.layout.activity_collision_game)

        val moleculeA = findViewById<ImageView>(R.id.moleculeA)
        val moleculeB = findViewById<ImageView>(R.id.moleculeB)

        var reacted = false

        runningAnimator = ObjectAnimator.ofFloat(
            moleculeB,"translationX",0f,400f
        ).apply {
            duration = 2000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }

        moleculeA.setOnTouchListener { v, e ->

            if (e.action == MotionEvent.ACTION_MOVE && !reacted) {

                v.x = e.rawX - v.width/2
                v.y = e.rawY - v.height/2

                if (isColliding(moleculeA, moleculeB)) {

                    reacted = true

                    moleculeA.animate().rotationBy(360f).setDuration(300).start()

                    moleculeB.animate().alpha(0.5f).setDuration(150)
                        .withEndAction {
                            moleculeB.alpha = 1f
                            nextLevel("Perfect collision! Reaction occurred.")
                        }
                }
            }
            true
        }
    }

    private fun isColliding(v1: View, v2: View): Boolean {
        return v1.x < v2.x + v2.width &&
                v1.x + v1.width > v2.x &&
                v1.y < v2.y + v2.height &&
                v1.y + v1.height > v2.y
    }

    override fun onPause() {
        super.onPause()
        runningAnimator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}