package com.example.medijourney.modules.authentication.sign_up

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.medijourney.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    // Properties
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var signUpViewModel: SignUpViewModel

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        signUpViewModel = SignUpViewModel(Firebase.auth)
    }

    override fun onStart() {
        super.onStart()
        binding.signUpButton.setOnClickListener {
            val fullName = binding.fullNameTextInputEditText.text.toString()
            val email = binding.emailTextInputEditText.text.toString()
            val password = binding.passwordTextInputEditText.text.toString()
            signUpViewModel.signUpUser(fullName, email, password) { isSuccess ->
                if (isSuccess) {

                } else {
                    showFailedAuthenticationToast()
                }
            }
        }
    }

    // Functions
    private fun showFailedAuthenticationToast() {
        Toast.makeText(
            baseContext,
            "Authentication failed.",
            Toast.LENGTH_SHORT,
        ).show()
    }
}