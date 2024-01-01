package com.example.medijourney.common.ui_components.fragments.otp_view

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.TimeUnit

class OtpViewModel: ViewModel() {

    // Properties
    var enableResendButton = MutableLiveData<Boolean>(false)
    var enableConfirmButton = MutableLiveData<Boolean>(false)
    var oldValuesAtPosition: MutableMap<Int, String> = mutableMapOf()
    var otpString: String? = null

    // Functions
    fun generateOTPString(position: Int, digit: String) {
        oldValuesAtPosition[position] = digit
        val otpStringBuilder = StringBuilder()
        val sortedMap = oldValuesAtPosition.toSortedMap()
        sortedMap.values.forEach { value ->
            if (value.isNotEmpty()) {
                otpStringBuilder.append(value)
            }
        }
        otpString = otpStringBuilder.toString()
        validateOTPString()
    }

    private fun validateOTPString() {
        val isValid = otpString?.count() == 6
        enableConfirmButton.postValue(isValid)
        enableConfirmButton.value = isValid
    }

    @SuppressLint("DefaultLocale")
    fun generateTimerString(millisUntilFinished: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}