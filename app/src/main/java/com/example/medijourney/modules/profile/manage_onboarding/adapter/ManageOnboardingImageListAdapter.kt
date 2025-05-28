package com.example.medijourney.modules.profile.manage_onboarding.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemImageManageOnboardingBinding

class ManageOnboardingImageListAdapter(private var models: MutableList<BaseItemInterface>) :
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
        val binding = ItemImageManageOnboardingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return ManageOnboardingImageItemHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as ManageOnboardingImageItemHolder
        itemHolder.output = output

        val model = models[position] as? ImageItemModel ?: return
        itemHolder.inputData(model)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    // Functions
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<BaseItemInterface>) {
        models.clear()
        models.addAll(newItems)
        notifyDataSetChanged()
    }
}