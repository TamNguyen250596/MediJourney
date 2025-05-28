package com.example.medijourney.common.ui_components.recycle_view_adapter.check_box

import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.ui_components.recycle_view_adapter.BaseAdapterInterface

interface CheckBoxAdapterInterface: BaseAdapterInterface {
    fun selectedCheckBox(model: BaseItemInterface?, isChecked: Boolean)
}