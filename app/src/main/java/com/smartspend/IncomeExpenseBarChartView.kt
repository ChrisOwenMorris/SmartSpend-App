package com.smartspend

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.toColorInt

class IncomeExpenseBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var income: Double = 0.0
    private var expense: Double = 0.0

    private val incomePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#4CAF50".toColorInt() // Green
        style = Paint.Style.FILL
    }

    private val expensePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#F44336".toColorInt() // Red
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    fun setData(incomeVal: Double, expenseVal: Double) {
        income = incomeVal
        expense = expenseVal
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 100f
        val chartHeight = height - (padding * 2)
        val chartWidth = width - (padding * 2)

        val maxVal = maxOf(income, expense).toFloat().coerceAtLeast(1f)
        val barWidth = chartWidth / 3f // Enough space for 2 bars + spacing

        // Draw Income Bar
        val incomeHeight = (income.toFloat() / maxVal) * chartHeight
        val incomeRect = RectF(
            padding,
            (height - padding) - incomeHeight,
            padding + barWidth,
            height - padding
        )
        canvas.drawRect(incomeRect, incomePaint)
        canvas.drawText("Income", incomeRect.centerX(), height - 40f, textPaint)

        // Draw Expense Bar
        val expenseHeight = (expense.toFloat() / maxVal) * chartHeight
        val expenseRect = RectF(
            width - padding - barWidth,
            (height - padding) - expenseHeight,
            width - padding,
            height - padding
        )
        canvas.drawRect(expenseRect, expensePaint)
        canvas.drawText("Expenses", expenseRect.centerX(), height - 40f, textPaint)
    }
}