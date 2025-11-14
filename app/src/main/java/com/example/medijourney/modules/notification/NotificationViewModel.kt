package com.example.medijourney.modules.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.UserNotification
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.respositories.UserNotificationRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val userNotificationRepository: UserNotificationRepo
) : ViewModel() {

    // Properties
    val models = MutableLiveData<MutableList<DynamicUIItem>>(mutableListOf())
    private val notificationsFlow = userNotificationRepository.getUserNotificationsFlow()
    private var currentMinCreatedDate: Long? = null
    private var currentObserverJob: Job? = null

    // Life cycle
    init {
        viewModelScope.launch {
            launch {
                observeData()
            }
            launch {
                observeLatestUserNotifications()
            }
        }
    }

    private suspend fun observeData() {
        notificationsFlow
            .firstThenDebounce(1000L)
            .collect {
            val modelList = getNotificationData(it)
            models.postValue(modelList)
        }
    }

    // Functions
    private suspend fun observeLatestUserNotifications() {
        userNotificationRepository
            .observeLatestNotifications()
            .collect {
                if (currentMinCreatedDate == null) {
                    setCurrentMinCreatedDate(it)
                }
            }
    }

    fun observeUserNotifications(position: Int) {
        val value = models.value ?: return
        if (position > value.size - 1) return
        val item = value[position]
        val notification = item.data as? UserNotification ?: return
        if (!notification.isValid()) return
        val createdAt = notification.createdAt ?: return
        val millis = DateHelper.convertRealmInstantToMillis(createdAt)

        currentMinCreatedDate?.let {
            if (millis < it) return

            if (currentObserverJob != null) {
                currentObserverJob?.cancel()
                currentObserverJob = null
            }

            currentObserverJob = viewModelScope.launch {
                val list = userNotificationRepository.getOlderNotifications(millis)
                setCurrentMinCreatedDate(list)

                delay(15000L)
                userNotificationRepository
                    .observeOlderNotifications(millis)
                    .collect { list ->
                        setCurrentMinCreatedDate(list)
                    }
            }
        }
    }

    private fun setCurrentMinCreatedDate(dataList: List<Map<String, Any>>) {
        currentMinCreatedDate = dataList.lastOrNull()?.get("created_at") as? Long
    }

    private fun getNotificationData(dataList: List<UserNotification>): MutableList<DynamicUIItem> {
        val list = mutableListOf<DynamicUIItem>()

        dataList.forEach {
            if (!it.isValid()) return@forEach

            val item = DynamicUIItem(
                type = Constants.ITEM,
                groupIndex = 0,
                itemTag = "",
                data = it,
                padding = EdgePadding(32, 32, 32, 32),
                backgroundColor = if (it.isRead) R.color.white else R.color.light_blue_color)
                .apply {
                image = ImageStyle(it.getIconName(), it.imageURL)
            }
            it.title?.let { title ->
                item.title = MTextStyle(title).apply {
                    font = R.font.proximanova_bold
                }
            }
            it.description?.let { description ->
                item.description = MTextStyle(description)
            }
            it.createdAt?.let { createdAt ->
                val dateString = DateHelper.convertRealmInstantToString(createdAt, "dd/MM/yyyy")
                item.secondaryDescription = MTextStyle(dateString).apply {
                    size = 12f
                }
            }
            list.add(item)
        }
        return list
    }

    fun deleteAllNotifications(callback: () -> Unit) {
        viewModelScope.launch {
            userNotificationRepository.deleteAllNotifications()
            callback.invoke()
        }
    }

    fun deleteNotification(position: Int, callback: () -> Unit) {
        val models = models.value ?: return callback.invoke()
        val item = models[position]
        val notification = item.data as? UserNotification ?: return callback.invoke()
        if (!notification.isValid()) return callback.invoke()
        val id = notification.id

        viewModelScope.launch {
            userNotificationRepository.deleteNotification(id)
            callback.invoke()

        }
    }

    fun readNotification(model: BaseItemInterface?) {
        val notification = model?.data as? UserNotification ?: return
        if (!notification.isValid()) return
        val userCode = FirebaseAuthManager.getCurrentUserCode() ?: return
        val notificationId = notification.id

        FireStoreManager.buildDoc(
            Pair(FireStoreCollection.USER_MEMBERS, userCode),
            Pair(FireStoreCollection.USER_NOTIFICATIONS, notification.id)
        )
            .update("is_read", true)
            .addOnCompleteListener {
                viewModelScope.launch {
                    RealmManager.update(UserNotification::class.java, notificationId, mapOf("is_read" to true))
                }
            }

    }

    fun getUserMessageId(userNotification: UserNotification): String? {
        if (!userNotification.isValid()) return null
        if (userNotification.relatedObjectType != Message::class.simpleName) return null
        return userNotification.relatedObjectId
    }
}