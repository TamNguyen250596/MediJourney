package com.example.medijourney.modules.profile.edit_profile.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemEditProfileBinding

class EditProfileAdapter(private var itemModels: MutableList<DynamicUIItem>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Properties
    var output: EditProfileAdapterInterface? = null

    // Life cycle
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is BaseViewHolderInterface) {
            holder.resetUI()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditProfileViewHolder {
        val binding = ItemEditProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EditProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holderView = holder as EditProfileViewHolder
        val itemModel = itemModels[position]

        holderView.output = output
        holderView.inputData(itemModel)
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

    fun updateItem(newItem: DynamicUIItem, position: Int) {
        itemModels[position] = newItem
        notifyItemChanged(position)
    }
}