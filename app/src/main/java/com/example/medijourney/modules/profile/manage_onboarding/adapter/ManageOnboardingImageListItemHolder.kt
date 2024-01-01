package com.example.medijourney.modules.profile.manage_onboarding.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.NestedRecycleItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.databinding.ItemHorizontalRecycleViewBinding

class ManageOnboardingImageListItemHolder(
    context: Context,
    binding: ItemHorizontalRecycleViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    // Properties
    var output: BaseAdapterInterface? = null
    var models: MutableList<BaseItemInterface> = mutableListOf()
        set(value) {
            field = value
            updateItems(value)
        }
    private val recyclerView = binding.horizontalRecyclerView

    // Init
    init {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    // Functions
    fun inputData(model: NestedRecycleItemModel) {
        models = model.items
        model.padding?.let {
            recyclerView.setPadding(it.left, it.top, it.right, it.bottom)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<BaseItemInterface>) {
        val adapter = recyclerView.adapter as? ManageOnboardingImageListAdapter
        if (adapter != null) {
            adapter.updateItems(newItems)
            adapter.notifyDataSetChanged()
        } else {
            recyclerView.adapter = ManageOnboardingImageListAdapter(newItems.toMutableList()).apply {
                output = this@ManageOnboardingImageListItemHolder.output
            }
        }
    }
}