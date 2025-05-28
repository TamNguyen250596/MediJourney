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
import com.example.medijourney.R.color.deep_turquoise_blue_color
import com.example.medijourney.R.color.disable_grey_color
import com.example.medijourney.R.color.white
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.databinding.FragmentSignUpBinding

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
        viewModel.onViewCreated(view)
        observeUIComponents()
        observeViewModel()
    }

    override fun onPause() {
        super.onPause()
        IndicatorHandler.hide()
    }

    // Functions
    private fun observeViewModel() {
        viewModel.enableSignUpButton.observe(viewLifecycleOwner) { enable ->
            binding.signUpButton.isEnabled = enable
            context?.let {
                val backgroundColor = when (enable) {
                    true -> ContextCompat.getColor(it, deep_turquoise_blue_color)
                    false -> ContextCompat.getColor(it, disable_grey_color)
                }
                binding.signUpButton.setBackgroundColor(backgroundColor)
                binding.signUpButton.setTextColor(ContextCompat.getColor(it, white))
            }
        }
        viewModel.errorMessages.observe(viewLifecycleOwner) {
            it?.let {
                showFailedAuthenticationToast(it)
            }
        }
    }

    private fun observeUIComponents() {
        binding.emailTextInputEditText.addTextChangedListener(textWatcher)
        binding.passwordTextInputEditText.addTextChangedListener(textWatcher)
        binding.confirmPasswordTextInputEditText.addTextChangedListener(textWatcher)
        binding.signUpButton.setOnClickListener {
            val fullName = binding.fullNameTextInputEditText.text.toString()
            val email = binding.emailTextInputEditText.text.toString()
            val password = binding.passwordTextInputEditText.text.toString()
            FirebaseAuthManager.signUpUser(fullName, email, password, requireContext())
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
        context?.let {
            when {
                binding.emailTextInputEditText.isFocused -> {
                    binding.emailTextInputLayout.error = viewModel.checkEmailError(text, it)
                }
                binding.passwordTextInputEditText.isFocused -> {
                    binding.passwordTextInputLayout.error = viewModel.checkPasswordError(text, it)
                }
                binding.confirmPasswordTextInputEditText.isFocused -> {
                    val password = binding.passwordTextInputEditText.text.toString()
                    binding.confirmPasswordTextInputLayout.error = viewModel.checkConfirmPasswordError(password, text, it)
                }
            }
        }
    }

    private fun showFailedAuthenticationToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}