package com.example.medijourney.modules.profile.change_password

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medijourney.R
import com.example.medijourney.common.extensions.disposeBy
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import io.reactivex.rxjava3.disposables.CompositeDisposable

class ChangePasswordViewModel: ViewModel() {

    // Properties
    var enableConfirmButton = MutableLiveData<Boolean>(false)
    var errorMessages = MutableLiveData<String?>(null)
    var successMessages = MutableLiveData<String?>(null)
    private var validTextViews: MutableMap<String, Boolean> = mutableMapOf()
    private var currentPassword: String? = null
    private val disposables = CompositeDisposable()

    // Life cycle
    fun onViewCreated(view: View) {
        validTextViews = getValidTextViews()
        observeUserAuthenticationResult(view)
    }

    // Functions
    private fun observeUserAuthenticationResult(view: View) {
        FirebaseAuthManager.userAuthenticationResult
            .distinctUntilChanged()
            .subscribe {
                when (it) {
                    AuthenticationResult.CHANGE_PASSWORD_SUCCESS -> {
                        successMessages.postValue(view.context.getString(R.string.change_password_success_message))
                    }
                    AuthenticationResult.CHANGE_PASSWORD_FAILED -> {
                        errorMessages.postValue(view.context.getString(R.string.error_user_deactivation_failed))
                    }
                    else -> {}
                }
            }.disposeBy(disposables)
    }

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
        return FirebaseAuthManager.userAuthenticationResult.value == AuthenticationResult.SIGN_IN_SUCCESS
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
}