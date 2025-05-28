package com.example.medijourney.modules.dashboard.search_sub_specialties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Hospital
import com.example.medijourney.common.models.realm_models.MedicalSubSpecialty
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SearchSubSpecialtiesViewModel : ViewModel() {

    // Properties
    private val _itemModels = MutableStateFlow<List<DynamicUIItem>>(emptyList())
    val itemModels: StateFlow<List<DynamicUIItem>> = _itemModels.asStateFlow()
    val searchTextFlow = MutableStateFlow<String?>(null)
    var didSearch: Boolean = false
    var highlightItemTag: String? = null
    private var hospital: Hospital? = null
    private var medicalSubSpecialties: RealmResults<MedicalSubSpecialty>? = null
    private var observeMedicalSubSpecialtiesJob: Job? = null

    // Life cycle
    fun onViewCreated(hospitalId: String) {
        viewModelScope.launch {
            getData(hospitalId)
            observeMedicalSubSpecialtiesJob = launch { observeData() }
            launch { observeSearchText() }
        }
        _itemModels.value = generateDynamicUIItemList(medicalSubSpecialties)
    }

    // Functions
    private suspend fun getData(hospitalId: String) {
        hospital = RealmManager.read(Hospital::class.java, hospitalId)
        getMedicalSubSpecialties()
    }

    private suspend fun getMedicalSubSpecialties(keywords: String? = null) {
        val hospital = hospital ?: return
        if (!hospital.isValid()) return
        if (hospital.medicalSubSpecialties.isEmpty()) return
        var queries: MutableList<RQuery> = mutableListOf()

        queries.add(RQuery.Where(MedicalSubSpecialty::id.name, Operator.IN, hospital.medicalSubSpecialties))
        if (!keywords.isNullOrEmpty()) {
            queries.add(RQuery.Where(MedicalSubSpecialty::keywords.name, Operator.CONTAINS, keywords))
        }

        medicalSubSpecialties = RealmManager.read(
            clazz = MedicalSubSpecialty::class.java,
            realmQuery = RQuery.And(queries),
            sort = listOf(Pair(MedicalSubSpecialty::position.name, Sort.ASCENDING))
        )
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeData() {
        val medicalSubSpecialties = medicalSubSpecialties ?: return

        medicalSubSpecialties
            .asFlow()
            .debounce(500)
            .collect {
                this.medicalSubSpecialties = it.list
                _itemModels.value = generateDynamicUIItemList(this.medicalSubSpecialties)
            }
    }

    private fun generateDynamicUIItemList(list: RealmResults<MedicalSubSpecialty>?): List<DynamicUIItem> {
        list ?: return emptyList()
        return list.mapNotNull {
            if (!it.isValid()) return@mapNotNull null

            DynamicUIItem(
                type = Constants.ITEM,
                itemTag = it.id,
                groupIndex = 0,
                data = it,
                backgroundColor = 0,
            ).apply {
                it.title?.let {
                    title = MTextStyle(it)
                }
            }
        }
    }

    fun getMedicalSubSpecialityMapData(itemModel: DynamicUIItem): Map<String, String> {
        val medicalSubSpecialty = itemModel.data as? MedicalSubSpecialty ?: return emptyMap()
        if (!medicalSubSpecialty.isValid()) return emptyMap()
        val values = mutableMapOf<String, String>()
        values[Constants.VALUE] = medicalSubSpecialty.title ?: ""
        values[Constants.OBJECT_ID] = medicalSubSpecialty.id
        return values
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
        observeMedicalSubSpecialtiesJob?.cancel()
        observeMedicalSubSpecialtiesJob = null
        viewModelScope.launch {
            getMedicalSubSpecialties(keyword)
            observeMedicalSubSpecialtiesJob = launch { observeData() }
            _itemModels.value = generateDynamicUIItemList(medicalSubSpecialties)
        }
    }
}