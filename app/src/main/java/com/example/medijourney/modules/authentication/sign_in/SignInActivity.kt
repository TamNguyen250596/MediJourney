package com.example.medijourney.modules.authentication.sign_in

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.medijourney.R
import com.example.medijourney.databinding.ActivitySignInBinding
import com.example.medijourney.modules.authentication.sign_up.SignUpActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    // Properties
    private lateinit var binding: ActivitySignInBinding
    private lateinit var signInViewModel: SignInViewModel

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        signInViewModel = SignInViewModel(Firebase.auth)
    }

    override fun onStart() {
        super.onStart()
        signInViewModel.user?.observe(this) { user ->
            if (user == null) {
                showFailedAuthenticationToast()
            } else {

            }
        }
        signInViewModel.enableSignInButton.observe(this) { enable ->
            binding.signInButton.isEnabled = enable
            binding.signInButton.setBackgroundColor(if (enable) ContextCompat.getColor(this,
                R.color.deep_turquoise_blue_color
            )
            else ContextCompat.getColor(this, R.color.disable_grey_color))
            binding.signInButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        binding.signInButton.setOnClickListener {
            val email = binding.emailTextInputEditText.text.toString()
            val password = binding.passwordTextInputEditText.text.toString()
            signInViewModel.signIn(email, password)
        }
        binding.emailTextInputEditText.addTextChangedListener(textWatcher)
        binding.passwordTextInputEditText.addTextChangedListener(textWatcher)
        binding.signUpButton.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }
    }

    // Functions
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            when {
                binding.emailTextInputEditText.isFocused -> {
                    binding.emailTextInputLayout.error = signInViewModel.checkEmailError(p0.toString(), this@SignInActivity)
                }
                binding.passwordTextInputEditText.isFocused -> {
                    binding.passwordTextInputLayout.error = signInViewModel.checkPasswordError(p0.toString(), this@SignInActivity)
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