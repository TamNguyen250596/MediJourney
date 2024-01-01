package com.example.medijourney.modules.health_center.main.sub_ui.fitness_trackers.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemFitnessTrackerBinding

class FitnessTrackersAdapter(private var models: MutableList<DynamicUIItem>) :
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
            ItemFitnessTrackerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FitnessTrackerHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as FitnessTrackerHolder
        itemHolder.output = output

        val model = models[position]
        itemHolder.inputData(model)
    }

    override fun getItemCount(): Int {
        return models.count()
    }

    // Functions
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<DynamicUIItem>) {
        models.clear()
        models.addAll(newItems)
        notifyDataSetChanged()
    }
}