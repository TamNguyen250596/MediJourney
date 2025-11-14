package com.example.medijourney.modules.health_center.add_fitness_tracker

import android.net.Uri
import android.view.Gravity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.SelectionItemModel
import com.example.medijourney.common.models.realm_models.FitnessTrackerActivity
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.FitnessTrackerActivityRepo
import com.example.medijourney.common.respositories.UserFitnessTrackerRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFitnessTrackerViewModel @Inject constructor(
    saveStateHandle: SavedStateHandle,
    fitnessTrackerActivityRepo: FitnessTrackerActivityRepo,
    private val userFitnessTrackerRepo: UserFitnessTrackerRepo
) : ViewModel() {

    // Properties
    var trackerImage = MutableLiveData<Uri?>()
    var trackerDeviceName = MutableLiveData<String?>()
    var itemModels = MutableLiveData<MutableList<SelectionItemModel>>()
    private val userFitnessTrackerId = saveStateHandle.get<String>("userFitnessTrackerId")
    private val fitnessTrackerActivitiesFlow = fitnessTrackerActivityRepo.getFitnessTrackerActivitiesFlow()
    private val userFitnessTrackerFlow = userFitnessTrackerRepo.getUserFitnessTrackerFlow(userFitnessTrackerId ?: "")
    private var params: MutableMap<String, Any> = mutableMapOf()
    private var trackerImageUri: Uri? = null

    // Life cycle
    init {
        viewModelScope.launch {
            handleUserFitnessTracker()
            observeData()
        }
    }

    // Functions
    private suspend fun handleUserFitnessTracker() {
        val userFitnessTracker = userFitnessTrackerFlow.first() ?: return
        if (!userFitnessTracker.isValid()) return

        trackerDeviceName.postValue(userFitnessTracker.deviceId)
        params["device_id"] = userFitnessTracker.deviceId

        userFitnessTracker.imageName?.let { imageName ->
            params["image_name"] = imageName
            downloadTrackerImage(imageName)
        }

        params["tracking_activities"] = userFitnessTracker.trackingActivities.toList()
    }

    private fun downloadTrackerImage(imageName: String) {
        val path = "images/${FirebaseAuthManager.getCurrentUserCode()}/$imageName"
        FirebaseStorageManager.downloadImage(path) { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                trackerImage.postValue(uri)
            }
        }
    }

    private suspend fun observeData() {
        fitnessTrackerActivitiesFlow
            .combine(userFitnessTrackerFlow) { fitnessTrackerActivities, userFitnessTracker ->
                Pair(fitnessTrackerActivities, userFitnessTracker)
            }
            .firstThenDebounce(500)
            .collect {
                val list = generateItemModels(it.first, it.second)
                itemModels.postValue(list)
            }
    }

    private fun generateItemModels(
        fitnessTrackerActivities: List<FitnessTrackerActivity>,
        userFitnessTracker: UserFitnessTracker?
    ): MutableList<SelectionItemModel> {
        val list = mutableListOf<SelectionItemModel>()

        fitnessTrackerActivities.forEach { tracker ->
            if (!tracker.isValid()) return@forEach

            var isSelected = false
            userFitnessTracker?.let {
                if (!it.isValid()) return@let
                isSelected = it.trackingActivities.contains(tracker.id)
            }

            val item = SelectionItemModel(
                data = tracker,
                title = MTextStyle(tracker.name ?: "").apply {
                    font = R.font.proximanova_bold
                },
                description = MTextStyle(tracker.description ?: ""),
                isClickable = true,
                isSelected = isSelected,
                additionalData = mapOf(
                    "gravity" to Gravity.TOP
                ),
                padding = EdgePadding(0,32, 0, 0)
            )
            list.add(item)
        }
        return list
    }

    @Suppress("UNCHECKED_CAST")
    fun handleTrackerActivities(model: BaseItemInterface?, isChecked: Boolean) {
        val trackerActivity = model?.data as? FitnessTrackerActivity ?: return
        val activities = params["tracking_activities"] as? MutableList<Int>

        activities?.let {
            if (it.contains(trackerActivity.id)) return

            if (isChecked) {
                it.add(trackerActivity.id)
            } else {
                it.remove(trackerActivity.id)
            }
            params["tracking_activities"] = it
        } ?: run {
            params["tracking_activities"] = mutableListOf(trackerActivity.id)
        }
    }

    fun handleToAddFitnessTracker(deviceId: String?, completion: (Boolean) -> Unit) {
        if (deviceId.isNullOrEmpty()) return completion.invoke(false)

        params["device_id"] = deviceId
        trackerImageUri?.let {
            val imageName = params["image_name"] as? String ?: return
            saveFitnessTrackerImage(it, imageName) { _ ->
                saveUserFitnessTracker { success ->
                    completion.invoke(success)
                }
            }
        } ?: run {
            saveUserFitnessTracker { success ->
                completion.invoke(success)
            }
        }
    }

    private fun saveFitnessTrackerImage(uri: Uri, imageName: String, completion: (Boolean) -> Unit) {
        FirebaseStorageManager.saveImage(uri, imageName) { url ->
            completion.invoke(url != null)
        }
    }

    private fun saveUserFitnessTracker(completion: (Boolean) -> Unit) {
        viewModelScope.launch {
            var result = false
            userFitnessTrackerId?.let {
                result = userFitnessTrackerRepo.updateUserFitnessTracker(it, params)
            } ?: run {
                result = userFitnessTrackerRepo.createUserFitnessTracker(params)
            }
            completion.invoke(result)
        }
    }
}