package com.example.medijourney.modules.health_center.nutrition_tracking_report


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.dual_image_v_text_view.DualImageVTextViewViewHolder
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
import com.example.medijourney.databinding.ItemDualImageVTextViewBinding
import com.example.medijourney.databinding.ItemHDualImageTextViewBinding

class NutritionTrackingReportAdapter: ListAdapter<DynamicUIItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    // Properties
    private val collapsedSections = mutableSetOf<Int>()

    // Life cycle
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is BaseViewHolderInterface) {
            holder.resetUI()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == Constants.HEADER) {
            val binding = ItemDualImageVTextViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DualImageVTextViewViewHolder(binding)
        } else {
            val binding = ItemHDualImageTextViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HDualImageTextViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == Constants.HEADER) {
            val headerHolder = holder as DualImageVTextViewViewHolder
            val itemModel = getItem(position)  ?: return

            headerHolder.inputData(itemModel)
            headerHolder.itemView.setOnClickListener {
                toggleSection(itemModel, position)
            }
        } else {
            val itemHolder = holder as HDualImageTextViewHolder
            val itemModel = getItem(position) ?: return

            itemHolder.inputData(itemModel)
            itemHolder.itemView.layoutParams.height =
                if (collapsedSections.contains(itemModel.groupIndex)) 0 else ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun toggleSection(itemModel: DynamicUIItem, position: Int) {
        val groupIndex = itemModel.groupIndex
        val isCollapsed = collapsedSections.contains(groupIndex)
        if (isCollapsed) {
            collapsedSections.remove(groupIndex)
        } else {
            collapsedSections.add(groupIndex)
        }
        val additionalData = itemModel.additionalData as? Map<String, Any>
        val items = additionalData?.get("itemsSize") as? Int ?: return

        notifyItemRangeChanged(position, items + 1)
    }

    // BaseAdapterInterface
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DynamicUIItem>() {
            override fun areItemsTheSame(oldItem: DynamicUIItem, newItem: DynamicUIItem): Boolean {
                return oldItem.itemTag == newItem.itemTag
            }

            override fun areContentsTheSame(oldItem: DynamicUIItem, newItem: DynamicUIItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}