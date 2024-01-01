package com.example.medijourney.common.ui_components.recycle_view_adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.SelectionItemModel
import com.example.medijourney.databinding.ItemSwitchButtonBinding

interface SwitchButtonItemHolderInterface : BaseAdapterInterface {
    fun didSwitch(isChecked: Boolean, model: BaseItemInterface)
}

@SuppressLint("UseSwitchCompatOrMaterialCode")
class SwitchButtonItemHolder(
    binding: ItemSwitchButtonBinding,
) : RecyclerView.ViewHolder(binding.root), BaseViewHolderInterface {

    // Properties
    var output: SwitchButtonItemHolderInterface? = null
    private val imageView: ImageView = binding.iconImageView
    private val textView: TextView = binding.titleTextView
    private val switchButton: Switch = binding.switchButton
    private val separatorView: View = binding.separatorView
    private var currentModel: SelectionItemModel? = null

    // Init
    init {
        switchButton.setOnCheckedChangeListener { view, isChecked ->
            val currentModel = currentModel ?: return@setOnCheckedChangeListener
            if (view.isPressed) {
                output?.didSwitch(isChecked, currentModel)
            }
        }
    }

    // Functions
    override fun resetUI() {
        imageView.visibility = View.GONE
        imageView.setImageDrawable(null)
        textView.text = null
        switchButton.isChecked = false
        separatorView.visibility = View.VISIBLE
        currentModel = null
    }

    fun inputData(model: SelectionItemModel) {
        currentModel = model
        textView.apply {
            text = model.title.text
            typeface = ResourcesCompat.getFont(context, model.title.font)
            textSize = model.title.size
            setTextColor(ResourcesCompat.getColor(resources, model.title.color, null))
        }
        switchButton.isChecked = model.isSelected
    }
}