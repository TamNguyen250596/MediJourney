package com.example.medijourney.modules.base.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.respositories.AdvertisementRepo
import com.example.medijourney.common.respositories.UserSettingRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    advertisementRepository: AdvertisementRepo,
    userSettingRepository: UserSettingRepo,
    mainActivityListener: MainActivityListener
): ViewModel() {

    // Properties
    var splashAdInfo = MutableLiveData<Pair<String, String?>>()
    var unreadNotificationCount = MutableLiveData(0)
    private val advertisementsFlow = advertisementRepository.getAdvertisements(listOf("splash_ad"))
    private val userSettingsFlow = userSettingRepository.getUserSettings()
    private var userSettingId: String? = null

    // Life cycle
    init {
        mainActivityListener.observe(viewModelScope)
        observeFlows()
    }

    // Observe data
    private fun observeFlows() {
        viewModelScope.launch {
            observeUserSettingsFlow()
        }
        viewModelScope.launch {
            observeAdvertisementsFlow()
        }
    }

    private suspend fun observeUserSettingsFlow() {
        userSettingsFlow
            .collect {
                val userSettingResult = it.firstOrNull() ?: return@collect
                if (!userSettingResult.isValid()) return@collect

                userSettingId = userSettingResult.id
                unreadNotificationCount.postValue(userSettingResult.unreadNotificationCount)
            }
    }

    private suspend fun observeAdvertisementsFlow() {
        advertisementsFlow
            .conflate()
            .onEach { delay(2000) }
            .collect {
                val ad = it.firstOrNull() ?: return@collect
                if (!ad.isValid()) return@collect
                val adInfo = Pair(
                    "images/advertisements/${ad.imageName}.png",
                    ad.actionUrl
                )

                splashAdInfo.postValue(adInfo)
            }
    }

    // Functions
    fun updateUnreadNotificationCount(count: Int) {
        if (unreadNotificationCount.value == 0) return
        val userSettingId = userSettingId ?: return

        viewModelScope.launch {
            val result = FireStoreManager.updateDoc(
                FireStoreCollection.USER_SETTINGS,
                userSettingId,
                mapOf("unread_notification_count" to count)
            )
            if (result) {
                unreadNotificationCount.postValue(count)
            }
        }
    }
}