package com.example.medijourney.common.ui_components.recycle_view_adapter

import com.example.medijourney.common.models.item_models.BaseItemInterface

interface BaseAdapterInterface {
    fun selectedItem(model: BaseItemInterface?)
}