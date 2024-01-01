package com.example.medijourney.modules.profile.manage_profile

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
import com.example.medijourney.common.ui_components.recycle_view_adapter.text_view.TextViewItemHolder
import com.example.medijourney.databinding.ItemHDualImageTextViewBinding
import com.example.medijourney.databinding.ItemTitleBinding

class ManageProfileAdapter(private var itemModels: MutableList<BaseItemInterface>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Properties
    var output: BaseAdapterInterface? = null

    // Life cycle
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is BaseViewHolderInterface) {
            holder.resetUI()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return this.itemModels[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == Constants.HEADER) {
            val binding = ItemTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TextViewItemHolder(binding)
        } else {
            val binding = ItemHDualImageTextViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HDualImageTextViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == Constants.HEADER) {
            val headerHolder = holder as TextViewItemHolder
            val itemModel = this.itemModels[position] as? TitleItemModel ?: return

            headerHolder.output = output
            headerHolder.inputData(itemModel)
        } else {
            val itemHolder = holder as HDualImageTextViewHolder
            val itemModel = this.itemModels[position] as? DynamicUIItem ?: return

            itemHolder.output = output
            itemHolder.inputData(itemModel)
        }
    }

    override fun getItemCount(): Int {
        return itemModels.size
    }

    // Functions
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<BaseItemInterface>) {
        itemModels.clear()
        itemModels.addAll(newItems)
        notifyDataSetChanged()
    }
}