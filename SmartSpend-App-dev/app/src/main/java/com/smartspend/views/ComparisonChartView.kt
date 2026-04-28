package com.smartspend.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * ComparisonChartView — Member 1 (Christopher Morris)
 *
 * Custom View that draws the "Monthly Comparison" side-by-side bar chart
 * showing Income (green) vs Expenses (blue) for March and April —
 * as shown on page 33 of the POE design document.
 *
 * Uses Android Canvas — no third-party library needed.
 */
class ComparisonChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // Data: month → (income, expenses)
    private val months = listOf(
        Triple("March", 5200f, 3100f),
        Triple("April", 5200f, 3070f)
    )

    private val maxValue = 6000f

    // Green bar for income
    private val incomePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        style = Paint.Style.FILL
    }

    // Blue bar for expenses
    private val expensesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0066CC")
        style = Paint.Style.FILL
    }

    // Grid paint
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
    }

    // Y-axis label paint
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A")
        textSize = 26f
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        val leftMargin   = 16f
        val rightMargin  = 16f
        val topMargin    = 10f
        val bottomMargin = 40f

        val chartW = w - leftMargin - rightMargin
        val chartH = h - topMargin - bottomMargin

        // Horizontal grid lines
        listOf(0f, 1500f, 3000f, 4500f, 6000f).forEach { value ->
            val y = topMargin + chartH - (value / maxValue) * chartH
            canvas.drawLine(leftMargin, y, leftMargin + chartW, y, gridPaint)
        }

        // Bar width and spacing
        val groupWidth = chartW / months.size
        val barWidth   = groupWidth * 0.28f
        val barGap     = groupWidth * 0.06f

        months.forEachIndexed { index, (month, income, expenses) ->
            val groupX = leftMargin + index * groupWidth + groupWidth / 2f

            // Income bar (left of pair)
            val incomeX  = groupX - barGap / 2f - barWidth
            val incomeH  = (income / maxValue) * chartH
            val incomeTop = topMargin + chartH - incomeH
            canvas.drawRoundRect(
                incomeX, incomeTop,
                incomeX + barWidth, topMargin + chartH,
                6f, 6f, incomePaint
            )

            // Expenses bar (right of pair)
            val expensesX = groupX + barGap / 2f
            val expensesH = (expenses / maxValue) * chartH
            val expensesTop = topMargin + chartH - expensesH
            canvas.drawRoundRect(
                expensesX, expensesTop,
                expensesX + barWidth, topMargin + chartH,
                6f, 6f, expensesPaint
            )

            // Month label below
            canvas.drawText(month, groupX, h - bottomMargin + 30f, labelPaint)
        }
    }
}