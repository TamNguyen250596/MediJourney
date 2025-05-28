package com.example.medijourney.common.ui_components.recycle_view_adapter.content_image

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemImageViewBinding

class ContentImageAdapter(private var models: MutableList<ImageItemModel>) :
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
        val binding =
            ItemImageViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContentImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val contentImageHolder = holder as ContentImageViewHolder
        contentImageHolder.output = output
        contentImageHolder.inputData(models[position])
    }

    override fun getItemCount(): Int {
        return models.size
    }

    // Functions
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<ImageItemModel>) {
        models.clear()
        models.addAll(newItems)
        notifyDataSetChanged()
    }
}