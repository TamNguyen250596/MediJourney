package com.example.medijourney.modules.dashboard.medication_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.CurrencyHelper
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.MedicalProduct
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.MedicalProductRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class MedicationListViewModel @Inject constructor(
    private val medicalProductRepository: MedicalProductRepo
) : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    private var observeMedicalProductsJob: Job? = null
    private var cursorPosition: Int? = null
    private var observeCurrentPageJob: Job? = null
    
    // Life cycle
    fun onViewCreated() {
        _isLoading.value = true
        viewModelScope.launch {
            observeMedicalProductsJob = supervisorScope {
                launch {
                    observeFS(null)
                    _isLoading.value = false
                }
                launch {
                    observeMedicalProducts(null)
                }
            }
            launch {
                observeSearchText()
            }
        }
    }

    // Functions
    fun getMedicalProductId(itemModel: DynamicUIItem): String? {
        val medicalProduct = itemModel.data as? MedicalProduct ?: return null
        if (!medicalProduct.isValid()) return null

        return medicalProduct.id
    }

    private suspend fun observeFS(keyword: String?) {
        medicalProductRepository.observeMedicalProducts(keyword, null)
            .collect {
                updateCursorPosition(it)
            }
    }

    private suspend fun observeMedicalProducts(keyword: String?) {
        medicalProductRepository.getMedicalProductsFlow(keyword)
            .firstThenDebounce(500)
            .collect {
                _itemModels.value = generateDynamicUIItemModels(it)
            }
    }

    private fun generateDynamicUIItemModels(medicalProducts: List<MedicalProduct>): List<DynamicUIItem> {
        return medicalProducts.mapNotNull {
            if (!it.isValid()) return@mapNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.id,
                groupIndex = 0,
                data = it,
                backgroundColor = 0
                ).apply {
                it.imageName?.let {
                    image = ImageStyle(url = "images/medical_products/${it}.png")
                }
                it.name?.let { name ->
                    title = MTextStyle(name)
                }
                CurrencyHelper.getPrice(it.price, "USD").let {
                    description = MTextStyle(it)
                }
            }
        }
    }

    // Search Messages
    private suspend fun observeSearchText() {
        searchTextFlow
            .firstThenDebounce(500)
            .collect {
                searchMedicalProduct()
            }
    }

    private fun searchMedicalProduct() {
        _isLoading.value = true
        cursorPosition = null
        observeMedicalProductsJob?.cancel()
        observeMedicalProductsJob = null
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null

        viewModelScope.launch {
            observeMedicalProductsJob = supervisorScope {
                launch {
                    observeFS(searchTextFlow.value)
                    _isLoading.value = false
                }
                launch {
                    observeMedicalProducts(searchTextFlow.value)
                }
            }
        }
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        val medicalProductPosition = getMedicalProductionPosition(index) ?: return
        val cursorPosition = this.cursorPosition ?: return
        if (medicalProductPosition < cursorPosition) return

        observeCurrentPageJob = viewModelScope.launch {
            medicalProductRepository.observeMedicalProducts(
                searchTextFlow.value,
                medicalProductPosition
            )
                .collect {
                    updateCursorPosition(it)
                }
        }
    }

    private fun getMedicalProductionPosition(index: Int): Int? {
        if (index >= _itemModels.value.size) return null
        val item = _itemModels.value[index]
        val medicalProduct = item.data as? MedicalProduct ?: return null
        if (!medicalProduct.isValid()) return null

        return medicalProduct.position
    }

    private fun updateCursorPosition(documents: List<Map<String, Any>>) {
        val data = documents.lastOrNull() ?: return
        val position = data["position"] as? Int ?: return
        val cursorPosition = cursorPosition
        if (cursorPosition != null && position < cursorPosition) return

        this.cursorPosition = position
    }
}