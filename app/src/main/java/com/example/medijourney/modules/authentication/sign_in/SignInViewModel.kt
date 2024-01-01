package com.example.medijourney.modules.authentication.sign_in

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.disposeBy
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import io.reactivex.rxjava3.disposables.CompositeDisposable

class SignInViewModel(): ViewModel() {

    // Properties
    var enableSignInButton = MutableLiveData<Boolean>(false)
    var errorMessages = MutableLiveData<String?>(null)
    private var validTextViews: MutableMap<String, Boolean> = mutableMapOf(
        "email" to false,
        "password" to false
    )
    private val disposables = CompositeDisposable()

    // Life cycle
    fun onViewCreated(view: View) {
        observeUserAuthenticationResult(view)
    }

    // Functions
    private fun observeUserAuthenticationResult(view: View) {
        FirebaseAuthManager.userAuthenticationResult
            .distinctUntilChanged()
            .subscribe {
                when (it) {
                    AuthenticationResult.SIGN_IN_FAILED -> {
                        errorMessages.postValue(view.context.getString(R.string.failed_authentication_error_message))
                    }
                    else -> {}
                }
            }.disposeBy(disposables)
    }

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
}