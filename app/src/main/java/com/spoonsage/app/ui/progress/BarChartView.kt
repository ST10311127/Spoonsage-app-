package com.spoonsage.app.ui.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.spoonsage.app.R

class BarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var values: List<Float> = emptyList()
    private var labels: List<String> = emptyList()
    private var target: Float = 0f

    private val density = resources.displayMetrics.density
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.sage_green) }
    private val overPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.carrot) }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.sage_light) }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_secondary)
        textSize = 11 * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
    }
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.tomato)
        strokeWidth = 1.5f * density
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(8f * density, 6f * density), 0f)
    }

    fun setData(values: List<Float>, labels: List<String>, target: Float) {
        this.values = values
        this.labels = labels
        this.target = target
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return
        val labelSpace = 20 * density
        val chartHeight = height - labelSpace
        val maxValue = maxOf(values.maxOrNull() ?: 0f, target, 1f) * 1.1f
        val slot = width.toFloat() / values.size
        val barWidth = slot * 0.55f
        val radius = 6 * density

        values.forEachIndexed { i, v ->
            val left = i * slot + (slot - barWidth) / 2
            val barTop = chartHeight - (v / maxValue) * chartHeight
            val paint = when {
                v <= 0f -> emptyPaint
                target > 0 && v > target -> overPaint
                else -> barPaint
            }
            val top = if (v <= 0f) chartHeight - 4 * density else barTop
            canvas.drawRoundRect(RectF(left, top, left + barWidth, chartHeight), radius, radius, paint)
            labels.getOrNull(i)?.let { canvas.drawText(it, left + barWidth / 2, height - 4 * density, textPaint) }
        }
        if (target > 0) {
            val y = chartHeight - (target / maxValue) * chartHeight
            canvas.drawLine(0f, y, width.toFloat(), y, targetPaint)
        }
    }
}
