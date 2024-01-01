package com.example.medijourney.modules.health_center.main.sub_ui.fitness_trackers

import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FitnessTrackersViewModel : ViewModel() {

    // Properties
    private var userFitnessTrackers: RealmResults<UserFitnessTracker>? = null
    var itemModels = MutableLiveData<MutableList<DynamicUIItem>>()

    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            getData()
            observeRealms()
        }
        itemModels.postValue(generateItemModels())
    }

    // Get data
    private suspend fun getData() {
        userFitnessTrackers = RealmManager.read(UserFitnessTracker::class.java)
    }

    // Observe data
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private suspend fun observeRealms() {
        val userFitnessTrackers = userFitnessTrackers ?: return

        userFitnessTrackers.asFlow()
            .debounce(500)
            .collect {
                this.userFitnessTrackers = it.list
                val models = generateItemModels()
                withContext(Dispatchers.Main) {
                    itemModels.postValue(models)
                }
            }
    }

    // Functions
    private fun generateItemModels(): MutableList<DynamicUIItem> {
        val list = mutableListOf<DynamicUIItem>()
        val userFitnessTrackers = userFitnessTrackers ?: return list
        val width = if (userFitnessTrackers.size > 1) getItemWidth() else null

        userFitnessTrackers.forEach { tracker ->
            if (!tracker.isValid()) return@forEach

            val item = DynamicUIItem(
                type = Constants.ITEM,
                groupIndex = 0,
                itemTag = "",
                data = tracker,
                padding = EdgePadding(0, 0, 0, 32),
                backgroundColor = R.color.white
            ).apply {
                image = if (tracker.imageName.isNullOrEmpty()) {
                    ImageStyle("ic_fitness_tracker")
                } else {
                    ImageStyle("", "images/${FirebaseAuthManager.getCurrentUserCode()}/${tracker.imageName}")
                }
                tracker.name?.let {
                    title = MTextStyle(it).apply {
                        font = R.font.proximanova_bold
                    }
                }
                tracker.model?.let {
                    description = MTextStyle(it)
                }
                tracker.version?.let {
                    secondaryDescription = MTextStyle(it)
                }
                additionalData = mapOf("width" to width)
            }

            list.add(item)
        }
        return list
    }

    private fun getItemWidth(): Int {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        return (screenWidth * 4) / 5
    }

    fun getUserFitnessTrackerId(model: BaseItemInterface?): String? {
        if (model == null) return null
        val userFitnessTrackers = model.data as? UserFitnessTracker ?: return null
        if (!userFitnessTrackers.isValid()) return null

        return userFitnessTrackers.id
    }
}