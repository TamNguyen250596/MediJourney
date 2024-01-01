package com.example.medijourney.modules.dashboard.medication_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.CurrencyHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
import com.example.medijourney.common.managers.fire_store.remove
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.MedicalProduct
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class MedicationListViewModel : ViewModel() {

    // Properties
    private val _isLoading = MutableStateFlow(value = true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    private var medicalProductResults: RealmResults<MedicalProduct>? = null
    private var observeMedicalProductsJob: Job? = null
    private var cursorPosition: Int? = null
    private var observeFistPageQuery: Query? = null
    private var currentPageQuery: MutableList<Query> = mutableListOf()
    private var observeCurrentPageJob: Job? = null
    
    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            observeFS(null)
            getMedicalProducts(null)
            _itemModels.value = generateDynamicUIItemModels(medicalProductResults)
            observeMedicalProductsJob = launch { observeMedicalProducts() }
            launch { observeSearchText() }
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeFistPageQuery?.remove()
        currentPageQuery.forEach { it.remove() }
    }

    // Functions
    fun getMedicalProductId(itemModel: DynamicUIItem): String? {
        val medicalProduct = itemModel.data as? MedicalProduct ?: return null
        if (!medicalProduct.isValid()) return null

        return medicalProduct.id
    }

    private fun observeFS(keyword: String?) {
        observeFistPageQuery = FireStoreManager.buildCollectionRef(FireStoreCollection.MEDICAL_PRODUCTS)
            .apply {
                if (!keyword.isNullOrEmpty()) {
                    whereArrayContains("keywords", keyword)
                }
            }
            .orderBy("position", Query.Direction.ASCENDING)

        observeFistPageQuery?.addListener {
            updateCursorPosition(it.documents)
            saveDocuments(it.documents)
        }
    }

    private suspend fun getMedicalProducts(keyword: String?) {
        val query = if (!keyword.isNullOrEmpty()) {
            RQuery.Where(MedicalProduct::keywords.name, Operator.CONTAINS, keyword)
        } else {
            null
        }
        medicalProductResults = RealmManager.read(
            clazz = MedicalProduct::class.java,
            realmQuery = query,
            sort = listOf(Pair(MedicalProduct::position.name, Sort.ASCENDING))
        )
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeMedicalProducts() {
        val results = medicalProductResults ?: return

        results
            .asFlow()
            .debounce(500)
            .collect {
                medicalProductResults = it.list
                _itemModels.value = generateDynamicUIItemModels(medicalProductResults)
        }
    }

    private fun generateDynamicUIItemModels(medicalProducts: RealmResults<MedicalProduct>?): List<DynamicUIItem> {
        medicalProducts ?: return emptyList()

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
    @OptIn(FlowPreview::class)
    private suspend fun observeSearchText() {
        searchTextFlow
            .debounce(500)
            .collect {
                searchMedicalProduct(it)
            }
    }

    private fun searchMedicalProduct(keywords: String?) {
        _isLoading.value = true
        cursorPosition = null
        observeMedicalProductsJob?.cancel()
        observeMedicalProductsJob = null
        currentPageQuery.forEach { it.remove() }
        currentPageQuery.clear()
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null
        viewModelScope.launch {
            getMedicalProducts(keywords)
            observeFS(keywords)
            observeMedicalProductsJob = launch { observeMedicalProducts() }
            _itemModels.value = generateDynamicUIItemModels(medicalProductResults)
            _isLoading.value = false
        }
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        val medicalProductPosition = getMedicalProductionPosition(index) ?: return
        val cursorPosition = this.cursorPosition ?: return
        if (medicalProductPosition < cursorPosition) return

        getCurrentPage(cursorPosition)
    }

    private fun getMedicalProductionPosition(index: Int): Int? {
        if (index >= _itemModels.value.size) return null
        val item = _itemModels.value[index]
        val medicalProduct = item.data as? MedicalProduct ?: return null
        if (!medicalProduct.isValid()) return null

        return medicalProduct.position
    }

    private fun getCurrentPage(cursorPosition: Int) {
        FireStoreManager.buildCollectionRef(FireStoreCollection.MEDICAL_PRODUCTS)
            .apply {
                if (!searchTextFlow.value.isNullOrEmpty()) {
                    whereArrayContains("keywords", searchTextFlow.value.toString())
                }
            }
            .whereGreaterThan("position", cursorPosition)
            .orderBy("position", Query.Direction.ASCENDING)
            .limit(Constants.DEFAULT_LIMIT)
            .get()
            .addOnSuccessListener { snapshot ->
                updateCursorPosition(snapshot.documents)
                saveDocuments(snapshot.documents)
                observeCurrentPage()
            }
    }

    private fun observeCurrentPage() {
        observeCurrentPageJob = viewModelScope.launch {
            delay(10_000)
            cursorPosition?.let { position ->

                FireStoreManager.buildCollectionRef(FireStoreCollection.MEDICAL_PRODUCTS)
                    .apply {
                        if (!searchTextFlow.value.isNullOrEmpty()) {
                            whereArrayContains("keywords", searchTextFlow.value.toString())
                        }
                    }
                    .whereGreaterThan("position", position)
                    .orderBy("position")
                    .limit(Constants.DEFAULT_LIMIT)
                    .also {
                        currentPageQuery.add(it)
                        it.addListener { snapshots ->
                            updateCursorPosition(snapshots.documents)
                            saveDocuments(snapshots.documents)
                        }
                    }

                FireStoreManager.buildCollectionRef(FireStoreCollection.MEDICAL_PRODUCTS)
                    .apply {
                        if (!searchTextFlow.value.isNullOrEmpty()) {
                            whereArrayContains("keywords", searchTextFlow.value.toString())
                        }
                    }
                    .whereLessThanOrEqualTo("position", position)
                    .orderBy("position")
                    .limit(Constants.DEFAULT_LIMIT)
                    .also {
                        currentPageQuery.add(it)
                        it.addListener { snapshots ->
                            saveDocuments(snapshots.documents)
                        }
                    }
            }
        }
    }

    private fun updateCursorPosition(documents: List<DocumentSnapshot>) {
        val lastDocument = documents.lastOrNull() ?: return
        val data = lastDocument.data ?: return
        val position = data["position"] as? Int ?: return
        val cursorPosition = cursorPosition
        if (cursorPosition != null && position < cursorPosition) return

        this.cursorPosition = position
    }

    private fun saveDocuments(documents: List<DocumentSnapshot>) {
        viewModelScope.launch {
            documents.forEach { doc ->
                doc.data?.let { data ->
                    RealmManager.create(MedicalProduct::class.java, data)
                }
            }
        }
    }
}