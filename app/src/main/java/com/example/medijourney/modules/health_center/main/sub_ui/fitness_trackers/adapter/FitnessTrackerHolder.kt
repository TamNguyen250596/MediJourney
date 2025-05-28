package com.example.medijourney.modules.health_center.main.sub_ui.fitness_trackers.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemFitnessTrackerBinding

class FitnessTrackerHolder(
    binding: ItemFitnessTrackerBinding
) : RecyclerView.ViewHolder(binding.root), BaseViewHolderInterface {

    // Properties
    private val boundsLayout = binding.boundsLayout
    private val imageView = binding.imageView
    private val titleTextView = binding.titleTextView
    private val descriptionTextView = binding.descriptionTextView
    private val versionTextView = binding.versionTextView
    private var currentModel: DynamicUIItem? = null
    var output: BaseAdapterInterface? = null

    // Life cycle
    init {
        binding.root.setOnClickListener {
            val output = output ?: return@setOnClickListener
            output.selectedItem(currentModel)
        }
    }

    override fun resetUI() {
        imageView.setImageDrawable(null)
        titleTextView.text = null
        descriptionTextView.text = null
        versionTextView.text = null
        currentModel = null
    }

    // Functions
    @Suppress("UNCHECKED_CAST")
    fun inputData(model: DynamicUIItem) {
        currentModel = model

        model.image?.let {
            when (true) {
                (it.url != null) -> {
                    showImageUri(imageView, it.url)
                }
                (ImageHelper.isValidResource(it.name)) -> {
                    showImageResource(imageView, it.name)
                }
                else -> {}
            }
        }

        model.title?.let {
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
        model.description?.let {
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
        model.secondaryDescription?.let {
            versionTextView.apply {
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
                visibility = View.VISIBLE
            }
        } ?: run {
            versionTextView.visibility = View.GONE
        }

        val additionalData = model.additionalData as? Map<String, Any>
        additionalData?.let {
            val width = it["width"] as? Int

            boundsLayout.apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    width ?: ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = model.padding?.right ?: 0
                }
            }
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
            FirebaseStorageManager.downloadImage(it) { uri ->
                Glide.with(imageView.context)
                    .load(uri)
                    .into(imageView)
                    .view.apply {
                        background = null
                        visibility = View.VISIBLE
                    }
            }
        }
    }
}