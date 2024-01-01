package com.example.medijourney.modules.health_center.add_fitness_tracker

import android.net.Uri
import android.view.Gravity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.SelectionItemModel
import com.example.medijourney.common.models.realm_models.FitnessTrackerActivity
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class AddFitnessTrackerViewModel : ViewModel() {

    // Properties
    var trackerImage = MutableLiveData<Uri?>()
    var trackerDeviceName = MutableLiveData<String?>()
    var itemModels = MutableLiveData<MutableList<SelectionItemModel>>()
    private var fitnessTrackerActivities: RealmResults<FitnessTrackerActivity>? = null
    private var params: MutableMap<String, Any> = mutableMapOf()
    private var trackerImageUri: Uri? = null
    private var userFitnessTracker: UserFitnessTracker? = null
    private var userFitnessTrackerId: String? = null

    // Life cycle
    fun onViewCreated(userFitnessTrackerId: String?) {
        viewModelScope.launch {
            this@AddFitnessTrackerViewModel.userFitnessTrackerId = userFitnessTrackerId
            getData()
            handleUserFitnessTracker()
            val models = generateItemModels()
            itemModels.postValue(models)

            launch {
                observeData()
            }
        }
    }

    // Functions
    private fun handleUserFitnessTracker() {
        val userFitnessTracker = this.userFitnessTracker ?: return
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

    private suspend fun getData() {
        userFitnessTrackerId?.let {
            userFitnessTracker = RealmManager.read(UserFitnessTracker::class.java, it)
        }
        fitnessTrackerActivities = RealmManager.read(FitnessTrackerActivity::class.java)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private suspend fun observeData() {
        val fitnessTrackerActivities = fitnessTrackerActivities ?: return

        fitnessTrackerActivities.asFlow()
            .debounce(500)
            .collect {
                this.fitnessTrackerActivities = it.list
                val list = generateItemModels()
                itemModels.postValue(list)
        }
    }

    private fun generateItemModels(): MutableList<SelectionItemModel> {
        val list = mutableListOf<SelectionItemModel>()
        val fitnessTrackerActivities = fitnessTrackerActivities ?: return list

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
        userFitnessTrackerId?.let {
            FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_FITNESS_TRACKERS, it))
                .update(params)
                .addOnCompleteListener {
                    completion.invoke(it.isSuccessful)
                }
        } ?: run {
            val docRef = FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_FITNESS_TRACKERS, null))
            params["id"] = docRef.id
            FirebaseAuthManager.getCurrentUserCode()?.let {
                params["user_code"] = it
            }

            docRef.set(params).addOnCompleteListener {
                completion.invoke(it.isSuccessful)
            }
        }
    }
}