package com.example.medijourney.modules.profile.change_password

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FAManger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val faManger: FAManger,
) : ViewModel() {

    // Properties
    val isLoading = MutableLiveData(false)
    val changePasswordState = MutableLiveData<AuthenticationResult?>(null)
    val enableConfirmButton = MutableLiveData(false)
    private var validTextViews: MutableMap<String, Boolean> = mutableMapOf()
    private var currentPassword: String? = null

    // Life cycle
    init {
        validTextViews = getValidTextViews()
    }

    // Functions
    private fun getValidTextViews(): MutableMap<String, Boolean> {
        val map = mutableMapOf(
            "newPassword" to false,
            "confirmNewPassword" to false
        )
        if (checkUserLoggedIn()) {
            map["currentPassword"] = false
        }
        return map
    }

    fun checkUserLoggedIn(): Boolean {
        return FAManger.currentUser != null
    }

    fun checkCurrentPasswordError(password: String, context: Context): String? {
        val isValid = password.count() >= 6
        validTextViews["currentPassword"] = isValid
        currentPassword = password
        setEnableSignUpButtonValue(!validTextViews.values.contains(false))
        return if (isValid) null else context.getString(R.string.password_length_short)
    }

    fun checkNewPasswordError(password: String, context: Context): String? {
        var errorMessage: String? = null

        if (password.count() < 6) {
            errorMessage = context.getString(R.string.password_length_short)
        } else if (password == currentPassword) {
            errorMessage = context.getString(R.string.new_password_same_current_password)
        }

        val isValid = errorMessage == null
        validTextViews["newPassword"] = isValid
        setEnableSignUpButtonValue(!validTextViews.values.contains(false) )
        return errorMessage
    }

    fun checkConfirmPasswordError(password: String, confirmPassword: String, context: Context): String? {
        val isValid = password == confirmPassword
        validTextViews["confirmNewPassword"] = isValid
        setEnableSignUpButtonValue(!validTextViews.values.contains(false))
        return if (isValid) null else context.getString(R.string.password_not_match)
    }

    private fun setEnableSignUpButtonValue(enable: Boolean) {
        this.enableConfirmButton.postValue(enable)
        this.enableConfirmButton.value = enable
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            if (checkUserLoggedIn()) {
                faManger.changePasswordWhenLogIn(newPassword)
            } else {
                val tempToken = state.get<String>("tempLogInToken") ?: return@launch
                faManger.changePasswordWithoutLogIn(tempToken, newPassword)
            }
            isLoading.postValue(false)
        }
    }
}