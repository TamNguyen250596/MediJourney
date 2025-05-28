package com.example.medijourney.modules.health_center.fitness_tracker_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.common.models.realm_models.NotificationType
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.launch

class FitnessTrackerDetailViewModel : ViewModel() {

    // Properties
    var itemModels = MutableLiveData<MutableList<BaseItemInterface>>()
    private var userFitnessTrackerId: String? = null
    private var userFitnessTracker: UserFitnessTracker? = null
    private var currentAppFitnessTrackerDetails: Map<String, Any> = mutableMapOf()
    var didHandledNavigation = false

    // Life cycle
    fun onViewCreated(userFitnessTrackerId: String?) {
        currentAppFitnessTrackerDetails = InternationManager.getCurrentAppFitnessTrackerDetail()

        viewModelScope.launch {
            this@FitnessTrackerDetailViewModel.userFitnessTrackerId = userFitnessTrackerId
            getData()
            generateItemModels()?.let { model ->
                itemModels.postValue(model)
            }
            observeData()
        }
    }

    // Functions
    fun getNotificationType(tag: String?): NotificationType? {
        tag?.let {
            return NotificationType.valueOf(tag)
        } ?: return null
    }

    private suspend fun getData() {
        userFitnessTrackerId?.let {
            userFitnessTracker = RealmManager.read(UserFitnessTracker::class.java, it)
        }
    }

    private suspend fun observeData() {
        val userFitnessTracker = userFitnessTracker ?: return

        userFitnessTracker.asFlow().collect {
            this.userFitnessTracker = it.obj

            generateItemModels()?.let { model ->
                itemModels.postValue(model)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun generateItemModels(): MutableList<BaseItemInterface>? {
        val attributes = currentAppFitnessTrackerDetails["attributes"] as? List<Map<String, Any>> ?: return null
        val dataList: MutableList<BaseItemInterface> = mutableListOf()

        for ((sectionIndex, attribute) in attributes.withIndex()) {
            val items = attribute["items"] as? List<Map<String, Any>>
            if (items.isNullOrEmpty()) continue

            val titleItemModel = TitleItemModel.fromMap(attribute)
            titleItemModel.title.font = R.font.proximanova_bold
            titleItemModel.groupIndex = sectionIndex
            dataList.add(titleItemModel)

            for ((itemIndex, item) in items.withIndex()) {
                val itemModel = generateDynamicUIModel(item)
                val isHideSeparator = itemIndex == items.lastIndex
                itemModel.groupIndex = sectionIndex
                itemModel.additionalData = mapOf(HDualImageTextViewHolder.IS_HIDE_SEPARATOR_KEY to isHideSeparator)
                itemModel.data = userFitnessTracker
                dataList.add(itemModel)
            }
        }

        return dataList
    }

    private fun generateDynamicUIModel(map: Map<String, Any>): DynamicUIItem {
        val itemModel = DynamicUIItem.fromMap(map)
        val userFitnessTracker = userFitnessTracker ?: return itemModel
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
            FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_FITNESS_TRACKERS, id))
                .delete()
                .addOnSuccessListener {
                    viewModelScope.launch {
                        RealmManager.delete(UserFitnessTracker::class.java, id)
                        completion?.invoke(true)
                    }
                }
                .addOnFailureListener {
                    completion?.invoke(false)
                }
        }
    }
}