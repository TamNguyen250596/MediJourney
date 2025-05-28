package com.example.medijourney.common.ui_components.fragments.user_avatar_section

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserAvatarSectionViewModel: ViewModel() {

    // Properties
    var bgImageUri = MutableLiveData<Uri?>()
    var avatarImageUri = MutableLiveData<Uri?>()
    var userNameText = MutableLiveData<String?>()
    var membershipInfo = MutableLiveData<Pair<String, String>?>()
}