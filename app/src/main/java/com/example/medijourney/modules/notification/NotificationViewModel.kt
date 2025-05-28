package com.example.medijourney.modules.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.get
import com.example.medijourney.common.managers.fire_store.observe
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.UserMessage
import com.example.medijourney.common.models.realm_models.UserNotification
import com.example.medijourney.common.models.ui_models.EdgePadding
import com.example.medijourney.common.models.ui_models.ImageStyle
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotificationViewModel: ViewModel() {

    // Properties
    var models = MutableLiveData<MutableList<DynamicUIItem>>(mutableListOf())
    private var notificationResult: RealmResults<UserNotification>? = null
    private var currentMinCreatedDate: Long? = null
    private var currentObserverId: Int? = null

    // Life cycle
    fun onViewCreated() {
        observeLatestUserNotifications()
        viewModelScope.launch {
            getData()
            observeRealms()
        }
    }

    override fun onCleared() {
        super.onCleared()
        FireStoreManager.removeListeners(this::class.java)
        currentObserverId?.let {
            FireStoreManager.removeListener(it)
        }
    }

    // Functions
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

            currentObserverId?.let { currentObserverId ->
                FireStoreManager.removeListener(currentObserverId)
            }

            FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_NOTIFICATIONS)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .whereLessThan("created_at", createdAt)
                .limit(100)
                .get(UserNotification::class.java) { snapshot ->
                    setCurrentMinCreatedDate(snapshot)
                }

            viewModelScope.launch {
                delay(15000L)
                val ref =
                    FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_NOTIFICATIONS)
                currentObserverId = ref.hashCode()
                ref.orderBy("created_at", Query.Direction.DESCENDING)
                    .whereLessThan("created_at", createdAt)
                    .limit(110)
                    .observe(UserNotification::class.java)
            }
        }
    }

    private fun observeLatestUserNotifications() {
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_NOTIFICATIONS)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(100)
            .observe(UserNotification::class.java, this::class.java) {
                if (currentMinCreatedDate == null) {
                    setCurrentMinCreatedDate(it)
                }
            }
    }

    private fun setCurrentMinCreatedDate(snapshot: QuerySnapshot?) {
        val documents = snapshot?.documents ?: return
        currentMinCreatedDate = documents.lastOrNull()?.data?.get("created_at") as? Long
    }

    private suspend fun getData() {
        notificationResult = RealmManager.read(UserNotification::class.java,
            sort = listOf(UserNotification::createdAt.name to Sort.DESCENDING))
    }

    private suspend fun observeRealms() {
        val notificationResult = notificationResult ?: return

        notificationResult.asFlow().collect {
            this.notificationResult = it.list

            val modelList = getNotificationData()
            models.postValue(modelList)
        }
    }

    private fun getNotificationData(): MutableList<DynamicUIItem> {
        val list = mutableListOf<DynamicUIItem>()
        val notificationResult = notificationResult ?: return list

        notificationResult.forEach {
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
        val notificationResult = notificationResult ?: return callback.invoke()

        notificationResult.forEachIndexed { index, userNotification ->
            if (!userNotification.isValid()) return@forEachIndexed

            FireStoreManager.buildDocRef(
                Pair(FireStoreCollection.USER_MEMBER, userNotification.userCode),
                Pair(FireStoreCollection.USER_NOTIFICATIONS, userNotification.id)
            )
                .delete()
                .addOnCompleteListener {
                    if (index == notificationResult.size - 1) {
                        viewModelScope.launch {
                            RealmManager.delete(UserNotification::class.java)
                            callback.invoke()
                        }
                    }
                }
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(5 * 60 * 1000L)
            callback.invoke()
        }
    }

    fun deleteNotification(position: Int, callback: () -> Unit) {
        val notificationResult = notificationResult ?: return callback.invoke()
        if (position > notificationResult.size - 1) return callback.invoke()
        val item = notificationResult[position]
        if (!item.isValid()) return callback.invoke()
        val userCode = FirebaseAuthManager.getCurrentUserCode() ?: return callback.invoke()
        val id = item.id

        FireStoreManager.buildDocRef(
            Pair(FireStoreCollection.USER_MEMBER, userCode),
            Pair(FireStoreCollection.USER_NOTIFICATIONS, item.id)
        )
            .delete()
            .addOnCompleteListener {
                viewModelScope.launch {
                    RealmManager.delete(UserNotification::class.java, id)
                    callback.invoke()
                }
            }
    }

    fun readNotification(model: BaseItemInterface?) {
        val notification = model?.data as? UserNotification ?: return
        if (!notification.isValid()) return
        val userCode = FirebaseAuthManager.getCurrentUserCode() ?: return
        val notificationId = notification.id

        FireStoreManager.buildDocRef(
            Pair(FireStoreCollection.USER_MEMBER, userCode),
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
        if (userNotification.relatedObjectType != UserMessage::class.simpleName) return null
        return userNotification.relatedObjectId
    }
}