package com.example.medijourney.common.ui_components.recycle_view_adapter.content_image

import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemImageViewBinding

class ContentImageViewHolder(
    binding: ItemImageViewBinding
) : RecyclerView.ViewHolder(binding.root), BaseViewHolderInterface {

    // Properties
    var output: BaseAdapterInterface? = null
    private var currentModel: ImageItemModel? = null
    private val boundsLayout = binding.boundsLayout
    private val imageView = binding.imageView
    private val textView = binding.textView

    // Init
    init {
        binding.root.setOnClickListener {
            output?.selectedItem(currentModel)
        }
    }

    // Functions
    override fun resetUI() {
        imageView.setImageDrawable(null)
        textView.text = null
        currentModel = null
    }

    fun inputData(model: ImageItemModel) {
        currentModel = model
        model.image.name?.let {
            ImageHelper.getResourceIdByName(it)?.let { imageId ->
                imageView.setImageResource(imageId)
            }
            model.image.size?.let { size ->
                imageView.setImageBitmap(imageView.drawable.toBitmap(size.first, size.second))
            }
        }
        model.title?.let {
            textView.apply {
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
            }
            textView.visibility = View.VISIBLE
        } ?: run {
            textView.visibility = View.GONE
        }
        model.padding?.let {
            boundsLayout.setPadding(it.left, it.top, it.right, it.bottom)
        }
    }


}