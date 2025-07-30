package com.example.medijourney.modules.dashboard.home.sub_views.advertisement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.WebModel
import com.example.medijourney.common.models.item_models.ImageItemModel
import com.example.medijourney.common.models.realm_models.Advertisement
import com.example.medijourney.common.models.ui_models.ImageStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class AdvertisementViewModel: ViewModel() {

    // Properties
    private val _items = MutableStateFlow<List<ImageItemModel>>(emptyList())
    val items: StateFlow<List<ImageItemModel>> = _items.asStateFlow()
    private var advertisementResults: RealmResults<Advertisement>? = null

    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            getAdvertisements()
            _items.value = generateItems(advertisementResults)
            observeAdvertisements()
        }
    }

    // Functions
    private suspend fun getAdvertisements() {
        advertisementResults = RealmManager.read(
            clazz = Advertisement::class.java,
            realmQuery = RQuery.Where(Advertisement::location.name, Operator.EQUAL, "banner"),
            sort = listOf(
                Pair(Advertisement::position.name, Sort.DESCENDING)
            )
        )
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeAdvertisements() {
        val advertisements = advertisementResults ?: return

        advertisements
            .asFlow()
            .debounce(500)
            .collect {
                advertisementResults = it.list
                _items.value = generateItems(advertisementResults)
            }
    }

    private fun generateItems(advertisements: RealmResults<Advertisement>?): List<ImageItemModel> {
        advertisements ?: return emptyList()
        return advertisements.mapNotNull { ad ->
            if (!ad.isValid()) return@mapNotNull null

            ImageItemModel(
                itemTag = ad.id,
                data = ad,
                image = ImageStyle().apply {
                    url = "images/advertisements/${ad.imageName}.jpg"
                }
            )
        }
    }

    fun getInitialPage(): Int {
        if (items.value.isEmpty()) return 0
        val maxPage = Int.MAX_VALUE
        val middleInt = maxPage / 2
        return (middleInt / items.value.size) * items.value.size
    }

    fun getItemIndex(page: Int): Int {
        if (items.value.isEmpty()) return 0
        return page % items.value.size
    }

    fun getItemModel(page: Int): ImageItemModel? {
        val index = getItemIndex(page)
        return items.value.getOrNull(index)
    }

    fun getWebViewModel(model: ImageItemModel?): WebModel? {
        val realmObject = model?.data as? Advertisement ?: return null
        if (!realmObject.isValid()) return null
        val actionUrl = realmObject.actionUrl ?: return null
        return WebModel().apply {
            url = actionUrl
        }
    }
}