package com.example.medijourney.modules.profile.share_app

import android.content.res.Resources
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.ui_models.EdgePadding

class ShareAppViewModel : ViewModel() {

    // Properties
    var items = MutableLiveData<MutableList<ImageItemModel>>(mutableListOf())
    private var appShareJson: Map<String, Any> = mapOf()
    private var width: Int = 0

    // Life cycle
    fun onViewCreated() {
        appShareJson = InternationManager.getCurrentAppShare()
        width = getScreenWidth()
        items.postValue(generateItems())
    }

    // Functions
    fun getShareUri(map: Map<String, Any>): Uri {
        val shareUrlString = map["share_url"] as? String ?: return Uri.EMPTY
        return shareUrlString.toUri()
    }

    @Suppress("UNCHECKED_CAST")
    fun getShareContent(map: Map<String, Any>): String {
        val shareContent = map["share_content"] as? Map<String, String> ?: return ""
        return shareContent[InternationManager.currentLocale] ?: ""
    }

    @Suppress("UNCHECKED_CAST")
    fun getTelegramShareContent(map: Map<String, Any>): String {
        map["share_url"] as? String ?: return ""
        val shareContent = map["share_content"] as? Map<String, String> ?: return ""
        val content = shareContent[InternationManager.currentLocale] ?: ""
        return content + shareContent
    }

    private fun getScreenWidth(): Int {
        val metrics = Resources.getSystem().displayMetrics
        return metrics.widthPixels / metrics.density.toInt()
    }

    @Suppress("UNCHECKED_CAST")
    private fun generateItems(): MutableList<ImageItemModel> {
        val attributes = appShareJson["attributes"] as? List<Map<String, Any>> ?: return mutableListOf()
        val imageWidth = 48
        val sidePadding = 32
        val left = (width - imageWidth * attributes.size - sidePadding) / (attributes.size - 1)
        return attributes.mapIndexed { index, map ->
            ImageItemModel.fromMap(map).apply {
                image.size = Pair(imageWidth, imageWidth)
                title?.size = 12f
                padding = EdgePadding(40,40, if (index == 0) 0 else left, 0)
                data = map
            }
        }.toMutableList()
    }
}