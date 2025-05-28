package com.example.medijourney.modules.profile.manage_onboarding.adapter

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.NestedRecycleItemModel
import com.example.medijourney.common.models.item_models.SelectionItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.SwitchButtonItemHolder
import com.example.medijourney.common.ui_components.recycle_view_adapter.SwitchButtonItemHolderInterface
import com.example.medijourney.databinding.ItemHorizontalRecycleViewBinding
import com.example.medijourney.databinding.ItemSwitchButtonBinding

class ManageOnboardingAdapter(private var models: MutableList<BaseItemInterface>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Properties
    var output: SwitchButtonItemHolderInterface? = null

    // Life cycle
    override fun getItemViewType(position: Int): Int {
        return models[position].type
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getGroupIndex(position: Int): Int {
        return models[position].groupIndex
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is BaseViewHolderInterface) {
            holder.resetUI()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Constants.HEADER -> {
                val binding = ItemSwitchButtonBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false)
                SwitchButtonItemHolder(binding)
            }

            Constants.NESTED_RECYCLE_VIEW -> {
                val binding = ItemHorizontalRecycleViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false)
                ManageOnboardingImageListItemHolder(parent.context, binding)
            }

            else -> {
                val footerView = View(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            16f,
                            parent.resources.displayMetrics
                        ).toInt()
                    )
                }
                object : RecyclerView.ViewHolder(footerView) {}
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when (viewType) {
            Constants.HEADER -> {
                val headerHolder = holder as SwitchButtonItemHolder
                headerHolder.output = output

                val model = models[position] as? SelectionItemModel
                model?.let {
                    headerHolder.inputData(it)
                }
            }

            Constants.NESTED_RECYCLE_VIEW -> {
                val nestedRecycleHolder = holder as ManageOnboardingImageListItemHolder
                nestedRecycleHolder.output = output

                val model = models[position] as? NestedRecycleItemModel
                model?.let {
                    nestedRecycleHolder.inputData(it)
                }
            }

            else -> {}
        }
    }

    override fun getItemCount(): Int {
        return this.models.size
    }

    // Functions
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<BaseItemInterface>) {
        models.clear()
        models.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, item: BaseItemInterface) {
        models[position] = item
        notifyItemChanged(position)
    }
}

