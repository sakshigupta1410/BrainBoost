package com.example.brainboost

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.floor

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onMovePlayed: (() -> Unit)? = null
    var onPawnPromotion: ((Int, Int) -> Unit)? = null

    // ---------- BOARD COLORS ----------
    private val light = Paint().apply {
        color = Color.parseColor("#EEEED2")
        isAntiAlias = true
    }

    private val dark = Paint().apply {
        color = Color.parseColor("#769656")
        isAntiAlias = true
    }

    private val selectPaint = Paint().apply {
        color = Color.parseColor("#AA4FC3F7")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val checkPaint = Paint().apply {
        color = Color.parseColor("#88FF1744")
        style = Paint.Style.FILL
    }

    private val lastMovePaint = Paint().apply {
        color = Color.parseColor("#8842A5F5")
    }

    private val dotPaint = Paint().apply {
        color = Color.parseColor("#66000000")
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#333333")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private var cell = 0f
    private var selected: Pair<Int, Int>? = null
    private var lastMove: Move? = null

    private val capturedWhite = mutableListOf<Int>()
    private val capturedBlack = mutableListOf<Int>()

    private val cache = mutableMapOf<Int, Bitmap>()

    // ---------- FORCE SQUARE BOARD ----------
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {

        val size = minOf(width, height).toFloat()
        cell = size / 8f

        // ---- Draw Board ----
        for (r in 0..7) {
            for (c in 0..7) {
                canvas.drawRect(
                    c * cell,
                    r * cell,
                    (c + 1) * cell,
                    (r + 1) * cell,
                    if ((r + c) % 2 == 0) light else dark
                )
            }
        }

        // ---- Last Move Highlight ----
        lastMove?.let {
            canvas.drawRect(
                it.tc * cell,
                it.tr * cell,
                (it.tc + 1) * cell,
                (it.tr + 1) * cell,
                lastMovePaint
            )
        }

        // ---- Check Highlight ----
        ChessEngine.kingInCheck(PieceColor.WHITE)?.let {
            canvas.drawRect(
                it.second * cell,
                it.first * cell,
                (it.second + 1) * cell,
                (it.first + 1) * cell,
                checkPaint
            )
        }

        ChessEngine.kingInCheck(PieceColor.BLACK)?.let {
            canvas.drawRect(
                it.second * cell,
                it.first * cell,
                (it.second + 1) * cell,
                (it.first + 1) * cell,
                checkPaint
            )
        }

        // ---- Selected Square ----
        selected?.let {
            canvas.drawRect(
                it.second * cell,
                it.first * cell,
                (it.second + 1) * cell,
                (it.first + 1) * cell,
                selectPaint
            )
        }

        // ---- Legal Move Dots ----
        selected?.let { (fr, fc) ->
            for (r in 0..7) {
                for (c in 0..7) {
                    if (ChessEngine.isValidMove(fr, fc, r, c)) {
                        canvas.drawCircle(
                            c * cell + cell / 2,
                            r * cell + cell / 2,
                            cell / 6,
                            dotPaint
                        )
                    }
                }
            }
        }

        // ---- Draw Pieces ----
        for (r in 0..7) {
            for (c in 0..7) {
                val p = ChessEngine.board[r][c] ?: continue
                val inset = cell * 0.05f

                canvas.drawBitmap(
                    getBitmap(p.drawable),
                    null,
                    RectF(
                        c * cell + inset,
                        r * cell + inset,
                        (c + 1) * cell - inset,
                        (r + 1) * cell - inset
                    ),
                    null
                )
            }
        }

        // ---- Border ----
        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            borderPaint
        )
        canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),borderPaint)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {

        if (e.action != MotionEvent.ACTION_DOWN) return true

        val c = floor(e.x / cell).toInt()
        val r = floor(e.y / cell).toInt()

        if (r !in 0..7 || c !in 0..7) return false

        if (selected == null) {
            val p = ChessEngine.board[r][c]
            if (p != null && ChessEngine.isCurrentPlayer(p))
                selected = r to c
        } else {

            val (fr, fc) = selected!!

            if (ChessEngine.isValidMove(fr, fc, r, c)) {

                val target = ChessEngine.board[r][c]

                target?.let {
                    if (it.color == PieceColor.WHITE)
                        capturedWhite.add(it.drawable)
                    else
                        capturedBlack.add(it.drawable)
                }

                ChessEngine.move(fr, fc, r, c)

                lastMove = Move(fr, fc, r, c, null, null)

                val moved = ChessEngine.board[r][c]

                if (moved?.type == PieceType.PAWN && (r == 0 || r == 7))
                    onPawnPromotion?.invoke(r, c)

                onMovePlayed?.invoke()
            }

            selected = null
        }

        invalidate()
        return true
    }

    fun resetCapturedPieces() {
        capturedWhite.clear()
        capturedBlack.clear()
        invalidate()
    }

    private fun sortCaptured(list: MutableList<Int>): List<Int> {
        val order = listOf(
            R.drawable.black_pawn,
            R.drawable.black_knight,
            R.drawable.black_bishop,
            R.drawable.black_rook,
            R.drawable.black_queen,
            R.drawable.white_pawn,
            R.drawable.white_knight,
            R.drawable.white_bishop,
            R.drawable.white_rook,
            R.drawable.white_queen
        )
        return list.sortedBy { order.indexOf(it) }
    }

    private fun getBitmap(id: Int): Bitmap =
        cache[id] ?: run {

            val d = ContextCompat.getDrawable(context, id)!!

            val b = Bitmap.createBitmap(
                cell.toInt(),
                cell.toInt(),
                Bitmap.Config.ARGB_8888
            )

            val c = Canvas(b)

            d.setBounds(0, 0, c.width, c.height)
            d.draw(c)

            cache[id] = b
            b
        }
}