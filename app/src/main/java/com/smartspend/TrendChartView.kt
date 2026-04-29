package com.smartspend

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt
import com.smartspend.data.dao.TrendSummary

class TrendChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var data: List<TrendSummary> = emptyList()

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#2575FC".toColorInt()
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 4f
    }

    fun setData(newData: List<TrendSummary>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val padding = 60f
        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)

        val maxAmount = data.maxOf { it.total }.toFloat().coerceAtLeast(1f)
        val barWidth = (chartWidth / data.size) * 0.7f
        val spacing = (chartWidth / data.size) * 0.3f

        // Draw X-Axis
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)

        data.forEachIndexed { index, summary ->
            val barHeight = (summary.total.toFloat() / maxAmount) * chartHeight

            val left = padding + (index * (barWidth + spacing)) + (spacing / 2)
            val top = (height - padding) - barHeight
            val right = left + barWidth
            val bottom = height - padding

            // Draw Bar
            canvas.drawRect(left, top, right, bottom, barPaint)

            // Draw Month Label (e.g., "04")
            canvas.drawText(summary.month, left + (barWidth / 2), height - 20f, textPaint)
        }
    }
}