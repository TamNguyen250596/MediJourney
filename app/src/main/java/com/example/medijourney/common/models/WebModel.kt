package com.example.medijourney.common.models

import com.example.medijourney.common.ui_components.fragments.web_view.WebViewConstant
import java.io.Serializable

class WebModel : Serializable {
    var webViewType: WebViewConstant? = null
    var title: String? = null
    var url: String? = null
}