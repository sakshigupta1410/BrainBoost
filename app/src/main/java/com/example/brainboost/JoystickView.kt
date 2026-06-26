package com.example.brainboost

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
    }

    private val knobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
    }

    private var centerX = 0f
    private var centerY = 0f
    private var baseRadius = 0f
    private var knobX = 0f
    private var knobY = 0f

    var listener: ((Float, Float) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = w / 2f
        centerY = h / 2f
        baseRadius = min(w, h) / 2.5f
        knobX = centerX
        knobY = centerY
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint)
        canvas.drawCircle(knobX, knobY, baseRadius / 2.5f, knobPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - centerX
                val dy = event.y - centerY
                val dist = min(baseRadius, sqrt(dx*dx + dy*dy))

                val angle = atan2(dy, dx)
                knobX = centerX + dist * cos(angle)
                knobY = centerY + dist * sin(angle)

                listener?.invoke(dx / baseRadius, dy / baseRadius)
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                knobX = centerX
                knobY = centerY
                listener?.invoke(0f, 0f)
                invalidate()
            }
        }
        return true
    }
}
