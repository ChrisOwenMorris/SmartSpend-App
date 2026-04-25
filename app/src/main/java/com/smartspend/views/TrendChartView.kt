package com.smartspend.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * TrendChartView — Member 1 (Christopher Morris)
 *
 * Custom View that draws the "Spending Trends" line + dot chart
 * as shown on page 33 of the POE design document (Jan, Feb, Mar, May).
 *
 * Uses Android Canvas — no third-party library needed.
 */
class TrendChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // Monthly data points: label → amount
    private val dataPoints = listOf(
        Pair("Jan", 2700f),
        Pair("Feb", 2900f),
        Pair("Mar", 3100f),
        Pair("May", 3600f)
    )

    private val maxValue = 3600f

    // Line paint — blue with dots
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0066CC")
        strokeWidth = 4f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    // Dot paint
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0066CC")
        style = Paint.Style.FILL
    }

    // Grid line paint
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
    }

    // Y-axis label paint
    private val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A")
        textSize = 28f
        textAlign = Paint.Align.RIGHT
    }

    // X-axis label paint
    private val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Chart margins
        val leftMargin   = 80f
        val rightMargin  = 20f
        val topMargin    = 20f
        val bottomMargin = 50f

        val chartW = w - leftMargin - rightMargin
        val chartH = h - topMargin - bottomMargin

        // Draw horizontal grid lines (4 lines: 0, 900, 1800, 2700, 3600)
        val ySteps = listOf(0, 900, 1800, 2700, 3600)
        ySteps.forEach { value ->
            val y = topMargin + chartH - (value.toFloat() / maxValue) * chartH
            canvas.drawLine(leftMargin, y, leftMargin + chartW, y, gridPaint)
            // Y-axis label
            canvas.drawText("${value / 1000}k", leftMargin - 8f, y + 9f, yLabelPaint)
        }

        // Calculate X positions for each data point
        val xStep = chartW / (dataPoints.size - 1).toFloat()
        val points = dataPoints.mapIndexed { index, (_, value) ->
            val x = leftMargin + index * xStep
            val y = topMargin + chartH - (value / maxValue) * chartH
            PointF(x, y)
        }

        // Draw connecting line between dots
        val path = Path()
        points.forEachIndexed { index, point ->
            if (index == 0) path.moveTo(point.x, point.y)
            else path.lineTo(point.x, point.y)
        }
        canvas.drawPath(path, linePaint)

        // Draw each dot + x-axis label
        points.forEachIndexed { index, point ->
            canvas.drawCircle(point.x, point.y, 8f, dotPaint)
            // White inner dot for "open circle" look
            canvas.drawCircle(point.x, point.y, 4f, Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
                isAntiAlias = true
            })
            // X-axis month label
            canvas.drawText(
                dataPoints[index].first,
                point.x,
                h - bottomMargin + 34f,
                xLabelPaint
            )
        }
    }
}