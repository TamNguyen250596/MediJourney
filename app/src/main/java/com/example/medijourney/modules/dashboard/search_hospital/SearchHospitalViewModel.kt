package com.example.medijourney.modules.dashboard.search_hospital

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.Hospital
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.HospitalRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class SearchHospitalViewModel @Inject constructor(
    private val hospitalRepository: HospitalRepo
) : ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    var didSearch: Boolean = false
    var highlightItemTag: String? = null
    private var observeHospitalsJob: Job? = null
    private var cursorTag: String? = null
    private var observeCurrentPageJob: Job? = null

    // Life cycle
    init {
        viewModelScope.launch {
            observeHospitalsJob = supervisorScope {
                launch {
                    observeFS()
                }
                launch {
                    observeData()
                }
            }
            launch { observeSearchText() }
        }
    }

    // Functions
    fun getHospitalMapData(itemModel: DynamicUIItem): Map<String, String> {
        val hospital = itemModel.data as? Hospital ?: return emptyMap()
        val values = mutableMapOf<String, String>()
        values[Constants.VALUE] = hospital.name ?: ""
        values[Constants.OBJECT_ID] = hospital.id
        return values
    }

    private suspend fun observeData(keywords: String? = null) {
        hospitalRepository.getHospitalsFlow(keywords)
            .firstThenDebounce(500)
            .collect {
                _itemModels.value = generateDynamicUIItemModels(it)
            }
    }

    private suspend fun observeFS(keywords: String? = null, cursor: String? = null) {
        hospitalRepository.observeHospitals(keywords, cursor)
            .collect {
                updateCursorTag(it)
            }
    }

    private fun generateDynamicUIItemModels(hospitals: List<Hospital>): List<DynamicUIItem> {
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
            observeHospitalsJob = supervisorScope {
                launch {
                    observeFS(keyword)
                }
                launch {
                    observeData(keyword)
                }
            }
        }
    }

    private fun resetAll() {
        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null

        _itemModels.value = emptyList()
        cursorTag = null
        observeHospitalsJob?.cancel()
        observeHospitalsJob = null
    }

    // Pagination
    fun fetchNextPage(index: Int) {
        val hospitalTag = getHospitalTag(index) ?: return
        val hospitalCursorTag = cursorTag ?: return
        if (hospitalTag <= hospitalCursorTag) return

        observeCurrentPageJob?.cancel()
        observeCurrentPageJob = null

        observeCurrentPageJob = viewModelScope.launch {
            hospitalRepository.observeHospitals(searchTextFlow.value, hospitalTag)
                .collect {
                    updateCursorTag(it)
                }
        }
    }

    private fun getHospitalTag(index: Int): String? {
        if (index >= _itemModels.value.size) return null
        val item = _itemModels.value[index]
        val conversation = item.data as? Conversation ?: return null
        if (!conversation.isValid()) return null

        return conversation.tag
    }

    private fun updateCursorTag(documents: List<Map<String, Any>>) {
        val data = documents.lastOrNull() ?: return
        val tag = data["tag"] as? String ?: return
        val cursorTag = cursorTag
        if (cursorTag != null && tag < cursorTag) return

        this.cursorTag = tag
    }
}