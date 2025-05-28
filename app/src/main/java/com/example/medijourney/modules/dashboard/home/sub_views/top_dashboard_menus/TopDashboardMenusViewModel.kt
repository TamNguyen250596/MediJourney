package com.example.medijourney.modules.dashboard.home.sub_views.top_dashboard_menus

import androidx.lifecycle.ViewModel
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.models.item_models.ImageItemModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TopDashboardMenusViewModel(): ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<ImageItemModel>>(emptyList())
    val itemModels: StateFlow<List<ImageItemModel>> = _itemModels.asStateFlow()
    private var currentAppDashboardMenus: Map<String, Any> = emptyMap()

    // Life cycle
    fun onViewCreated() {
        currentAppDashboardMenus = InternationManager.getCurrentAppDashboardMenus()
        _itemModels.value = generateItemModels(currentAppDashboardMenus)
    }

    // Functions
    @Suppress("UNCHECKED_CAST")
    private fun generateItemModels(data: Map<String, Any>): List<ImageItemModel> {
        val topMenus = data["top_menus"] as? List<String> ?: return emptyList()
        if (topMenus.isEmpty()) return emptyList()
        val attributes = data["attributes"] as? List<Map<String, Any>> ?: return emptyList()
        if (attributes.isEmpty()) return emptyList()
        var itemModels = mutableListOf<ImageItemModel>()

        attributes.forEach {
            val items = it["items"] as? List<Map<String, Any>> ?: return@forEach
            if (items.isEmpty()) return@forEach
            items.forEach {
                val itemTag = it["item_tag"] as? String ?: return@forEach
                if (topMenus.contains(itemTag)) {
                    val itemModel = ImageItemModel.fromMap(it)
                    itemModels.add(itemModel)
                }
            }
        }
        return itemModels
    }
}