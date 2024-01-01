package com.example.medijourney.common.ui_components.item_touch_helper.swipe_delete

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.R

class SwipeToDeleteCallback(private val context: Context, private val radius: Float = 32f) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    // Properties
    var output: SwipeToDeleteCallbackInterface? = null
    private var clearPaint: Paint? = null
    private var deleteIcon: Drawable? = null
    private var paint: Paint? = null
    private val iconWidth: Int
        get() {
            deleteIcon?.let {
                return it.intrinsicWidth
            } ?: return 0
        }
    private val iconHeight: Int
        get() {
            deleteIcon?.let {
                return it.intrinsicHeight
            } ?: return 0
        }

    // Functions
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val output = output ?: return
        output.onSwiped(viewHolder.bindingAdapterPosition, direction)
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            val left = itemView.right + dX
            val top = itemView.top.toFloat()
            val right = itemView.right.toFloat()
            val bottom = itemView.bottom.toFloat()
            clearCanvas(c, left, top, right, bottom)
            return
        }

        val left = if (itemView.right + dX <= recyclerView.paddingLeft) {
            recyclerView.paddingLeft.toFloat()
        } else {
            itemView.right + dX
        }
        val right = itemView.right.toFloat()
        val top = itemView.top.toFloat()
        val bottom = itemView.bottom.toFloat()

        if (paint == null) {
            paint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.primary_red_color)
                strokeWidth = 2f
                style = Paint.Style.FILL_AND_STROKE
            }
        }
        if (deleteIcon == null) {
            deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)?.apply {
                setTint(ContextCompat.getColor(context, R.color.white))
            }
        }
        paint?.let {
            c.drawRoundRect(left, top, right, bottom, radius, radius, it)
        }

        val deleteIconVerticalMargin = (itemHeight - iconHeight) / 2
        val deleteIconTop = itemView.top + deleteIconVerticalMargin
        val deleteIconLeft = itemView.right - 32 - iconWidth
        val deleteIconRight = itemView.right - 32
        val deleteIconBottom = deleteIconTop + iconHeight
        deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteIcon?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    @Suppress("NAME_SHADOWING")
    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        val c = c ?: return
        if (clearPaint == null) {
            clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        }
        clearPaint?.let {
            c.drawRect(left, top, right, bottom, it)
        }
    }
}