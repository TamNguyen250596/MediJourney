package com.example.medijourney.modules.authentication.sign_up

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.medijourney.R.color.*
import com.example.medijourney.databinding.ActivitySignUpBinding
import com.example.medijourney.modules.authentication.sign_in.SignInActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    // Properties
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var signUpViewModel: SignUpViewModel
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        signUpViewModel = SignUpViewModel(Firebase.auth)
        setupResultLauncher()
    }

    override fun onStart() {
        super.onStart()
        signUpViewModel.user?.observe(this) { user ->
            if (user == null) {
                showFailedAuthenticationToast()
            } else {

            }
        }
        signUpViewModel.enableSignUpButton.observe(this) { enable ->
            binding.signUpButton.isEnabled = enable
            binding.signUpButton.setBackgroundColor(if (enable) ContextCompat.getColor(this, deep_turquoise_blue_color)
            else ContextCompat.getColor(this, disable_grey_color))
            binding.signUpButton.setTextColor(ContextCompat.getColor(this, white))
        }
        binding.emailTextInputEditText.addTextChangedListener(textWatcher)
        binding.passwordTextInputEditText.addTextChangedListener(textWatcher)
        binding.signUpButton.setOnClickListener {
            val fullName = binding.fullNameTextInputEditText.text.toString()
            val email = binding.emailTextInputEditText.text.toString()
            val password = binding.passwordTextInputEditText.text.toString()
            signUpViewModel.signUpUser(fullName, email, password)
        }
        binding.googleImageButton.setOnClickListener {
            signUpViewModel.signInGmail(this, resultLauncher)
        }
        binding.signInButton.setOnClickListener {
            val signInIntent = Intent(this, SignInActivity::class.java)
            startActivity(signInIntent)
        }
    }

    // Functions
    private fun setupResultLauncher() {
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
               signUpViewModel.handleActivityResult(result)
            }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            when {
                binding.emailTextInputEditText.isFocused -> {
                    binding.emailTextInputLayout.error = signUpViewModel.checkEmailError(p0.toString(), this@SignUpActivity)
                }
                binding.passwordTextInputEditText.isFocused -> {
                    binding.passwordTextInputLayout.error = signUpViewModel.checkPasswordError(p0.toString(), this@SignUpActivity)
                }
            }
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    private fun showFailedAuthenticationToast() {
        Toast.makeText(
            baseContext,
            "Authentication failed.",
            Toast.LENGTH_SHORT,
        ).show()
    }
}