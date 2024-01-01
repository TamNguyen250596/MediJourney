package com.example.medijourney.modules.dashboard.home.sub_views.medical_purchase_progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.UserMedicalProduct
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class MedicalPurchaseProgressViewModel: ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    private var currentAppMedicalPurchaseProgress: Map<String, Any> = emptyMap()
    private var userMedicalProductResults: RealmResults<UserMedicalProduct>? = null

    // Life cycle
    fun onViewCreated() {
        currentAppMedicalPurchaseProgress = InternationManager.getCurrentAppMedicalPurchaseProgress()
        _itemModels.value = generateItemModelList(currentAppMedicalPurchaseProgress)
        viewModelScope.launch {
            getData()
            observeData()
        }
    }

    // Functions
    private suspend fun getData() {
        userMedicalProductResults = RealmManager.read(UserMedicalProduct::class.java)
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val userMedicalProductResults = userMedicalProductResults ?: return
        userMedicalProductResults
            .asFlow()
            .debounce(500)
            .collect {
            this.userMedicalProductResults = it.list
            _itemModels.value = generateItemModelList(currentAppMedicalPurchaseProgress)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun generateItemModelList(data: Map<String, Any>): List<DynamicUIItem> {
        val attributes = data["attributes"] as? List<Map<String, Any>> ?: return emptyList()
        if (attributes.isEmpty()) return emptyList()
        return attributes.map {
            DynamicUIItem.fromMap(it).apply {
                val userMedicalProducts = userMedicalProductResults ?: return@apply
                val numberOfItem = userMedicalProducts.count { it.isValid() &&
                        it.status == itemTag && !it.isRead }
                if (numberOfItem == 0) return@apply
                description = MTextStyle(text = numberOfItem.toString())
            }
        }
    }
}