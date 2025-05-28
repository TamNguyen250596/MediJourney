package com.example.medijourney.common.ui_components.custom_views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.medijourney.common.models.ui_models.Segment

class MultiSegmentProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private var cornerRadius = 6f

    private var segments: List<Segment> = listOf()

    fun setup(newSegments: List<Segment>, radius: Float = 6f) {
        segments = newSegments
        cornerRadius = radius
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segments.isEmpty()) return

        val barHeight = height.toFloat()
        val rectF = RectF(0f, 0f, width.toFloat(), barHeight)

        path.reset()
        path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.clipPath(path)

        var startX = 0f

        for (segment in segments) {
            val segmentWidth = segment.percentage * width
            paint.color = context.resources.getColor(segment.color, null)
            canvas.drawRect(startX, 0f, startX + segmentWidth, barHeight, paint)
            startX += segmentWidth
        }
    }
}