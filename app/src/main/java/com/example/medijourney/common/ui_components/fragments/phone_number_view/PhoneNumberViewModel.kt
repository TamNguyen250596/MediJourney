package com.example.medijourney.common.ui_components.fragments.phone_number_view

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants

class PhoneNumberViewModel : ViewModel() {

    // Properties
    var enableSendButton = MutableLiveData(false)

    // Functions
    fun validatePhoneNumber(phoneNumber: String, context: Context): String? {
        val isValid = phoneNumber.matches(Constants.PHONE_NUMBER_VALIDATOR_FORMAT.toRegex())
        setEnableSendButton(isValid)
        return if (isValid) null else context.getString(R.string.phone_number_format_invalid)
    }

    private fun setEnableSendButton(enable: Boolean) {
        this.enableSendButton.postValue(enable)
        this.enableSendButton.value = enable
    }
}