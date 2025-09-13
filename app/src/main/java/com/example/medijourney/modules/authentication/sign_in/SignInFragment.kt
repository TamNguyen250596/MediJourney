package com.example.medijourney.modules.authentication.sign_in

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.databinding.FragmentSignInBinding
import com.example.medijourney.modules.base.auth.AuthActivity
import com.facebook.CallbackManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment() {

    // Properties
    private lateinit var binding: FragmentSignInBinding
    private val viewModel: SignInViewModel by viewModels()
    private var callBackManager: CallbackManager? = null

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        observeUIComponents()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callBackManager?.onActivityResult(requestCode, resultCode, data)
    }

    // Functions
    private fun observeViewModel() {
        viewModel.enableSignInButton.observe(viewLifecycleOwner) { enable ->
            binding.signInButton.isEnabled = enable
            context?.let {
                val backgroundColor = when (enable) {
                    true -> ContextCompat.getColor(it, R.color.deep_turquoise_blue_color)
                    false -> ContextCompat.getColor(it, R.color.disable_grey_color)
                }
                binding.signInButton.setBackgroundColor(backgroundColor)
                binding.signInButton.setTextColor(ContextCompat.getColor(it, R.color.white))
            }
        }
        viewModel.signInState.observe(viewLifecycleOwner) {
            when(it) {
                AuthenticationResult.SIGN_IN_FAILED -> {
                    showFailedAuthenticationToast(getString(R.string.failed_authentication_error_message))
                }
                AuthenticationResult.SIGN_IN_SUCCESS -> {
                    val activity = requireActivity() as? AuthActivity ?: return@observe
                    activity.openMainApp()
                }
                else -> {}
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it) {
                IndicatorHandler.show(requireContext())
            } else {
                IndicatorHandler.hide()
            }
        }
    }

    private fun observeUIComponents() {
        binding.emailTextInputEditText.addTextChangedListener(textWatcher)
        binding.passwordTextInputEditText.addTextChangedListener(textWatcher)
        binding.signInButton.setOnClickListener {
            viewModel.signIn(
                binding.emailTextInputEditText.text.toString(),
                binding.passwordTextInputEditText.text.toString()
            )
        }
        binding.forgotPasswordTextView.setOnClickListener {
            handlePhoneAuthenticationNavigation(Constants.FORGOT_PASSWORD)
        }
        binding.signUpTextView.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
        binding.facebookSignInImageButton.setOnClickListener {
            if (callBackManager == null) {
                callBackManager = CallbackManager.Factory.create()
            }
            callBackManager?.let {
                viewModel.fbSignIn(requireActivity(), it)
            }
        }
        binding.googleSignInImageButton.setOnClickListener {
            viewModel.googleSignIn(requireActivity())
        }
        binding.phoneImageButton.setOnClickListener {
            handlePhoneAuthenticationNavigation(Constants.SIGN_IN_BY_PHONE_NUMBER)
        }
        binding.fingerPrintImageButton.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_fingerPrintSignInFragment)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            handleTextChanged(p0.toString())
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    private fun handleTextChanged(text: String) {
        when {
            binding.emailTextInputEditText.isFocused -> {
                binding.emailTextInputLayout.error = viewModel.checkEmailError(text, requireContext())
            }
            binding.passwordTextInputEditText.isFocused -> {
                binding.passwordTextInputLayout.error = viewModel.checkPasswordError(text, requireContext())
            }
        }
    }

    private fun showFailedAuthenticationToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun handlePhoneAuthenticationNavigation(viewType: String) {
        val direction = SignInFragmentDirections.actionSignInFragmentToPhoneAuthenticationFragment()
        direction.viewType = viewType
        findNavController().navigate(direction)
    }
}