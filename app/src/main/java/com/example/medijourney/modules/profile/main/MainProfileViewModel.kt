package com.example.medijourney.modules.profile.main

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.extensions.firstThenDebounce
import com.example.medijourney.common.helpers.MDataStore
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.Membership
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.models.realm_models.UserMembership
import com.example.medijourney.common.respositories.MembershipRepo
import com.example.medijourney.common.respositories.UserMembershipRepo
import com.example.medijourney.common.respositories.UserRepo
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class MainProfileViewModel @Inject constructor(
    private val mDataStore: MDataStore,
    private val faManger: FAManger,
    userRepository: UserRepo,
    private val userMembershipRepository: UserMembershipRepo,
    private val membershipRepository: MembershipRepo
) : ViewModel() {

    // Properties
    val isLoading = MutableLiveData(false)
    var bgImageUri = MutableLiveData<Uri?>(null)
    var avatarImageUri = MutableLiveData<Uri?>(null)
    var userName = MutableLiveData<String?>(null)
    var userMembershipInfo = MutableLiveData<Pair<String, String>?>(null)
    var itemModels = MutableLiveData<MutableList<DynamicUIItem>>(mutableListOf())
    private var currentUserFlow = userRepository.getUserFlow(FAManger.currentUserCode)
    private var membershipsFlow = membershipRepository.getMembershipsFlow()
    private var userMembershipFlow = userMembershipRepository.getUserMembershipFow()
    private var currentAppMainProfile: Map<String, Any> = mutableMapOf()

    // Life cycle
    init {
        currentAppMainProfile = InternationManager.getCurrentAppMainProfile()
        viewModelScope.launch {
            observeData()
        }
        handleBackgroundImage()
        handleAvatarImage()

        val modelList = generateCellViewModels()
        itemModels.postValue(modelList)
    }

    // Get Data
    // Observe Data
    private suspend fun observeData() = supervisorScope {
        launch {
            membershipRepository.observeMemberships()
        }
        launch {
            userMembershipRepository.observeUserMembership()
        }
        launch {
            currentUserFlow
                .firstThenDebounce(500)
                .collect {
                    handleBackgroundImage()
                    handleAvatarImage()
                    handleUserName(it)
                }
        }
        launch {
            membershipsFlow
                .combine(userMembershipFlow) { memberships, userMembership ->
                    Pair(memberships, userMembership)
                }
                .firstThenDebounce(500)
                .collect {
                    val model = generateUserMembershipInfo(it.second, it.first)
                    userMembershipInfo.postValue(model)
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

    private fun handleUserName(user: User?) {
        if (user == null) return
        if (!user.isValid()) return
        userName.postValue(user.displayName)
    }

    private fun generateUserMembershipInfo(userMembership: UserMembership?, memberships: List<Membership>?): Pair<String, String>? {
        if (userMembership == null) return null
        if (userMembership.isValid()) return null
        if (memberships == null) return null

        val membership = memberships.firstOrNull { it.id == userMembership.membershipId } ?: return null
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

    fun logOut(activity: Activity) {
        viewModelScope.launch {
            isLoading.postValue(true)
            faManger.logOut(activity, mDataStore)
            isLoading.postValue(false)
        }
    }
}