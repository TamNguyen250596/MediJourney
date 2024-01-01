package com.example.medijourney.common.ui_components.recycle_view_adapter.text_view

import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.databinding.ItemTitleBinding

class TextViewItemHolder(
    binding: ItemTitleBinding
): RecyclerView.ViewHolder(binding.root), BaseViewHolderInterface {

    // Properties
    var output: BaseAdapterInterface? = null
    private val boundsLayout = binding.boundsLayout
    private val titleTextView = binding.titleTextView
    private var currentModel: BaseItemInterface? = null

    init {
        binding.root.setOnClickListener {
            output?.selectedItem(currentModel)
        }
    }

    // Functions
    override fun resetUI() {
        titleTextView.text = null
        currentModel = null
    }

    fun inputData(model: TitleItemModel) {
        currentModel = model
        titleTextView.apply {
            text = model.title.text
            typeface = ResourcesCompat.getFont(context, model.title.font)
            textSize = model.title.size
            setTextColor(ResourcesCompat.getColor(resources, model.title.color, null))

            model.padding?.let {
                boundsLayout.setPadding(it.left, it.top, it.right, it.bottom)
            }
        }
    }
}