package com.example.medijourney.common.ui_components.recycle_view_adapter.text_view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.databinding.ItemTitleBinding

class TextViewListAdapter(private var items: MutableList<TitleItemModel>):
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
        val binding = ItemTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TextViewItemHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as TextViewItemHolder
        itemHolder.output = output
        itemHolder.inputData(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // Functions
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<TitleItemModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}