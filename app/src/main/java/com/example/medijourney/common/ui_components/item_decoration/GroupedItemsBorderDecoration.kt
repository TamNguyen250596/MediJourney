package com.example.medijourney.common.ui_components.item_decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import java.util.UUID

class GroupedItemsBorderDecoration(
    context: Context,
    borderColor: Int = ContextCompat.getColor(context, R.color.deep_turquoise_blue_color)
    ) : RecyclerView.ItemDecoration() {

    // MARK: - Properties
    private val paint = Paint().apply {
        color = borderColor
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private var cornerRadius = 32f

    // MARK: - Functions
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val left = parent.paddingStart
        val right = parent.width - parent.paddingEnd
        var currentUUID = UUID.randomUUID().toString()
        val groupedItems: MutableMap<String, MutableList<View>> = mutableMapOf()

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val viewType = parent.adapter?.getItemViewType(position)

            if (viewType == Constants.ITEM) {
                groupedItems[currentUUID]?.add(child) ?: run {
                    groupedItems[currentUUID] = mutableListOf(child)
                }
            } else {
                currentUUID = UUID.randomUUID().toString()
            }
        }

        groupedItems.forEach { (_, views) ->
            val top = views.minOf { it.top }
            val bottom = views.maxOf { it.bottom }

            c.drawRoundRect(
                left.toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                this.cornerRadius,
                this.cornerRadius,
                paint)
        }
    }
}
