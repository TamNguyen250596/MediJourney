package com.example.medijourney.common.models.item_models

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.ui_models.EdgePadding

data class NestedRecycleItemModel(
    override var type: Int = Constants.NESTED_RECYCLE_VIEW,
    override var groupIndex: Int = 0,
    override var itemTag: String = "",
    override var data: Any? = null,
    override var padding: EdgePadding? = null,
    var items: MutableList<BaseItemInterface> = mutableListOf(),
): BaseItemInterface