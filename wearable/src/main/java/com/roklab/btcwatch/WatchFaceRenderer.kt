package com.roklab.btcwatch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.style.UserStyleRepository
import com.roklab.btcwatch.complication.ComplicationManager
import com.roklab.btcwatch.theme.ColorThemes
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance canvas renderer for BTC Watch Face.
 * Renders analog dial, hands, complications, and manages ambient mode.
 */
class WatchFaceRenderer(
    surfaceHolder: android.view.SurfaceHolder,
    private val context: Context,
    private val watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    private val userStyleRepository: UserStyleRepository
) : Renderer.CanvasRenderer(
    surfaceHolder = surfaceHolder,
    interactiveDrawMode = DrawMode.Immediate,
    canvasType = CanvasType.HARDWARE,
    interactiveFrameRateMs = 16, // 60 FPS
    ambientFrameRateMs = 1000    // ~1 FPS ambient
) {

    private val complicationManager = ComplicationManager()

    // Paint objects for performance
    private val dialPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val hourHandPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeLineCap = Paint.Cap.ROUND
    }

    private val minuteHandPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeLineCap = Paint.Cap.ROUND
    }

    private val secondHandPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeLineCap = Paint.Cap.ROUND
    }

    private val tickPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeLineCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }

    private var currentTheme = ColorThemes.getDefaultTheme()
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var dialRadius = 0f

    override fun onSurfaceChanged(holder: android.view.SurfaceHolder, format: Int, width: Int, height: Int) {
        super.onSurfaceChanged(holder, format, width, height)
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        centerX = screenWidth / 2f
        centerY = screenHeight / 2f
        dialRadius = minOf(centerX, centerY) * 0.95f
    }

    override fun render(zonedDateTime: ZonedDateTime) {
        val canvas = surfaceHolder.lockCanvas() ?: return

        try {
            // Clear canvas
            canvas.drawColor(currentTheme.backgroundColor)

            if (watchState.isAmbient) {
                renderAmbientMode(canvas, zonedDateTime)
            } else {
                renderInteractiveMode(canvas, zonedDateTime)
            }

            // Render complications
            renderComplications(canvas)

        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun renderInteractiveMode(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        // Draw dial
        drawDial(canvas)

        // Draw hour markers
        drawHourMarkers(canvas)

        // Draw hands
        drawHands(canvas, zonedDateTime)

        // Draw center dot
        drawCenterDot(canvas)
    }

    private fun renderAmbientMode(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        // Ambient mode: simplified, outline only
        // Draw dial outline
        dialPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = currentTheme.ambientTextColor
        }
        canvas.drawCircle(centerX, centerY, dialRadius, dialPaint)

        // Draw hands (outline only)
        drawHandsAmbient(canvas, zonedDateTime)

        // Draw simplified hour markers
        drawSimpleHourMarkers(canvas)
    }

    private fun drawDial(canvas: Canvas) {
        dialPaint.apply {
            color = currentTheme.backgroundColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, dialRadius, dialPaint)

        // Draw dial ring
        dialPaint.apply {
            color = currentTheme.primaryColor
            style = Paint.Style.STROKE
            strokeWidth = dialRadius * 0.05f
        }
        canvas.drawCircle(centerX, centerY, dialRadius, dialPaint)

        // Draw Bitcoin logo placeholder (simplified)
        drawBitcoinLogo(canvas)
    }

    private fun drawBitcoinLogo(canvas: Canvas) {
        // Simple Bitcoin circle logo at top
        val logoRadius = dialRadius * 0.08f
        dialPaint.apply {
            color = currentTheme.primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY - dialRadius * 0.7f, logoRadius, dialPaint)

        // Draw "₿" symbol (simplified as text)
        textPaint.apply {
            color = currentTheme.backgroundColor
            textSize = logoRadius * 1.5f
        }
        canvas.drawText("₿", centerX, centerY - dialRadius * 0.65f, textPaint)
    }

    private fun drawHourMarkers(canvas: Canvas) {
        tickPaint.apply {
            color = currentTheme.primaryColor
            strokeWidth = dialRadius * 0.01f
        }

        for (i in 0 until 12) {
            val angle = i * 30f * (Math.PI / 180f)
            val startRadius = dialRadius * 0.85f
            val endRadius = dialRadius * 0.95f

            val startX = centerX + (startRadius * sin(angle)).toFloat()
            val startY = centerY - (startRadius * cos(angle)).toFloat()
            val endX = centerX + (endRadius * sin(angle)).toFloat()
            val endY = centerY - (endRadius * cos(angle)).toFloat()

            canvas.drawLine(startX, startY, endX, endY, tickPaint)
        }
    }

    private fun drawSimpleHourMarkers(canvas: Canvas) {
        tickPaint.apply {
            color = currentTheme.ambientTextColor
            strokeWidth = dialRadius * 0.005f
        }

        for (i in listOf(12, 3, 6, 9)) {
            val angle = (i * 30f) * (Math.PI / 180f)
            val startRadius = dialRadius * 0.8f
            val endRadius = dialRadius * 0.92f

            val startX = centerX + (startRadius * sin(angle)).toFloat()
            val startY = centerY - (startRadius * cos(angle)).toFloat()
            val endX = centerX + (endRadius * sin(angle)).toFloat()
            val endY = centerY - (endRadius * cos(angle)).toFloat()

            canvas.drawLine(startX, startY, endX, endY, tickPaint)
        }
    }

    private fun drawHands(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        val hour = zonedDateTime.hour % 12
        val minute = zonedDateTime.minute
        val second = zonedDateTime.second

        // Hour hand
        hourHandPaint.color = currentTheme.handsColor
        hourHandPaint.strokeWidth = dialRadius * 0.06f
        val hourAngle = (hour * 30f + minute * 0.5f) * (Math.PI / 180f)
        drawHand(canvas, hourAngle, dialRadius * 0.5f, hourHandPaint)

        // Minute hand
        minuteHandPaint.color = currentTheme.handsColor
        minuteHandPaint.strokeWidth = dialRadius * 0.04f
        val minuteAngle = (minute * 6f + second * 0.1f) * (Math.PI / 180f)
        drawHand(canvas, minuteAngle, dialRadius * 0.7f, minuteHandPaint)

        // Second hand (thin, accent color, optional)
        secondHandPaint.color = currentTheme.accentColor
        secondHandPaint.strokeWidth = dialRadius * 0.015f
        val secondAngle = (second * 6f) * (Math.PI / 180f)
        drawHand(canvas, secondAngle, dialRadius * 0.75f, secondHandPaint)
    }

    private fun drawHandsAmbient(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        val hour = zonedDateTime.hour % 12
        val minute = zonedDateTime.minute

        // Hour hand
        hourHandPaint.apply {
            color = currentTheme.ambientTextColor
            style = Paint.Style.STROKE
            strokeWidth = dialRadius * 0.03f
        }
        val hourAngle = (hour * 30f + minute * 0.5f) * (Math.PI / 180f)
        drawHand(canvas, hourAngle, dialRadius * 0.5f, hourHandPaint)

        // Minute hand
        minuteHandPaint.apply {
            color = currentTheme.ambientTextColor
            style = Paint.Style.STROKE
            strokeWidth = dialRadius * 0.02f
        }
        val minuteAngle = (minute * 6f) * (Math.PI / 180f)
        drawHand(canvas, minuteAngle, dialRadius * 0.7f, minuteHandPaint)
    }

    private fun drawHand(canvas: Canvas, angle: Double, length: Float, paint: Paint) {
        val endX = centerX + (length * sin(angle)).toFloat()
        val endY = centerY - (length * cos(angle)).toFloat()
        canvas.drawLine(centerX, centerY, endX, endY, paint)
    }

    private fun drawCenterDot(canvas: Canvas) {
        dialPaint.apply {
            color = currentTheme.handsColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, dialRadius * 0.04f, dialPaint)
    }

    private fun renderComplications(canvas: Canvas) {
        val leftBounds = complicationManager.getLeftComplicationBounds(centerX, centerY, dialRadius)
        val rightBounds = complicationManager.getRightComplicationBounds(centerX, centerY, dialRadius)

        // Render left complication (slot 0)
        val leftData = complicationSlotsManager.getComplicationOption(
            ComplicationManager.LEFT_COMPLICATION_ID
        )?.data?.complicationData

        if (leftData != null) {
            val state = ComplicationManager.ComplicationState(
                id = ComplicationManager.LEFT_COMPLICATION_ID,
                data = leftData,
                bounds = leftBounds,
                isAmbient = watchState.isAmbient
            )
            complicationManager.renderComplication(canvas, state, currentTheme, dialRadius * 0.08f)
        }

        // Render right complication (slot 1)
        val rightData = complicationSlotsManager.getComplicationOption(
            ComplicationManager.RIGHT_COMPLICATION_ID
        )?.data?.complicationData

        if (rightData != null) {
            val state = ComplicationManager.ComplicationState(
                id = ComplicationManager.RIGHT_COMPLICATION_ID,
                data = rightData,
                bounds = rightBounds,
                isAmbient = watchState.isAmbient
            )
            complicationManager.renderComplication(canvas, state, currentTheme, dialRadius * 0.08f)
        }
    }

    /**
     * Update theme when user style changes
     */
    fun updateTheme(theme: ColorThemes.Theme) {
        currentTheme = ColorThemes.getTheme(theme)
        invalidate()
    }
}
