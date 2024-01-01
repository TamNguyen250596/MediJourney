package com.example.medijourney.common.ui_components.item_decoration

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(private val topSpace: Int = 0,
                           private val bottomSpace: Int = 0,
                           private val leftSpace: Int = 0,
                           private val rightSpace: Int = 0,
                           private val leftPadding: Int = 0,
                           private val rightPadding: Int = 0)
    : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        var tempLeft = leftSpace
        var tempRight = rightSpace

        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount
        if (itemPosition == itemCount - 1) {
            tempRight = rightPadding
        }
        if (itemPosition == 0) {
            tempLeft = leftPadding
        }
        outRect.set(tempLeft, topSpace, tempRight, bottomSpace)
    }
}