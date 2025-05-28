package com.example.medijourney.common.ui_components.item_decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.ui_models.Rect
import com.example.medijourney.modules.profile.manage_onboarding.adapter.ManageOnboardingAdapter
import kotlin.math.max
import kotlin.math.min

class GroupedSectionBorderDecoration(
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
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val left = parent.paddingStart.toFloat()
        val right = parent.width - parent.paddingEnd.toFloat()
        val rectAtGroup: MutableMap<Int, Rect> = mutableMapOf()

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val adapter = parent.adapter as? ManageOnboardingAdapter ?: continue
            val groupIndex = adapter.getGroupIndex(position)
            val viewType = adapter.getItemViewType(position)
            if (viewType == Constants.FOOTER) continue

            rectAtGroup[groupIndex]?.let {
                it.top = min(it.top, child.top.toFloat())
                it.bottom = max(it.bottom, child.bottom.toFloat())
            } ?: run {
                rectAtGroup[groupIndex] = Rect(left, child.top.toFloat(),  right, child.bottom.toFloat())
            }
        }

        rectAtGroup.forEach { (_, rect) ->
            c.drawRoundRect(left, rect.top + 2f,
                right, rect.bottom - 2f,
                cornerRadius, cornerRadius, paint)
        }
    }
}