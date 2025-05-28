package com.example.medijourney.modules.dashboard.educational_organizations

import androidx.lifecycle.ViewModel
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EducationalOrganizationsViewModel : ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private var currentEducationalOrganizations: Map<String, Any> = emptyMap()

    // Life cycle
    fun onViewCreated() {
        currentEducationalOrganizations = InternationManager.getCurrentAppEducationalOrganizations()
        _itemModels.value = generateItemModels(currentEducationalOrganizations)
    }

    // Functions
    @Suppress("UNCHECKED_CAST")
    private fun generateItemModels(data: Map<String, Any>): List<DynamicUIItem> {
        val attributes = data["attributes"] as? List<Map<String, Any>> ?: return emptyList()
        if (attributes.isEmpty()) return emptyList()

        return attributes.map {
            DynamicUIItem.fromMap(it).apply {
                image?.name?.let {
                    image?.url = "images/educational_organizations/${it}.jpg"
                }
            }
        }
    }
}