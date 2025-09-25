package com.example.medijourney.modules.base.auth

import androidx.lifecycle.ViewModel
import com.example.medijourney.common.managers.firebase_auth.FAManger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthActivityViewModel @Inject constructor() : ViewModel()  {

    fun checkUserLoggedIn(): Boolean {
        return FAManger.currentUser != null
    }
}