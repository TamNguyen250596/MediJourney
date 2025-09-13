package com.example.medijourney.modules.authentication.sign_in

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FAManger
import com.facebook.CallbackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val faManger: FAManger
) : ViewModel() {

    // Properties
    val signInState = MutableLiveData<AuthenticationResult>(null)
    val isLoading = MutableLiveData(false)
    val enableSignInButton = MutableLiveData<Boolean>(false)
    private var validTextViews: MutableMap<String, Boolean> = mutableMapOf(
        "email" to false,
        "password" to false
    )

    // Functions
    fun checkEmailError(email: String, context: Context): String? {
        val isValid = email.matches(Constants.EMAIL_VALIDATOR_FORMAT.toRegex())
        this.validTextViews["email"] = isValid
        this.setEnableSignInButtonValue(!this.validTextViews.values.contains(false))
        return if (isValid) null else context.getString(R.string.email_format_invalid)
    }

    fun checkPasswordError(password: String, context: Context): String? {
        val isValid = password.count() >= 6
        this.validTextViews["password"] = isValid
        this.setEnableSignInButtonValue(!this.validTextViews.values.contains(false) )
        return  if (isValid) null else context.getString(R.string.password_length_short)
    }

    private fun setEnableSignInButtonValue(enable: Boolean) {
        this.enableSignInButton.postValue(enable)
        this.enableSignInButton.value = enable
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = faManger.signIn(email, password)
            isLoading.postValue(false)
            signInState.postValue(result)
        }
    }

    fun fbSignIn(activity: Activity, callbackManager: CallbackManager) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = faManger.signInByFacebook(activity, callbackManager)
            isLoading.postValue(false)
            signInState.postValue(result)
        }
    }

    fun googleSignIn(activity: Activity) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = faManger.signInByGoogle(activity)
            isLoading.postValue(false)
            signInState.postValue(result)
        }
    }
}