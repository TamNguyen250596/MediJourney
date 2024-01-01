package com.example.medijourney.common.models.item_models

import com.example.medijourney.common.models.ui_models.EdgePadding

interface BaseItemInterface {
    var type: Int
    var groupIndex: Int
    var itemTag: String
    var data: Any?
    var padding: EdgePadding?
}