package com.example.medijourney.modules.profile.manage_onboarding.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.medijourney.R
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemImageManageOnboardingBinding

class ManageOnboardingImageItemHolder(
    binding: ItemImageManageOnboardingBinding
) : RecyclerView.ViewHolder(binding.root), BaseViewHolderInterface {

    // Properties
    var output: BaseAdapterInterface? = null
    private val boundsLayout: View = binding.boundsLayout
    private val imageView: ImageView = binding.imageView
    private val textView: TextView = binding.textView
    private var currentModel: ImageItemModel? = null

    // Init
    init {
        binding.root.setOnClickListener {
            currentModel?.let {
                output?.selectedItem(it)
            }
        }
    }

    override fun resetUI() {
        setDefaultBackground(imageView)
        imageView.setImageDrawable(null)
        textView.text = null
        currentModel = null
    }

    // Functions
    fun inputData(model: ImageItemModel) {
        currentModel = model
        if (ImageHelper.isValidResource(model.image.name)) {
            showImageResource(imageView, model.image.name)
        } else {
            showImageUri(imageView, model.image.url)
        }
        model.title?.let {
            textView.apply {
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
            }
        }
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
            FirebaseStorageManager.downloadImage(it) { uri ->
                Glide.with(imageView)
                    .load(uri)
                    .into(imageView)
                    .view.apply {
                        background = null
                        visibility = View.VISIBLE
                    }
            }
        }
    }

    private fun setDefaultBackground(imageView: ImageView) {
        imageView.apply {
            setBackgroundColor(ResourcesCompat.getColor(resources, R.color.corn_flower_blue_color, null))
        }
    }
}