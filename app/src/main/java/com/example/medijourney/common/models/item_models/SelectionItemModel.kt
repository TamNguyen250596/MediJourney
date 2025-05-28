package com.example.medijourney.common.models.item_models

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.MTextStyle

data class SelectionItemModel(
    override var type: Int = Constants.ITEM,
    override var groupIndex: Int = 0,
    override var itemTag: String = "",
    override var data: Any? = null,
    override var padding: EdgePadding? = null,
    var title: MTextStyle,
    var description: MTextStyle? = null,
    var isSelected: Boolean,
    var isClickable: Boolean = true,
    var additionalData: Any? = null
): BaseItemInterface
