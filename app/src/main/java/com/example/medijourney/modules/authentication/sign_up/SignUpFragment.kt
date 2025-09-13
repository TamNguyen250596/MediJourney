package com.example.medijourney.modules.authentication.sign_up

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
import com.example.medijourney.R.color.deep_turquoise_blue_color
import com.example.medijourney.R.color.disable_grey_color
import com.example.medijourney.R.color.white
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.databinding.FragmentSignUpBinding
import com.example.medijourney.modules.base.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    // Properties
    private lateinit var binding: FragmentSignUpBinding
    private val viewModel: SignUpViewModel by viewModels()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeUIComponents()
        observeViewModel()
    }

    // Functions
    private fun observeViewModel() {
        viewModel.enableSignUpButton.observe(viewLifecycleOwner) { enable ->
            binding.signUpButton.isEnabled = enable
            val backgroundColor = when (enable) {
                true -> ContextCompat.getColor(requireContext(), deep_turquoise_blue_color)
                false -> ContextCompat.getColor(requireContext(), disable_grey_color)
            }
            binding.signUpButton.setBackgroundColor(backgroundColor)
            binding.signUpButton.setTextColor(ContextCompat.getColor(requireContext(), white))
        }
        viewModel.signUpState.observe(viewLifecycleOwner) {
            when (it) {
                AuthenticationResult.SIGN_UP_FAILED -> {
                    showFailedAuthenticationToast(getString(R.string.failed_authentication_error_message))
                }
                AuthenticationResult.SIGN_UP_SUCCESS -> {
                    val activity = requireActivity() as? AuthActivity ?: return@observe
                    activity.openMainApp()
                }
                else -> {}
            }
        }
        viewModel.errorMessages.observe(viewLifecycleOwner) {
            val message = it ?: return@observe
            showFailedAuthenticationToast(message)
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
        binding.confirmPasswordTextInputEditText.addTextChangedListener(textWatcher)
        binding.signUpButton.setOnClickListener {
            viewModel.signUp(
                binding.fullNameTextInputEditText.text.toString(),
                binding.emailTextInputEditText.text.toString(),
                binding.passwordTextInputEditText.text.toString()
            )
        }
        binding.signInButton.setOnClickListener {
            findNavController().popBackStack()
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
            binding.confirmPasswordTextInputEditText.isFocused -> {
                val password = binding.passwordTextInputEditText.text.toString()
                binding.confirmPasswordTextInputLayout.error = viewModel.checkConfirmPasswordError(password, text, requireContext())
            }
        }
    }

    private fun showFailedAuthenticationToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}