package com.example.medijourney.modules.authentication.sign_up

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FAManger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val faManger: FAManger
) : ViewModel() {

    // Properties
    val isLoading = MutableLiveData(false)
    val signUpState = MutableLiveData<AuthenticationResult>(null)
    val enableSignUpButton = MutableLiveData(false)
    val errorMessages = MutableLiveData<String?>(null)
    private var validTextViews: MutableMap<String, Boolean> = mutableMapOf(
        "email" to false,
        "password" to false,
        "confirmPassword" to false
    )

    // Functions
    fun checkEmailError(email: String, context: Context): String? {
        val isValid = email.matches(Constants.EMAIL_VALIDATOR_FORMAT.toRegex())
        this.validTextViews["email"] = isValid
        this.setEnableSignUpButtonValue(!this.validTextViews.values.contains(false))
        return if (isValid) null else context.getString(R.string.email_format_invalid)
    }

    fun checkPasswordError(password: String, context: Context): String? {
        val isValid = password.count() >= 6
        this.validTextViews["password"] = isValid
        this.setEnableSignUpButtonValue(!this.validTextViews.values.contains(false) )
        return  if (isValid) null else context.getString(R.string.password_length_short)
    }

    fun checkConfirmPasswordError(password: String, confirmPassword: String, context: Context): String? {
        val isValid = password == confirmPassword
        this.validTextViews["confirmPassword"] = isValid
        this.setEnableSignUpButtonValue(!this.validTextViews.values.contains(false))
        return if (isValid) null else context.getString(R.string.password_not_match)
    }

    private fun setEnableSignUpButtonValue(enable: Boolean) {
        this.enableSignUpButton.postValue(enable)
    }

    fun signUp(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = faManger.signUp(email, password, mapOf("full_name" to fullName))
            isLoading.postValue(false)
            signUpState.postValue(result)
        }
    }
}
