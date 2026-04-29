package com.smartspend

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var percentage: Float = 0f

    // Preallocated paint objects — avoids allocations during draw
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#F0F0F0".toColorInt()
        style = Paint.Style.FILL
    }

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val oval = RectF()

    fun setPercentage(percent: Float) {
        percentage = percent.coerceIn(0f, 100f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = minOf(width, height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        val radius = size / 2f

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // Draw background circle
        canvas.drawCircle(cx, cy, radius, backgroundPaint)

        // Draw gradient arc
        if (percentage > 0) {
            gradientPaint.shader = SweepGradient(
                cx, cy,
                intArrayOf(
                    "#6A11CB".toColorInt(),
                    "#2575FC".toColorInt(),
                    "#6A11CB".toColorInt()
                ),
                floatArrayOf(0f, 0.5f, 1f)
            )
            val sweepAngle = (percentage / 100f) * 360f
            canvas.drawArc(oval, -90f, sweepAngle, true, gradientPaint)

            // Draw inner white circle for donut effect
            val innerRadius = radius * 0.55f
            canvas.drawCircle(cx, cy, innerRadius, innerPaint)
        }
    }
}