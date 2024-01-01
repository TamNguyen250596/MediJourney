package com.example.medijourney.common.managers.bio_metric

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.medijourney.R
import kotlin.reflect.KFunction1

object BiometricPromptUtils {

    // Properties
    private const val TAG = "BiometricPromptUtils"

    // Functions
    fun createBiometricPrompt(
        activity: FragmentActivity,
        processSuccess: (BiometricPrompt.AuthenticationResult) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)

            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                processSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    fun createPromptInfo(activity: FragmentActivity): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(activity.getString(R.string.medi_journey))
            setSubtitle(activity.getString(R.string.log_in_using_biometric_credential))
            setDescription(activity.getString(R.string.log_in_biometric_credential_description))
            setConfirmationRequired(false)
            setNegativeButtonText(activity.getString(R.string.use_app_password))
        }.build()
}