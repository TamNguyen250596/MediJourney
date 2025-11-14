package com.example.medijourney.modules.health_center.fitness_tracker_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.common.models.realm_models.NotificationType
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.UserFitnessTrackerRepo
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FitnessTrackerDetailViewModel @Inject constructor(
    saveState: SavedStateHandle,
    private val userFitnessTrackerRepo: UserFitnessTrackerRepo
) : ViewModel() {

    // Properties
    var itemModels = MutableLiveData<MutableList<BaseItemInterface>>()
    private var userFitnessTrackerId: String? = saveState.get<String>("userFitnessTrackerId")
    private var currentAppFitnessTrackerDetails: Map<String, Any> = mutableMapOf()
    var didHandledNavigation = false

    // Life cycle
    init {
        currentAppFitnessTrackerDetails = InternationManager.getCurrentAppFitnessTrackerDetail()
        viewModelScope.launch {
            observeData()
        }
    }

    // Functions
    fun getNotificationType(tag: String?): NotificationType? {
        tag ?: return null
        return NotificationType.valueOf(tag)
    }

    private suspend fun observeData() {
        val id = userFitnessTrackerId ?: return

        userFitnessTrackerRepo
            .getUserFitnessTrackerFlow(id)
            .collect {
                val userFitnessTracker = it ?: return@collect
                itemModels.postValue(generateItemModels(userFitnessTracker))
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun generateItemModels(userFitnessTracker: UserFitnessTracker): MutableList<BaseItemInterface> {
        val attributes = currentAppFitnessTrackerDetails["attributes"] as? List<Map<String, Any>> ?: return mutableListOf()
        val dataList: MutableList<BaseItemInterface> = mutableListOf()

        for ((sectionIndex, attribute) in attributes.withIndex()) {
            val items = attribute["items"] as? List<Map<String, Any>>
            if (items.isNullOrEmpty()) continue

            val titleItemModel = TitleItemModel.fromMap(attribute)
            titleItemModel.title.font = R.font.proximanova_bold
            titleItemModel.groupIndex = sectionIndex
            dataList.add(titleItemModel)

            for ((itemIndex, item) in items.withIndex()) {
                val itemModel = generateDynamicUIModel(item, userFitnessTracker)
                val isHideSeparator = itemIndex == items.lastIndex
                itemModel.groupIndex = sectionIndex
                itemModel.additionalData = mapOf(HDualImageTextViewHolder.IS_HIDE_SEPARATOR_KEY to isHideSeparator)
                itemModel.data = userFitnessTracker
                dataList.add(itemModel)
            }
        }

        return dataList
    }

    private fun generateDynamicUIModel(map: Map<String, Any>, userFitnessTracker: UserFitnessTracker): DynamicUIItem {
        val itemModel = DynamicUIItem.fromMap(map)
        if (!userFitnessTracker.isValid()) return itemModel
        val description = MTextStyle("", R.font.proximanova_regular, 16f, R.color.black)

        when (itemModel.itemTag) {
            "name" -> {
                description.text = userFitnessTracker.name ?: ""
            }
            "model" -> {
                description.text = userFitnessTracker.model ?: ""
            }
            "version" -> {
                description.text = userFitnessTracker.version ?: ""
            }
            "device_id" -> {
                description.text = userFitnessTracker.deviceId
            }
            "battery_percentage" -> {
                description.text = "${userFitnessTracker.batteryPercentage}%"
            }
            "battery_maximum_capacity" -> {
                description.text = "${userFitnessTracker.batteryMaximumCapacity}%"
            }
            "storage_capacity" -> {
                description.text = "${userFitnessTracker.storageCapacity} GB"
            }
            "storage_availability" -> {
                description.text = "${userFitnessTracker.storageAvailability} GB"
            }
        }
        if (description.text.isNotEmpty()) {
            itemModel.description = description
        }
        return itemModel
    }

    fun getItemTag(model: BaseItemInterface?): String? {
        return (model as? DynamicUIItem)?.itemTag
    }

    fun checkEnableActivity(model: BaseItemInterface?): Boolean {
        val dynamicUIItem = model as? DynamicUIItem ?: return false
        val userFitnessTracker = dynamicUIItem.data as? UserFitnessTracker ?: return false
        if (!userFitnessTracker.isValid()) return false

        val activity: Int = when (dynamicUIItem.itemTag) {
            "sleep_tracking" -> 3
            "nutrition_tracking" -> 2
            "exercise_tracking" -> 1
            else -> return false
        }
        return userFitnessTracker.trackingActivities.contains(activity)
    }

    fun deleteUserFitnessTracker(completion: ((Boolean) -> Unit)? = null) {
        userFitnessTrackerId?.let { id ->
            viewModelScope.launch {
                val result = userFitnessTrackerRepo.deleteUserFitnessTracker(id)
                completion?.invoke(result)
            }
        }
    }
}