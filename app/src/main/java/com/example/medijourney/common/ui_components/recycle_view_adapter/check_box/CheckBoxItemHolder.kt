package com.example.medijourney.common.ui_components.recycle_view_adapter.check_box

import android.view.Gravity
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.SelectionItemModel
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemCheckBoxBinding

class CheckBoxItemHolder(
    binding: ItemCheckBoxBinding
): RecyclerView.ViewHolder(binding.root), BaseViewHolderInterface {

    // Properties
    var output: CheckBoxAdapterInterface? = null
    private val boundsLayout = binding.boundsLayout
    private val textView = binding.titleTextView
    private val descriptionTextView = binding.descriptionTextView
    private val checkBox = binding.checkBox
    private var currentModel: BaseItemInterface? = null

    init {
        binding.root.setOnClickListener {
            output?.selectedItem(currentModel)
        }
        checkBox.setOnCheckedChangeListener { view, _ ->
            if (view.isPressed) {
                output?.selectedCheckBox(currentModel, checkBox.isChecked)
            }
        }
    }

    // Functions
    override fun resetUI() {
        textView.text = null
        checkBox.isChecked = false
        currentModel = null
        descriptionTextView.visibility = View.GONE
        descriptionTextView.text = null
    }

    @Suppress("UNCHECKED_CAST")
    fun inputData(model: SelectionItemModel) {
        currentModel = model
        textView.apply {
            text = model.title.text
            typeface = ResourcesCompat.getFont(context, model.title.font)
            textSize = model.title.size
            setTextColor(ResourcesCompat.getColor(resources, model.title.color, null))
        }
        model.description?.let {
            descriptionTextView.apply {
                visibility = View.VISIBLE
                text = it.text
                typeface = ResourcesCompat.getFont(context, it.font)
                textSize = it.size
                setTextColor(ResourcesCompat.getColor(resources, it.color, null))
            }
        } ?: run {
            descriptionTextView.visibility = View.GONE
        }
        checkBox.isChecked = model.isSelected
        checkBox.isClickable = model.isClickable
        model.padding?.let {
            boundsLayout.setPadding(it.left, it.top, it.right, it.bottom)
        }

        val additionalData = model.additionalData as? Map<String, Any>
        additionalData?.let {
            val gravity = it["gravity"] as? Int
            boundsLayout.gravity = gravity ?: Gravity.CENTER_VERTICAL
        }
    }
}