package com.example.medijourney.modules.base.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.addListener
import com.example.medijourney.common.managers.fire_store.observe
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RQuery
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.Advertisement
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.DoctorAppointment
import com.example.medijourney.common.models.realm_models.MedicalSpecialty
import com.example.medijourney.common.models.realm_models.MedicalSubSpecialty
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.models.realm_models.UserConversation
import com.example.medijourney.common.models.realm_models.UserMedicalProduct
import com.example.medijourney.common.models.realm_models.UserMedicalSpecialty
import com.example.medijourney.common.models.realm_models.UserMessage
import com.example.medijourney.common.models.realm_models.UserNotification
import com.example.medijourney.common.models.realm_models.UserSetting
import com.google.firebase.installations.FirebaseInstallations
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel: ViewModel() {

    // Properties
    var unreadNotificationCount = MutableLiveData(0)
    private var userSettingResult: RealmResults<UserSetting>? = null

    // Life cycle
    fun onCreate() {
        syncFireStore()
        viewModelScope.launch {
            getData()
            observeData()
        }
    }

    // FireStore
    private fun syncFireStore() {
        syncHighFSPriority()
        CoroutineScope(Dispatchers.IO).launch {
            delay(5000L)
            syncMediumFSPriority()
            delay(5000L)
            syncLowFSPriority()
        }
    }

    private fun syncHighFSPriority() {
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        FireStoreManager.buildDocRef(Pair(FireStoreCollection.USER_MEMBER, currentUserCode))
            .observe(User::class.java)
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_SETTINGS)
            .observe(UserSetting::class.java)
        FireStoreManager.buildCollectionRef(FireStoreCollection.ADVERTISEMENTS)
            .observe(Advertisement::class.java)
    }

    private fun syncMediumFSPriority() {
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_MEDICAL_PRODUCTS)
            .observe(UserMedicalProduct::class.java, this::class.java)
        FireStoreManager.buildCollectionRef(FireStoreCollection.DOCTORS_APPOINTMENTS)
            .whereEqualTo("patient_id", currentUserCode)
            .observe(DoctorAppointment::class.java, this::class.java)
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                syncUserRedundantData(task.result)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun syncUserRedundantData(fid: String) {
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_REDUNDANT_DATA)
            .whereEqualTo(Constants.FID, fid)
            .addListener {
                viewModelScope.launch {
                    it.documents.forEach { doc ->
                        val data = doc.data ?: return@forEach
                        val dataType = data["data_type"] as? String ?: return@forEach
                        val dataIds = data["data_ids"] as? List<String> ?: return@forEach

                        val clazz = when (dataType) {
                            UserNotification::class.simpleName -> {
                                UserNotification::class.java
                            }
                            UserMessage::class.simpleName -> {
                                UserMessage::class.java
                            }
                            Message::class.simpleName -> {
                                Message::class.java
                            }
                            UserConversation::class.simpleName -> {
                                UserConversation::class.java
                            }
                            Conversation::class.simpleName -> {
                                Conversation::class.java
                            }
                            else -> {
                                null
                            }
                        }
                        if (clazz != null) {
                            RealmManager.delete(
                                clazz = clazz,
                                realmQuery = RQuery.Where(UserNotification::id.name, Operator.IN, dataIds)
                            )
                            FireStoreManager.buildUserDocRef(Pair(FireStoreCollection.USER_REDUNDANT_DATA, doc.id)).delete()
                        }
                    }
                }
            }
    }

    private fun syncLowFSPriority() {
        FireStoreManager.buildCollectionRef(FireStoreCollection.MEDICAL_SPECIALTIES)
            .observe(MedicalSpecialty::class.java, this::class.java)
        FireStoreManager.buildCollectionRef(FireStoreCollection.MEDICAL_SUB_SPECIALTIES)
            .observe(MedicalSubSpecialty::class.java, this::class.java)
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_MEDICAL_SPECIALTIES)
            .observe(UserMedicalSpecialty::class.java, this::class.java)
    }

    // Get data
    private suspend fun getData() {
        getUserSettingResult()
    }

    private suspend fun getUserSettingResult() {
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        userSettingResult = RealmManager.read(UserSetting::class.java,
            realmQuery = RQuery.Where(UserSetting::userCode.name, Operator.EQUAL, currentUserCode))
    }

    // Observe data
    private suspend fun observeData() {
        observeUserSettingResult()
    }

    private suspend fun observeUserSettingResult() {
        val userSettingResult = userSettingResult ?: return

        userSettingResult.asFlow().collect {
            this.userSettingResult = it.list

            withContext(Dispatchers.Main) {
                unreadNotificationCount.postValue(getUnreadNotificationCount())
            }
        }
    }

    // Functions
    private fun getUnreadNotificationCount(): Int {
        val userSettingResult = userSettingResult ?: return 0
        val userSetting = userSettingResult.firstOrNull() ?: return 0
        if (!userSetting.isValid()) return 0

        return userSetting.unreadNotificationCount
    }

    fun updateUnreadNotificationCount(count: Int) {
        if (unreadNotificationCount.value == 0) return
        val userSettingResult = userSettingResult ?: return
        val userSetting = userSettingResult.firstOrNull() ?: return
        if (!userSetting.isValid()) return

        FireStoreManager.buildDocRef(
            Pair(FireStoreCollection.USER_MEMBER, userSetting.userCode),
            Pair(FireStoreCollection.USER_SETTINGS, userSetting.id)
        )
            .update("unread_notification_count", count)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.Main).launch {
                    unreadNotificationCount.postValue(count)
                }
            }
    }
}