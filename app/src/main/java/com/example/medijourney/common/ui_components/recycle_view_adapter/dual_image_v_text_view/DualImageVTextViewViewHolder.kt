package com.example.medijourney.common.ui_components.recycle_view_adapter.dual_image_v_text_view

import android.content.res.ColorStateList
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemDualImageVTextViewBinding

class DualImageVTextViewViewHolder(binding: ItemDualImageVTextViewBinding) : RecyclerView.ViewHolder(binding.root),
    BaseViewHolderInterface {

    // Properties
    private val containerView = binding.containerView
    private val iconImageView = binding.iconImageView
    private val titleTextView = binding.titleTextView
    private val descriptionTextView = binding.descriptionTextView
    private val rightImageView = binding.rightImageView
    private var currentItemModel: DynamicUIItem? = null
    var output: BaseAdapterInterface? = null

    // Life cycle
    init {
        binding.root.setOnClickListener {
            val output = output ?: return@setOnClickListener
            output.selectedItem(currentItemModel)
        }
    }

    override fun resetUI() {
        iconImageView.visibility = View.GONE
        iconImageView.setImageDrawable(null)
        titleTextView.visibility = View.GONE
        titleTextView.text = null
        descriptionTextView.visibility = View.GONE
        descriptionTextView.text = null
        rightImageView.visibility = View.GONE
        rightImageView.setImageDrawable(null)
    }

    // Functions
    fun inputData(itemModel: DynamicUIItem) {
        currentItemModel = itemModel

        itemModel.image?.let {
            iconImageView.apply {
                when {
                    ImageHelper.isValidResource(it.name) ->
                        showImageResource(iconImageView, it.name, it.color, it.size)

                    it.url != null -> showImageUri(iconImageView, it.url)
                    else -> hideImageView(iconImageView)
                }
                visibility = View.VISIBLE
            }
        } ?: run {
            iconImageView.visibility = View.GONE
        }

        itemModel.title?.let {
            titleTextView.apply {
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
                visibility = View.VISIBLE
            }
        } ?: run {
            titleTextView.visibility = View.GONE
        }

        itemModel.description?.let {
            descriptionTextView.apply {
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
                visibility = View.VISIBLE
            }
        } ?: run {
            descriptionTextView.visibility = View.GONE
        }

        itemModel.secondaryImage?.let {
            rightImageView.apply {
                when {
                    ImageHelper.isValidResource(it.name) -> showImageResource(
                        rightImageView,
                        it.name
                    )

                    it.url != null -> showImageUri(rightImageView, it.url)
                    else -> hideImageView(rightImageView)
                }
                visibility = View.VISIBLE
            }
        } ?: run {
            rightImageView.visibility = View.GONE
        }

        itemModel.padding?.let {
            containerView.setPadding(it.left, it.top, it.right, it.bottom)
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun showImageResource(imageView: ImageView,
                                  name: String?,
                                  color: Int? = null,
                                  size: Pair<Int, Int>? = null) {
        val name = name ?: return

        imageView.apply {
            ImageHelper.getResourceIdByName(name)?.let {
                setImageResource(it)
                color?.let {
                    imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, color, null))
                }
                size?.let {
                    layoutParams.width = size.first
                    layoutParams.height = size.second
                }
            }
        }
    }

    private fun showImageUri(imageView: ImageView, url: String?) {
        imageView.apply {
            setImageURI(Uri.parse(url))
        }
    }

    private fun hideImageView(imageView: ImageView) {
        imageView.visibility = View.GONE
    }
}
