package com.example.medijourney.common.models.item_models

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.models.ui_models.EdgePadding

data class BaseItemModel(
    override var type: Int = Constants.FOOTER,
    override var groupIndex: Int = 0,
    override var itemTag: String = "",
    override var data: Any? = null,
    override var padding: EdgePadding? = null,
): BaseItemInterface
