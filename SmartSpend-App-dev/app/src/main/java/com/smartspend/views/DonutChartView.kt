package com.smartspend.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // Data: category name → amount in Rands
    private val segments = listOf(
        Pair(Color.parseColor("#10B981"), 850f),   // Food — green
        Pair(Color.parseColor("#0066CC"), 420f),   // Transport — blue
        Pair(Color.parseColor("#7C3AED"), 1200f),  // Housing — purple
        Pair(Color.parseColor("#0891B2"), 750f),   // Entertainment — teal
    )

    private val total = segments.sumOf { it.second.toDouble() }.toFloat()

    // Paint for the arc segments
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    // Paint for the centre label text
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E")
        textAlign = Paint.Align.CENTER
        textSize = 36f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val subLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A")
        textAlign = Paint.Align.CENTER
        textSize = 24f
    }

    // Oval bounding rect for the arc
    private val oval = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val strokeWidth = w * 0.18f  // Donut ring thickness

        arcPaint.strokeWidth = strokeWidth

        // Leave room for the stroke so arcs don't get clipped at edge
        val padding = strokeWidth / 2f + 4f
        oval.set(padding, padding, w - padding, h - padding)

        var startAngle = -90f  // Start from the top of the circle

        // Draw each coloured segment
        segments.forEach { (colour, value) ->
            val sweepAngle = (value / total) * 360f
            arcPaint.color = colour
            canvas.drawArc(oval, startAngle, sweepAngle - 2f, false, arcPaint)
            startAngle += sweepAngle
        }

        // Draw centre label — shows the largest category name and amount
        val cx = w / 2f
        val cy = h / 2f
        canvas.drawText("Food", cx, cy - 10f, subLabelPaint)
        canvas.drawText("R850", cx, cy + 30f, labelPaint)
    }
}