package com.example.medijourney.modules.profile.advanced_security

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.Constants.CIPHERTEXT_WRAPPER
import com.example.medijourney.common.constants.Constants.SHARED_PREFS_FILENAME
import com.example.medijourney.common.helpers.DataStoreHelper
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.bio_metric.BiometricPromptUtils
import com.example.medijourney.common.managers.bio_metric.CryptographyManager
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.databinding.FragmentAdvancedSecurityBinding
import kotlinx.coroutines.launch

class AdvancedSecurityFragment : Fragment() {

    // Properties
    private lateinit var binding: FragmentAdvancedSecurityBinding
    private val viewModel: AdvancedSecurityViewModel by viewModels()
    private val cryptographyManager = CryptographyManager()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdvancedSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated()
        setupView()
        observeViewModel()
        observeUIComponents()
    }

    // Functions
    private fun setupView() {
        viewModel.checkFingerPrintAuthEnable(cryptographyManager, requireContext())
    }

    private fun observeViewModel() {
        viewModel.isFingerprintAuthEnabled.observe(viewLifecycleOwner) {
            binding.fingerPrintSwitch.isChecked = it
            binding.separatorView.visibility = if (it) View.GONE else View.VISIBLE
            binding.passwordTextInputLayout.visibility = if (it) View.GONE else View.VISIBLE
        }
        viewModel.isOTPAuthEnabled.observe(viewLifecycleOwner) {
            binding.otpSwitch.isChecked = it
        }
    }

    private fun observeUIComponents() {
        binding.fingerPrintSwitch.setOnCheckedChangeListener { switch, isChecked ->
            if (switch.isPressed) {
                if (isChecked) {
                    handleEnableFingerPrintAuth()
                } else {
                    handleDisableFingerPrintAuth()
                }
            }
        }
        binding.otpSwitch.setOnCheckedChangeListener { switch, isChecked ->
            if (switch.isPressed) {
                if (isChecked) {
                    openPhoneAuthView()
                } else {
                    handleDisableOTPAuth()
                }
            }
        }
    }

    // Finger Print
    private fun handleDisableFingerPrintAuth() {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentUserEmail = FirebaseAuthManager.getCurrentFirebaseUser()?.email ?: return@launch

            cryptographyManager.removeData(currentUserEmail)
            DataStoreHelper.removeStringInList(requireContext(), Constants.biometricAuthUsers, currentUserEmail)
            viewModel.updateFingerPrintSwitch(false)
        }
    }

    private fun handleEnableFingerPrintAuth() {
        val errorMessage = viewModel.checkPasswordError(binding.passwordTextInputEditText.text.toString(), requireContext())
        binding.passwordTextInputLayout.error = errorMessage
        if (errorMessage != null) return

        showFingerPrintPopUp()
    }

    private fun showFingerPrintPopUp() {
        val canAuthenticateWithBiometrics = BiometricManager.from(requireContext()).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS

        if (canAuthenticateWithBiometrics) {
            handleBiometricSuccess()
        } else {
            showBiometricAuthError()
        }
    }

    private fun handleBiometricSuccess() {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentUserEmail = FirebaseAuthManager.getCurrentFirebaseUser()?.email ?: return@launch
            val cipher = cryptographyManager.getInitializedCipherForEncryption(currentUserEmail)
            val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(
                requireActivity(), ::encryptAndStoreServerToken
            )
            val promptInfo = BiometricPromptUtils.createPromptInfo(requireActivity())
            biometricPrompt.authenticate(promptInfo, androidx.biometric.BiometricPrompt.CryptoObject(cipher))
            DataStoreHelper.saveStringInList(requireContext(), Constants.biometricAuthUsers, currentUserEmail)
        }
    }

    private fun encryptAndStoreServerToken(authResult: androidx.biometric.BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.let { cipher ->
            val password = binding.passwordTextInputEditText.text.toString()
            val encryptedServerTokenWrapper = cryptographyManager.encryptData(password, cipher)
            cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                encryptedServerTokenWrapper,
                requireContext(),
                SHARED_PREFS_FILENAME,
                Context.MODE_PRIVATE,
                CIPHERTEXT_WRAPPER
            )
            viewModel.updateFingerPrintSwitch(true)
        }
    }

    private fun showBiometricAuthError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.app_cant_authenticate_using_biometrics),
            Toast.LENGTH_SHORT
        ).show()
    }

    // OTP
    private fun handleDisableOTPAuth() {
        IndicatorHandler.show(requireContext())
        viewModel.disableOTPAuth {
            IndicatorHandler.hide()
        }
    }

    private fun openPhoneAuthView() {
        Handler(Looper.getMainLooper()).postDelayed({
            val action = AdvancedSecurityFragmentDirections.actionAdvancedSecurityFragmentToPhoneAuthenticationFragment()
            action.viewType = Constants.ENABLE_OTP_AUTH
            findNavController().navigate(action)
        }, 500)
    }
}