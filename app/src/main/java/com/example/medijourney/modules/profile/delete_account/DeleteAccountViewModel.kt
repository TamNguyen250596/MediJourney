package com.example.medijourney.modules.profile.delete_account

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.helpers.MDataStore
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FAManger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val mDataStore: MDataStore,
    private val faManger: FAManger
) : ViewModel() {

    // Properties
    val isLoading = MutableLiveData(false)
    val authState = MutableLiveData<AuthenticationResult?>(null)

    // Functions
    fun deActiveUser(activity: Activity) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = faManger.deActiveUser()
            if (result == AuthenticationResult.DEACTIVE_ACCOUNT_SUCCESS) {
              logOut(activity)
            } else {
              authState.postValue(result)
            }
        }
    }

    fun deleteUser(activity: Activity) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = faManger.deleteUser()
            if (result == AuthenticationResult.DELETE_ACCOUNT_SUCCESS) {
                logOut(activity)
            } else {
                authState.postValue(result)
            }
        }
    }

    private suspend fun logOut(activity: Activity) {
        faManger.logOut(activity, mDataStore)
    }
}