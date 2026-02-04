package com.roklab.btcwatch.complication

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.LongTextComplicationData
import com.roklab.btcwatch.theme.ColorThemes

/**
 * Manages complication rendering and lifecycle.
 * Supports multiple complication types with proper bounds and styling.
 */
class ComplicationManager {

    companion object {
        // Complication slot IDs
        const val LEFT_COMPLICATION_ID = 0
        const val RIGHT_COMPLICATION_ID = 1
    }

    /**
     * Complication data holder
     */
    data class ComplicationState(
        val id: Int,
        val data: ComplicationData?,
        val bounds: RectF,
        val isAmbient: Boolean = false
    )

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    /**
     * Render a complication at the given position and bounds
     */
    fun renderComplication(
        canvas: Canvas,
        state: ComplicationState,
        colorScheme: ColorThemes.ColorScheme,
        textSize: Float = 16f
    ) {
        if (state.data == null) return

        when (state.data) {
            is ShortTextComplicationData -> {
                renderShortTextComplication(canvas, state, colorScheme, textSize)
            }
            is RangedValueComplicationData -> {
                renderRangedValueComplication(canvas, state, colorScheme, textSize)
            }
            is LongTextComplicationData -> {
                renderLongTextComplication(canvas, state, colorScheme, textSize)
            }
            else -> {
                // Handle other complication types if needed
            }
        }
    }

    /**
     * Render short text complication (e.g., date, battery %)
     */
    private fun renderShortTextComplication(
        canvas: Canvas,
        state: ComplicationState,
        colorScheme: ColorThemes.ColorScheme,
        textSize: Float
    ) {
        val data = state.data as ShortTextComplicationData
        val text = data.text.getTextAt(null, 0).toString()

        paint.apply {
            color = if (state.isAmbient) colorScheme.ambientTextColor else colorScheme.textColor
            textSize = textSize
            textAlign = Paint.Align.CENTER
        }

        val x = state.bounds.centerX()
        val y = state.bounds.centerY() + (paint.descent() - paint.ascent()) / 2

        canvas.drawText(text, x, y, paint)

        // Draw optional title/label
        data.title?.let { title ->
            paint.textSize = textSize * 0.7f
            val titleText = title.getTextAt(null, 0).toString()
            canvas.drawText(titleText, x, y - textSize, paint)
        }
    }

    /**
     * Render ranged value complication (e.g., battery level, steps progress)
     */
    private fun renderRangedValueComplication(
        canvas: Canvas,
        state: ComplicationState,
        colorScheme: ColorThemes.ColorScheme,
        textSize: Float
    ) {
        val data = state.data as RangedValueComplicationData

        val bounds = state.bounds
        val radius = minOf(bounds.width(), bounds.height()) / 2.5f
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()

        // Draw background circle
        paint.apply {
            color = if (state.isAmbient) colorScheme.ambientBackgroundColor else colorScheme.backgroundColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Draw progress arc
        val progress = (data.value / data.max).coerceIn(0f, 1f)
        val arcBounds = RectF(
            centerX - radius * 0.9f,
            centerY - radius * 0.9f,
            centerX + radius * 0.9f,
            centerY + radius * 0.9f
        )

        paint.apply {
            color = if (state.isAmbient) colorScheme.ambientTextColor else colorScheme.accentColor
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.2f
        }

        canvas.drawArc(arcBounds, -90f, progress * 360f, false, paint)

        // Draw text value
        paint.apply {
            color = if (state.isAmbient) colorScheme.ambientTextColor else colorScheme.textColor
            textSize = textSize
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
        }

        val valueText = data.value.toInt().toString()
        val y = centerY + (paint.descent() - paint.ascent()) / 2

        canvas.drawText(valueText, centerX, y, paint)
    }

    /**
     * Render long text complication
     */
    private fun renderLongTextComplication(
        canvas: Canvas,
        state: ComplicationState,
        colorScheme: ColorThemes.ColorScheme,
        textSize: Float
    ) {
        val data = state.data as LongTextComplicationData
        val text = data.text.getTextAt(null, 0).toString()

        paint.apply {
            color = if (state.isAmbient) colorScheme.ambientTextColor else colorScheme.textColor
            textSize = textSize * 0.8f
            textAlign = Paint.Align.CENTER
        }

        val x = state.bounds.centerX()
        val y = state.bounds.centerY() + (paint.descent() - paint.ascent()) / 2

        // Simple ellipsis if text is too long
        var displayText = text
        while (paint.measureText(displayText) > state.bounds.width() * 0.9f && displayText.length > 1) {
            displayText = displayText.dropLast(1) + "…"
        }

        canvas.drawText(displayText, x, y, paint)
    }

    /**
     * Calculate if a point is within complication bounds (for tap detection)
     */
    fun isComplicationTapped(tapX: Float, tapY: Float, bounds: RectF): Boolean {
        return bounds.contains(tapX, tapY)
    }

    /**
     * Get complication bounds for left position (9 o'clock)
     */
    fun getLeftComplicationBounds(centerX: Float, centerY: Float, radius: Float): RectF {
        val complicationRadius = radius * 0.25f
        return RectF(
            centerX - radius * 0.6f - complicationRadius,
            centerY - complicationRadius,
            centerX - radius * 0.6f + complicationRadius,
            centerY + complicationRadius
        )
    }

    /**
     * Get complication bounds for right position (3 o'clock)
     */
    fun getRightComplicationBounds(centerX: Float, centerY: Float, radius: Float): RectF {
        val complicationRadius = radius * 0.25f
        return RectF(
            centerX + radius * 0.6f - complicationRadius,
            centerY - complicationRadius,
            centerX + radius * 0.6f + complicationRadius,
            centerY + complicationRadius
        )
    }
}
