package com.example.medijourney.modules.authentication.phone_authentication

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.extensions.disposeBy
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserSetting
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.launch

class PhoneAuthenticationViewModel : ViewModel() {

    // Properties
    var errorMessages = MutableLiveData<String?>(null)
    var phoneNumber: String? = null
    var otp: String? = null
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

    fun enableOTPAuth(completion: (Boolean) -> Unit) {
        viewModelScope.launch {
            val uid = FirebaseAuthManager.getCurrentUserCode() ?: return@launch completion.invoke(false)
            val userSetting = RealmManager.read(UserSetting::class.java, uid) ?: return@launch completion.invoke(false)
            if (!userSetting.isValid()) return@launch completion.invoke(false)
            if (otp?.count() != 6) return@launch completion.invoke(false)

            FireStoreManager.buildDocRef(
                Pair(FireStoreCollection.USER_MEMBER, uid),
                Pair(FireStoreCollection.USER_SETTINGS, userSetting.id)
            )
                .update(mapOf("enable_otp_auth" to true))
                .addOnCompleteListener {
                    completion.invoke(it.isSuccessful)
                }
        }
    }

    fun requestAPIToGetTempLogInToken(completion: (String?) -> Unit) {
        completion.invoke("test")
    }
}