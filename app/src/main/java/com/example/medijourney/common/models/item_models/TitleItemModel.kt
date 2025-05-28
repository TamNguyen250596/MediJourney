package com.example.medijourney.common.models.item_models

import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.MTextStyle

data class TitleItemModel(
    override var type: Int = Constants.ITEM,
    override var groupIndex: Int = 0,
    override var itemTag: String = "",
    override var padding: EdgePadding? = null,
    override var data: Any? = null,
    var title: MTextStyle
): BaseItemInterface {

    companion object {
        fun fromMap(map: Map<String, Any>): TitleItemModel {
            val type = if (map["item_tag"] != null) Constants.ITEM else Constants.HEADER
            val itemTag = (map["item_tag"] as? String) ?: (map["section_tag"] as? String) ?: ""
            val title = if (type == Constants.ITEM) {
                map.getLocalizedString("title_localized")?.let { MTextStyle(it)
                } ?: MTextStyle("")
            } else {
                map.getLocalizedString("section_name_localized")?.let { MTextStyle(it)
                } ?: MTextStyle("")
            }
            return TitleItemModel(type = type, itemTag = itemTag, title = title)
        }
    }
}