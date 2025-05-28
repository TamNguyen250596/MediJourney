package com.example.medijourney.modules.profile.advanced_security

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.DataStoreHelper
import com.example.medijourney.common.managers.bio_metric.CryptographyManager
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.UserSetting
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.isValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdvancedSecurityViewModel: ViewModel() {

    // Properties
    var isFingerprintAuthEnabled = MutableLiveData(false)
    var isOTPAuthEnabled = MutableLiveData(false)
    private var userSetting: UserSetting? = null

    // Life cycle
    fun onViewCreated() {
        viewModelScope.launch {
            getUserSetting()
            updateOTPSwitch()
        }
    }

    override fun onCleared() {
        super.onCleared()
        FireStoreManager.removeListeners(this::class.java)
    }

    // Functions
    private suspend fun getUserSetting() {
        val uid = FirebaseAuthManager.getCurrentUserCode() ?: return

        userSetting = RealmManager.read(UserSetting::class.java, uid)
        observeUserSetting()
    }

    // Observe Data
    private suspend fun observeUserSetting() {
        val userSetting = userSetting ?: return

        userSetting.asFlow().collect { changes ->
            this.userSetting = changes.obj
            updateOTPSwitch()
        }
    }

    // Finger Print
    fun checkFingerPrintAuthEnable(cryptographyManager: CryptographyManager, context: Context) {
        viewModelScope.launch {
            var result = false
            val currentUserEmail = FirebaseAuthManager.getCurrentFirebaseUser()?.email
            if (currentUserEmail != null) {
                result = cryptographyManager.checkKey(currentUserEmail) &&
                        DataStoreHelper.checkStringInList(context, Constants.biometricAuthUsers, currentUserEmail)
            }
            updateFingerPrintSwitch(result)
        }
    }

    fun updateFingerPrintSwitch(enable: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            isFingerprintAuthEnabled.postValue(enable)
        }
    }

    // OTP
    private fun updateOTPSwitch() {
        val userSetting = userSetting ?: return
        if (!userSetting.isValid()) return
        val enabledOTPAuth = userSetting.enableOTPAuth

        CoroutineScope(Dispatchers.Main).launch {
            isOTPAuthEnabled.postValue(enabledOTPAuth)
        }
    }

    fun checkPasswordError(password: String, context: Context): String? {
        val isValid = password.count() >= 6
        return if (isValid) null else context.getString(R.string.password_length_short)
    }

    fun disableOTPAuth(completion: (Boolean) -> Unit) {
        val userSetting = userSetting
        val uid = FirebaseAuthManager.getCurrentUserCode()
        if (uid == null || userSetting == null) return

        FireStoreManager.buildDocRef(
            Pair(FireStoreCollection.USER_MEMBER, uid),
            Pair(FireStoreCollection.USER_SETTINGS, userSetting.id)
        )
            .update(mapOf("enable_otp_auth" to false))
            .addOnCompleteListener {
                completion.invoke(it.isSuccessful)
            }
    }
}