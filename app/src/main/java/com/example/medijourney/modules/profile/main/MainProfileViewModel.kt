package com.example.medijourney.modules.profile.main

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.fire_store.observe
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.managers.realm.where
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Membership
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.models.realm_models.UserMedicalSpecialty
import com.example.medijourney.common.models.realm_models.UserMembership
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.notifications.UpdatedObject
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class MainProfileViewModel: ViewModel() {

    // Properties
    var bgImageUri = MutableLiveData<Uri?>(null)
    var avatarImageUri = MutableLiveData<Uri?>(null)
    var userName = MutableLiveData<String?>(null)
    var userMembershipInfo = MutableLiveData<Pair<String, String>?>(null)
    var itemModels = MutableLiveData<MutableList<DynamicUIItem>>(mutableListOf())
    private var currentUser: User? = null
    private var membershipResult: RealmResults<Membership>? = null
    private var userMembershipResult: RealmResults<UserMembership>? = null
    private var currentAppMainProfile: Map<String, Any> = mutableMapOf()

    // Life cycle
    init {
        currentAppMainProfile = InternationManager.getCurrentAppMainProfile()
        syncFireStore()
        viewModelScope.launch {
            getData()
            observeData()
        }
        handleBackgroundImage()
        handleAvatarImage()

        val modelList = generateCellViewModels()
        itemModels.postValue(modelList)
    }

    override fun onCleared() {
        super.onCleared()
        FireStoreManager.removeListeners(this::class.java)
    }

    // Get Data
    private suspend fun getData() {
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        currentUser = RealmManager.read(User::class.java, currentUserCode)
        userMembershipResult = RealmManager.read(UserMembership::class.java,
            realmQuery = where(UserMedicalSpecialty::userId.name, Operator.EQUAL, currentUserCode)
        )
        membershipResult = RealmManager.read(Membership::class.java)
    }

    // Observe Data
    private fun syncFireStore() {
        FireStoreManager.buildCollection(FireStoreCollection.MEMBERSHIPS)
            .observe(Membership::class.java, this::class.java)
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_MEMBERSHIP)
            .observe(UserMembership::class.java, this::class.java)
    }

    private suspend fun observeData() = supervisorScope {
        launch { observeCurrentUser() }
        launch { observeUserMembership() }
    }

    private suspend fun observeCurrentUser() {
        val currentUser = currentUser ?: return

        currentUser.asFlow(listOf(User::displayName.name)).collect {
            this.currentUser = it.obj
            when (it) {
                is UpdatedObject -> {
                    when (true) {
                        it.changedFields.contains(User::backgroundUrl.name) -> {
                            handleBackgroundImage()
                        }
                        it.changedFields.contains(User::avatarUrl.name) -> {
                            handleAvatarImage()
                        }
                        it.changedFields.contains(User::displayName.name) -> {
                            handleUserName()
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private suspend fun observeUserMembership() {
        val membershipResult = membershipResult ?: return
        val userMembershipResult = userMembershipResult ?: return

        combine(
            membershipResult.asFlow(),
            userMembershipResult.asFlow(),
        ) { membershipResult, userMembershipResult ->
            this.membershipResult = membershipResult.list
            this.userMembershipResult = userMembershipResult.list
        }
            .debounce(500)
            .collectLatest {
                val modelList = generateUserMembershipInfo()
                withContext(Dispatchers.Main) {
                    userMembershipInfo.postValue(modelList)
                }
            }
    }

    // Functions
    private fun handleBackgroundImage() {
        FirebaseStorageManager.downloadUserImage("background", "jpg") { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                bgImageUri.postValue(uri)
            }
        }
    }

    private fun handleAvatarImage() {
        FirebaseStorageManager.downloadUserImage("avatar", "jpg") { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                avatarImageUri.postValue(uri)
            }
        }
    }

    private fun handleUserName() {
        val currentUser = currentUser ?: return
        CoroutineScope(Dispatchers.Main).launch {
            userName.postValue(currentUser.takeIf { it.isValid() }?.displayName)
        }
    }

    private fun generateUserMembershipInfo(): Pair<String, String>? {
        val userMembershipResult = userMembershipResult ?: return null
        val membershipResult = membershipResult ?: return null
        val userMembership = userMembershipResult.firstOrNull() ?: return null
        if (!userMembership.isValid()) return null

        val membership = membershipResult.firstOrNull { it.id == userMembership.membershipId } ?: return null
        if (!membership.isValid()) return null

        val imageName = membership.imageName ?: ""
        val name = membership.name ?: ""
        return Pair(imageName, name)
    }

    @Suppress("UNCHECKED_CAST")
    private fun generateCellViewModels(): MutableList<DynamicUIItem> {
        val attributes = currentAppMainProfile["attributes"] as? List<Map<String, Any>> ?: return mutableListOf()

        return attributes.mapIndexed { index, map ->
            DynamicUIItem.fromMap(map).apply {
                title?.font = R.font.proximanova_bold
                if (index == attributes.size - 1) {
                    additionalData = mapOf(HDualImageTextViewHolder.IS_HIDE_SEPARATOR_KEY to true)
                }
            }
        }.toMutableList()
    }
}