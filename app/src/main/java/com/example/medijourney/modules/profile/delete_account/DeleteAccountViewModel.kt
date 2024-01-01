package com.example.medijourney.modules.profile.delete_account

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medijourney.R
import com.example.medijourney.common.extensions.disposeBy
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import io.reactivex.rxjava3.disposables.CompositeDisposable

class DeleteAccountViewModel: ViewModel() {

    // Properties
    var shouldLogout = MutableLiveData<Boolean>(false)
    var errorMessages = MutableLiveData<String?>(null)
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
                    AuthenticationResult.DEACTIVE_ACCOUNT_FAILED -> {
                        errorMessages.postValue(view.context.getString(R.string.error_user_deactivation_failed))
                    }
                    AuthenticationResult.DELETE_ACCOUNT_FAILED -> {
                        errorMessages.postValue(view.context.getString(R.string.error_user_deletion_failed))
                    }
                    AuthenticationResult.DELETE_ACCOUNT_SUCCESS, AuthenticationResult.DEACTIVE_ACCOUNT_SUCCESS -> {
                        shouldLogout.postValue(true)
                    }
                    else -> {}
                }
            }.disposeBy(disposables)
    }

    fun deActiveUser(context: Context) {
        FirebaseAuthManager.deActiveUser(context)
    }

    fun deleteUser(activity: Activity) {
        FirebaseAuthManager.deleteUser(activity)
    }
}