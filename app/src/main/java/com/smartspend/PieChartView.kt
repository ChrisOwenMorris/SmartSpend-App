package com.smartspend

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

// Simple data class to hold our slices
data class PieSlice(val name: String, val value: Double, val color: Int)

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var slices: List<PieSlice> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val oval = RectF()

    fun setData(newSlices: List<PieSlice>) {
        slices = newSlices
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = minOf(width, height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        val radius = size / 2f
        val totalValue = slices.sumOf { it.value }.toFloat()

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius)

        if (totalValue == 0f) {
            // Draw a gray circle if no data
            paint.color = "#F0F0F0".toColorInt()
            canvas.drawCircle(cx, cy, radius, paint)
            return
        }

        var startAngle = -90f // Start at the top

        for (slice in slices) {
            val sweepAngle = (slice.value.toFloat() / totalValue) * 360f
            paint.color = slice.color

            canvas.drawArc(oval, startAngle, sweepAngle, true, paint)
            startAngle += sweepAngle
        }

        // Draw inner circle for the "Donut" look
        canvas.drawCircle(cx, cy, radius * 0.6f, innerPaint)
    }
}