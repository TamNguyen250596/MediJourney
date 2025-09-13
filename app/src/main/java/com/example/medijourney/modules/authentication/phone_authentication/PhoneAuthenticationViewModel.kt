package com.example.medijourney.modules.authentication.phone_authentication

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneAuthenticationViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val faManger: FAManger
) : ViewModel() {

    // Properties
    val isLoading = MutableLiveData(false)
    val signInState = MutableLiveData<AuthenticationResult>(null)
    private var phoneNumber: String? = null
    private var otp: String? = null

    // Functions
    fun updatePhoneNumber(value: String?) {
        phoneNumber = value
    }

    fun updateOTP(value: String?) {
        isLoading.postValue(true)
        otp = value
    }

    fun handleViewType(activity: Activity) {
        val viewType = state.get<String>("viewType") ?: return
        when (viewType) {
            Constants.FORGOT_PASSWORD -> {
                requestAPIToGetTempLogInToken()
            }
            Constants.ENABLE_OTP_AUTH -> {
                viewModelScope.launch {
                    enableOTPAuth()
                    isLoading.postValue(false)
                }
            }
            Constants.SIGN_IN_BY_PHONE_NUMBER -> {
                val phoneNumber = phoneNumber ?: return

                viewModelScope.launch {
                    faManger.signInByPhoneNumber(phoneNumber, activity)
                    isLoading.postValue(false)
                    signInState.postValue(AuthenticationResult.SIGN_IN_SUCCESS)
                }
            }
        }
    }

    suspend fun enableOTPAuth() {
        val uid = FirebaseAuthManager.getCurrentUserCode() ?: return
        val userSetting = RealmManager.read(UserSetting::class.java, uid) ?: return
        if (!userSetting.isValid()) return
        if (otp?.count() != 6) return

        FireStoreManager.updateDoc(
            FireStoreCollection.USER_SETTINGS,
            userSetting.id,
            mapOf("enable_otp_auth" to true)
        )
    }

    private fun requestAPIToGetTempLogInToken() {}
}