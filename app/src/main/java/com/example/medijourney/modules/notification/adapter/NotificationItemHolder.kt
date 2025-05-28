package com.example.medijourney.modules.notification.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medijourney.R
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemNotificationBinding

class NotificationItemHolder(
    binding: ItemNotificationBinding
) : RecyclerView.ViewHolder(binding.root), BaseViewHolderInterface {

    // Properties
    private val boundsLayout = binding.boundsLayout
    private val imageView = binding.imageView
    private val titleTextView = binding.titleTextView
    private val descriptionTextView = binding.descriptionTextView
    private val timeTextView = binding.timeTextView
    private var currentModel: DynamicUIItem? = null
    var output: BaseAdapterInterface? = null

    // Init
    init {
        binding.root.setOnClickListener {
            currentModel?.let {
                output?.selectedItem(it)
            }
        }
    }

    // Functions
    @SuppressLint("ResourceAsColor")
    override fun resetUI() {
        boundsLayout.setBackgroundColor(R.color.light_blue_color)
        imageView.setImageDrawable(null)
        titleTextView.text = null
        descriptionTextView.text = null
        timeTextView.text = null
        currentModel = null
    }

    fun inputData(model: DynamicUIItem) {
        currentModel = model
        model.image?.let {
            if (ImageHelper.isValidResource(it.name)) {
                showImageResource(imageView, it.name)
            } else {
                showImageUri(imageView, it.url)
            }
        } ?: run {
            imageView.visibility = View.GONE
        }
        model.title?.let {
            titleTextView.apply {
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
            }
            titleTextView.visibility = View.VISIBLE
        } ?: run {
            titleTextView.visibility = View.GONE
        }
        model.description?.let {
            descriptionTextView.apply {
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
                }
            descriptionTextView.visibility = View.VISIBLE
        } ?: run {
            descriptionTextView.visibility = View.GONE
        }
        model.secondaryDescription?.let {
            timeTextView.apply {
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
            }
            timeTextView.visibility = View.VISIBLE
        } ?: run {
            timeTextView.visibility = View.GONE
        }
        boundsLayout.setBackgroundColor(ResourcesCompat.getColor(boundsLayout.resources, model.backgroundColor, null))
        model.padding?.let {
            boundsLayout.setPadding(it.left, it.top, it.right, it.bottom)
        }
    }

    private fun showImageResource(imageView: ImageView, name: String?) {
        name?.let {
            imageView.apply {
                ImageHelper.getResourceIdByName(name)?.let {
                    setImageResource(it)
                }
                background = null
                visibility = View.VISIBLE
            }
        }
    }

    private fun showImageUri(imageView: ImageView, url: String?) {
        url?.let {
            Glide.with(imageView).load(Uri.parse(it))
        }
    }
}