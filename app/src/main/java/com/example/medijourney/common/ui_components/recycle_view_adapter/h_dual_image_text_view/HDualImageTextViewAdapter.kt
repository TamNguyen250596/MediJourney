package com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemHDualImageTextViewBinding

class HDualImageTextViewAdapter(private var itemModels: MutableList<DynamicUIItem>) :
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemHDualImageTextViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HDualImageTextViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as HDualImageTextViewHolder
        val itemModel = this.itemModels[position]

        itemHolder.output = output
        itemHolder.inputData(itemModel)
    }

    override fun getItemCount(): Int {
        return itemModels.size
    }

    // Functions
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<DynamicUIItem>) {
        itemModels.clear()
        itemModels.addAll(newItems)
        notifyDataSetChanged()
    }
}