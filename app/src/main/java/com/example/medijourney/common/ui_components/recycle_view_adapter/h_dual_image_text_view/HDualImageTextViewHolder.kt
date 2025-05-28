package com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view

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
import com.example.medijourney.databinding.ItemHDualImageTextViewBinding

class HDualImageTextViewHolder(binding: ItemHDualImageTextViewBinding) :
    RecyclerView.ViewHolder(binding.root), BaseViewHolderInterface {

    // Properties
    private var containerLayout = binding.containerLayout
    private val leftImageView = binding.iconImageView
    private val titleTextView = binding.titleTextView
    private val rightTextView = binding.rightTextView
    private val rightImageView = binding.rightImageView
    private val separatorView = binding.separatorView
    private var currentItemModel: DynamicUIItem? = null
    var output: BaseAdapterInterface? = null
    companion object {
        const val IS_HIDE_SEPARATOR_KEY = "isHideSeparator"
    }

    // Init
    init {
        binding.root.setOnClickListener {
            val output = output ?: return@setOnClickListener
            val currentItemModel = currentItemModel ?: return@setOnClickListener
            output.selectedItem(currentItemModel)
        }
    }

    // Functions
    override fun resetUI() {
        leftImageView.visibility = View.GONE
        leftImageView.setImageDrawable(null)
        titleTextView.visibility = View.GONE
        titleTextView.text = null
        rightTextView.visibility = View.GONE
        rightTextView.text = null
        rightImageView.visibility = View.GONE
        rightImageView.setImageDrawable(null)
        separatorView.visibility = View.VISIBLE
    }

    @Suppress("UNCHECKED_CAST")
    fun inputData(itemModel: DynamicUIItem) {
        currentItemModel = itemModel

        itemModel.image?.let {
            leftImageView.apply {
                when {
                    ImageHelper.isValidResource(it.name) ->
                        showImageResource(leftImageView, it.name, it.color, it.size)
                    it.url != null -> showImageUri(leftImageView, it.url)
                    else -> hideImageView(leftImageView)
                }
                visibility = View.VISIBLE
            }
        } ?: run {
            leftImageView.visibility = View.GONE
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
            rightTextView.apply {
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
                visibility = View.VISIBLE
            }
        } ?: run {
            rightTextView.visibility = View.GONE
        }

        itemModel.secondaryImage?.let {
            rightImageView.apply {
                when {
                    ImageHelper.isValidResource(it.name) ->
                        showImageResource(rightImageView, it.name)

                    it.url != null -> showImageUri(rightImageView, it.url)
                    else -> hideImageView(rightImageView)
                }
                visibility = View.VISIBLE
            }
        } ?: run {
            rightImageView.visibility = View.GONE
        }

        itemModel.padding?.let {
            containerLayout.setPadding(it.left, it.top, it.right, it.bottom)
        }

        itemModel.additionalData?.let {
            (it as? Map<String, Any>)?.let { additionalData ->
                val isHideSeparator = additionalData[IS_HIDE_SEPARATOR_KEY] as? Boolean ?: false
                separatorView.visibility = if (isHideSeparator) View.INVISIBLE else View.VISIBLE
            }
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