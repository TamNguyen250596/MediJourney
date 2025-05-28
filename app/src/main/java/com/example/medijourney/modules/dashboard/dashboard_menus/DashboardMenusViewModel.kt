package com.example.medijourney.modules.dashboard.dashboard_menus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.models.item_models.ImageItemModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class DashboardMenusViewModel : ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<ImageItemModel>>(emptyList())
    val itemModels: StateFlow<List<ImageItemModel>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    private var currentAppDashboardMenus: Map<String, Any> = emptyMap()

    // Life cycle
    fun onViewCreated() {
        currentAppDashboardMenus = InternationManager.getCurrentAppDashboardMenus()
        _itemModels.value = generateItemModels(currentAppDashboardMenus)
        viewModelScope.launch {
            observeSearchText()
        }
    }

    // Search Conversations
    @OptIn(FlowPreview::class)
    private suspend fun observeSearchText() {
        searchTextFlow
            .debounce(500)
            .collect {
                _itemModels.value = generateItemModels(currentAppDashboardMenus, it)
            }
    }

    // Functions
    @Suppress("UNCHECKED_CAST")
    private fun generateItemModels(data: Map<String, Any>, searchText: String? = null): List<ImageItemModel> {
        val attributes = data["attributes"] as? List<Map<String, Any>> ?: return emptyList()
        if (attributes.isEmpty()) return emptyList()
        var models = mutableListOf<ImageItemModel>()

        attributes.forEach {
            val items = it["items"] as? List<Map<String, Any>> ?: return@forEach
            if (items.isEmpty()) return@forEach
            val headerModel = ImageItemModel.Companion.fromMap(it)

            items.mapNotNull {
                val item = ImageItemModel.Companion.fromMap(it)

                if (searchText.isNullOrEmpty()) {
                    item
                } else {
                    val keywords = it.getLocalizedString("keywords") ?: return@mapNotNull item
                    if (keywords.contains(searchText)) {
                        item
                    } else {
                        null
                    }
                }
            }.also {
                if (it.isNotEmpty()) {
                    models.add(headerModel)
                    models.addAll(it)
                }
            }
        }
        return models
    }
}