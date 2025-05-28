package com.example.medijourney.modules.profile.edit_profile.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medijourney.common.helpers.ImageHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseViewHolderInterface
import com.example.medijourney.databinding.ItemEditProfileBinding

class EditProfileViewHolder(binding: ItemEditProfileBinding) :
    RecyclerView.ViewHolder(binding.root),
    BaseViewHolderInterface {

    // Properties
    private val textInputLayout = binding.textInputLayout
    private val textInputEditText = binding.textInputEditText
    private val contentButton = binding.contentButton
    private var currentItemModel: DynamicUIItem? = null
    var output: EditProfileAdapterInterface? = null

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (!textInputEditText.isFocused) return
            val output = output ?: return
            val currentItemModel = currentItemModel ?: return
            val text = textInputEditText.text.toString()
            val error = output.checkErrorAtField(currentItemModel.itemTag, text)

            if (error == null) {
                textInputLayout.isErrorEnabled = false
                output.updateValue(currentItemModel.itemTag, textInputEditText.text.toString())
            } else {
                textInputLayout.isErrorEnabled = true
                textInputLayout.error = error
            }
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    companion object {
        const val IS_EDITABLE = "isEditable"
    }

    // Life cycle
    init {
        contentButton.setOnClickListener {
            val output = output ?: return@setOnClickListener
            val currentItemModel = currentItemModel ?: return@setOnClickListener

            output.selectedItem(currentItemModel)
        }
    }

    override fun resetUI() {
        textInputLayout.error = null
        textInputLayout.startIconDrawable = null
        textInputLayout.hint = null
        textInputLayout.endIconDrawable = null
        textInputEditText.text = null
        textInputEditText.removeTextChangedListener(textWatcher)
    }

    // Functions
    @Suppress("UNCHECKED_CAST")
    fun inputData(itemModel: DynamicUIItem) {
        currentItemModel = itemModel

        itemModel.image?.name?.let {
            textInputLayout.startIconDrawable = ImageHelper.getDrawableByName(it)
        }

        val title = output?.getTempValue(itemModel.itemTag) ?: itemModel.title?.text
        title?.let {
            textInputEditText.setText(it)
        }

        itemModel.title?.let {
            textInputEditText.typeface = ResourcesCompat.getFont(textInputEditText.context, it.font)
            textInputEditText.textSize = it.size
            textInputEditText.setTextColor(
                ResourcesCompat.getColor(
                    textInputEditText.resources,
                    it.color,
                    null
                )
            )
            textInputLayout.error = output?.getErrorAtField(textInputEditText.text.toString())
        }

        itemModel.description?.let {
            textInputEditText.hint = it.text
            textInputEditText.setHintTextColor(
                ResourcesCompat.getColor(
                    textInputEditText.resources,
                    it.color,
                    null
                )
            )
        }

        itemModel.secondaryImage?.name?.let {
            textInputLayout.endIconDrawable = ImageHelper.getDrawableByName(it)
        }

        itemModel.additionalData?.let {
            val isEditable = it as? Map<String, Any> ?: return
            if (isEditable[IS_EDITABLE] as? Boolean == true) {
                textInputEditText.addTextChangedListener(textWatcher)
                contentButton.visibility = View.GONE
            } else {
                contentButton.visibility = View.VISIBLE
            }
        }
    }
}
