package com.example.medijourney.modules.profile.change_password

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.medijourney.R
import com.example.medijourney.R.color.deep_turquoise_blue_color
import com.example.medijourney.R.color.disable_grey_color
import com.example.medijourney.R.color.white
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {

    // Properties
    private lateinit var binding: FragmentChangePasswordBinding
    private val viewModel: ChangePasswordViewModel by viewModels()
    private val tempLogInToken: String? by navArgs()

    // Life cycle
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated(view)
        setupUI()
        observeViewModel()
    }

    // Functions
    private fun setupUI() {
        if (viewModel.checkUserLoggedIn()) {
            binding.currentPasswordTextInputEditText.visibility = View.VISIBLE
            binding.currentPasswordTextInputEditText.addTextChangedListener(textWatcher)
        } else {
            binding.currentPasswordTextInputEditText.visibility = View.GONE
        }
        binding.passwordTextInputEditText.addTextChangedListener(textWatcher)
        binding.confirmPasswordTextInputEditText.addTextChangedListener(textWatcher)
        binding.confirmButton.setOnClickListener {
            val newPassword = binding.passwordTextInputEditText.text.toString()
            if (viewModel.checkUserLoggedIn()) {
                FirebaseAuthManager.changePasswordWhenLogIn(newPassword, requireContext())
            } else {
                val tempToken = tempLogInToken ?: return@setOnClickListener
                FirebaseAuthManager.changePasswordWithoutLogIn(tempToken, newPassword, requireContext())
            }
        }
    }

    private fun observeViewModel() {
        viewModel.enableConfirmButton.observe(viewLifecycleOwner) { enable ->
            binding.confirmButton.isEnabled = enable
            context?.let {
                val backgroundColor = when (enable) {
                    true -> ContextCompat.getColor(it, deep_turquoise_blue_color)
                    false -> ContextCompat.getColor(it, disable_grey_color)
                }
                binding.confirmButton.setBackgroundColor(backgroundColor)
                binding.confirmButton.setTextColor(ContextCompat.getColor(it, white))
            }
        }
        viewModel.errorMessages.observe(viewLifecycleOwner) {
            it?.let {
                showDialog(getString(R.string.error), it)
            }
        }
        viewModel.successMessages.observe(viewLifecycleOwner) {
            it?.let {
                showDialog(getString(R.string.success), it)
            }
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
            binding.currentPasswordTextInputEditText.isFocused -> {
                binding.currentPasswordTextInputEditText.error = viewModel.checkCurrentPasswordError(text, requireContext())
            }
            binding.passwordTextInputEditText.isFocused -> {
                binding.passwordTextInputLayout.error = viewModel.checkNewPasswordError(text, requireContext())
            }
            binding.confirmPasswordTextInputEditText.isFocused -> {
                val password = binding.passwordTextInputEditText.text.toString()
                binding.confirmPasswordTextInputLayout.error = viewModel.checkConfirmPasswordError(password, text, requireContext())
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage(message)
            .setTitle(title)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}