package com.example.medijourney.common.ui_components.recycle_view_adapter.check_box

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.SelectionItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemCheckBoxBinding

class CheckBoxAdapter(private var items: MutableList<SelectionItemModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Properties
    var output: CheckBoxAdapterInterface? = null

    // Functions
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is BaseViewHolderInterface) {
            holder.resetUI()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemCheckBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CheckBoxItemHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as CheckBoxItemHolder
        itemHolder.output = output
        itemHolder.inputData(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // Functions
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<SelectionItemModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}