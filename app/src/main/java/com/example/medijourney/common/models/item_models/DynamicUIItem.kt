package com.example.medijourney.common.models.item_models

import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle

data class DynamicUIItem(
    override var type: Int,
    override var groupIndex: Int = 0,
    override var itemTag: String,
    override var data: Any? = null,
    override var padding: EdgePadding? = null,
    var image: ImageStyle? = null,
    var secondaryImage: ImageStyle? = null,
    var title: MTextStyle? = null,
    var description: MTextStyle? = null,
    var secondaryDescription: MTextStyle? = null,
    var backgroundColor: Int,
    var additionalData: Any? = null
): BaseItemInterface {

    // Companion
    companion object {
        fun fromMap(map: Map<String, Any>): DynamicUIItem {
            val type = if (map["item_tag"] != null) Constants.ITEM else Constants.HEADER
            val itemTag = (map["item_tag"] as? String) ?: (map["section_tag"] as? String) ?: ""
            val title = if (type == Constants.ITEM) {
                map.getLocalizedString("title_localized")?.let { MTextStyle(it)
                } ?: MTextStyle("")
            } else {
                map.getLocalizedString("section_name_localized")?.let { MTextStyle(it)
                } ?: MTextStyle("")
            }
            val description = map.getLocalizedString("description_localized")?.let { MTextStyle(it) }
            val image = (map["image_name"] as? String)?.let { ImageStyle(it) }
            val secondaryImage = (map["secondary_image_name"] as? String)?.let { ImageStyle(it) }

            return DynamicUIItem(
                type = type,
                itemTag = itemTag,
                groupIndex = 0,
                data = null,
                padding = null,
                image = image,
                secondaryImage = secondaryImage,
                title = title,
                description = description,
                secondaryDescription = null,
                backgroundColor = R.color.white,
                additionalData = null
            )
        }
    }

    // Functions
    fun getAdditionalValue(key: String): Any? {
        return additionalData?.let {
            if (it is Map<*, *>) {
                it[key]
            } else {
                null
            }
        }
    }
}