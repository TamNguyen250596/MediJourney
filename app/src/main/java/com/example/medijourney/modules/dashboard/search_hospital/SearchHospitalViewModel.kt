package com.example.medijourney.modules.dashboard.search_hospital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
import com.example.medijourney.common.managers.fire_store.remove
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.Hospital
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
import kotlin.collections.forEach

class SearchHospitalViewModel : ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    var didSearch: Boolean = false
    private var hospitalResults: RealmResults<Hospital>? = null
    private var observeHospitalsJob: Job? = null
    private var cursorTag: String? = null
    private var firstPageQuery: Query? = null
    private var currentPageQuery: MutableList<Query> = mutableListOf()
    private var observeCurrentPageJob: Job? = null
    var highlightItemTag: String? = null

    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            getData()
            observeFS()
            observeHospitalsJob = launch { observeData() }
            launch { observeSearchText() }
        }
        _itemModels.value = generateDynamicUIItemModels(hospitalResults)
    }

    override fun onCleared() {
        super.onCleared()
        removeAllFSListeners()
    }

    // Functions
    fun getHospitalMapData(itemModel: DynamicUIItem): Map<String, String> {
        val hospital = itemModel.data as? Hospital ?: return emptyMap()
        val values = mutableMapOf<String, String>()
        values[Constants.VALUE] = hospital.name ?: ""
        values[Constants.OBJECT_ID] = hospital.id
        return values
    }

    private suspend fun getData(keywords: String? = null) {
        var query: RQuery? = null

        if (!keywords.isNullOrEmpty()) {
            query = RQuery.Where(Hospital::keywords.name, Operator.CONTAINS, keywords)
        }
        hospitalResults = RealmManager.read(
            clazz = Hospital::class.java,
            realmQuery = query,
            sort = listOf(Hospital::tag.name to Sort.ASCENDING)
        )
    }

    private fun observeFS(keywords: String? = null) {
        firstPageQuery = FireStoreManager.buildCollection(FireStoreCollection.HOSPITALS)
            .apply {
                if (!keywords.isNullOrEmpty()) {
                    whereArrayContains(Hospital::keywords.name, keywords)
                }
            }
            .orderBy(Hospital::tag.name)
            .limit(Constants.DEFAULT_LIMIT)

        firstPageQuery?.addListener {
            updateCursorTag(it.documents)
            saveDocuments(it.documents)
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val hospitals = hospitalResults ?: return

        hospitals.asFlow()
            .debounce(500)
            .collect {
                hospitalResults = it.list
                _itemModels.value = generateDynamicUIItemModels(hospitalResults)
            }
    }

    private fun generateDynamicUIItemModels(hospitals: RealmResults<Hospital>?): List<DynamicUIItem> {
        hospitals ?: return emptyList()
        return hospitals.mapNotNull {
            if (!it.isValid()) return@mapNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.id,
                groupIndex = 0,
                data = it,
                backgroundColor = 0,
            ).apply {
                image = ImageStyle().apply {
                    it.imageName?.let { imageName ->
                        url = "images/hospitals/${imageName}.jpg"
                    }
                    name = "ic_hospital"
                }
                it.name?.let {
                    title = MTextStyle(it)
                }
                it.location?.let { des ->
                    description = MTextStyle(des)
                }
            }
        }
    }

    // Search Hospital
    @OptIn(FlowPreview::class)
    private suspend fun observeSearchText() {
        searchTextFlow
            .debounce(500)
            .collect {
                if (!didSearch) return@collect
                searchHospitals(it)
            }
    }

    private fun searchHospitals(keyword: String?) {
        resetAll()
        viewModelScope.launch {
            getData(keyword)
            observeFS(keyword)
            observeHospitalsJob = launch { observeData() }
            _itemModels.value = generateDynamicUIItemModels(hospitalResults)
        }
    }

    private fun resetAll() {
        removeAllFSListeners()
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null

        _itemModels.value = emptyList()
        hospitalResults = null
        cursorTag = null
        observeHospitalsJob?.cancel()
        observeHospitalsJob = null
    }

    private fun removeAllFSListeners() {
        firstPageQuery?.remove()
        firstPageQuery = null
        currentPageQuery.forEach { it.remove() }
        currentPageQuery.clear()
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        val hospitalTag = getHospitalTag(index) ?: return
        val hospitalCursorTag = cursorTag ?: return
        if (hospitalTag <= hospitalCursorTag) return
        currentPageQuery.forEach { it.remove() }
        currentPageQuery.clear()
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null
        getCurrentPage(hospitalTag)
    }

    private fun getHospitalTag(index: Int): String? {
        if (index >= _itemModels.value.size) return null
        val item = _itemModels.value[index]
        val conversation = item.data as? Conversation ?: return null
        if (!conversation.isValid()) return null

        return conversation.tag
    }

    private fun getCurrentPage(hospitalTag: String) {
        FireStoreManager.buildCollection(FireStoreCollection.HOSPITALS)
            .whereGreaterThan(Hospital::tag.name, hospitalTag)
            .orderBy(Hospital::tag.name)
            .limit(Constants.DEFAULT_LIMIT)
            .get()
            .addOnSuccessListener {
                updateCursorTag(it.documents)
                saveDocuments(it.documents)
                observeCurrentPage()
            }
    }

    private fun observeCurrentPage() {
        observeCurrentPageJob = viewModelScope.launch {
            delay(10_000)
            cursorTag?.let { tag ->

                FireStoreManager.buildCollection(FireStoreCollection.HOSPITALS)
                    .whereGreaterThan(Hospital::tag.name, tag)
                    .orderBy(Hospital::tag.name)
                    .limit(Constants.DEFAULT_LIMIT)
                    .also {
                        currentPageQuery.add(it)
                        it.addListener { snapshots ->
                            updateCursorTag(snapshots.documents)
                            saveDocuments(snapshots.documents)
                        }
                    }

                FireStoreManager.buildCollection(FireStoreCollection.HOSPITALS)
                    .whereLessThanOrEqualTo(Hospital::tag.name, tag)
                    .orderBy(Hospital::tag.name)
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

    private fun updateCursorTag(documents: List<DocumentSnapshot>) {
        val lastDocument = documents.lastOrNull() ?: return
        val data = lastDocument.data ?: return
        val tag = data["tag"] as? String ?: return
        val cursorTag = cursorTag
        if (cursorTag != null && tag < cursorTag) return

        this.cursorTag = tag
    }

    private fun saveDocuments(documents: List<DocumentSnapshot>) {
        viewModelScope.launch {
            documents.forEach { doc ->
                doc.data?.let { data ->
                    RealmManager.create(Hospital::class.java, data)
                }
            }
        }
    }
}