package com.example.medijourney.common.ui_components.item_decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.R

class ItemBorderDecoration(
    context: Context,
    borderColor: Int = ContextCompat.getColor(context, R.color.deep_turquoise_blue_color)
): RecyclerView.ItemDecoration() {

    // MARK: - Properties
    private val paint = Paint().apply {
        color = borderColor
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private var cornerRadius = 32f

    // MARK: - Functions
    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(0, 16, 0, 16)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val left = parent.paddingStart
        val right = parent.width - parent.paddingEnd

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)

            c.drawRoundRect(
                left.toFloat(),
                child.top.toFloat(),
                right.toFloat(),
                child.bottom.toFloat(),
                this.cornerRadius,
                this.cornerRadius,
                paint
            )
        }
    }
}