package com.example.medijourney.modules.authentication.sign_up

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medijourney.R
import com.example.medijourney.common.constants.FirebaseConstants
import com.example.medijourney.common.constants.StringFormatConstants
import com.example.medijourney.common.models.User
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignUpViewModel(private val auth: FirebaseAuth): ViewModel() {

    // Properties
    var user: MutableLiveData<User>? = null
    var enableSignUpButton = MutableLiveData<Boolean>(false)
    private var validTextViews: MutableMap<String, Boolean> = mutableMapOf(
        "email" to false,
        "password" to false
    )

    // Functions
    fun handleActivityResult(result: ActivityResult) {
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                handleGoogleSignInIntentCode(result.data)
            }
        }
    }

    fun checkEmailError(email: String, context: android.content.Context): String? {
        val isValid = email.matches(StringFormatConstants.emailValidatorFormat.toRegex())
        validTextViews["email"] = isValid
        this.setEnableSignUpButtonValue(!validTextViews.values.contains(false))
        return if (isValid) null else context.getString(R.string.email_format_invalid)
    }

    fun checkPasswordError(password: String, context: android.content.Context): String? {
        val isValid = password.count() >= 6
        validTextViews["password"] = isValid
        this.setEnableSignUpButtonValue(!validTextViews.values.contains(false) )
        return  if (isValid) null else context.getString(R.string.password_length_short)
    }

    fun signUpUser(fullName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { currentUser ->
                        val user = User(currentUser.email.toString(), fullName)
                        saveUserToFirebaseStore(user, currentUser.uid)
                    }
                } else {
                    this.setUserValue(null)
                }
            }
    }

    fun signInGmail(activity: Activity, resultLauncher: ActivityResultLauncher<Intent>) {
        val signInRequest = GoogleSignInOptions.Builder()
            .requestIdToken(FirebaseConstants.webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, signInRequest)
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInIntentCode(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.result
            val idToken = account.idToken ?: return
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth.currentUser?.let { currentUser ->
                            val user = User(currentUser.email.toString(), currentUser.displayName.toString())
                            saveUserToFirebaseStore(user, currentUser.uid)
                        }
                    } else {
                        this.setUserValue(null)
                    }
                }
        } catch (e: ApiException) {
            this.setUserValue(null)
        }
    }

    private fun saveUserToFirebaseStore(user: User, documentId: String) {
        FirebaseFirestore.getInstance()
            .collection(FirebaseConstants.userMemberCollectionID)
            .document(documentId)
            .set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    this.setUserValue(user)
                } else {
                    this.setUserValue(null)
                }
            }
    }

    private fun setUserValue(user: User?) {
        this.user?.postValue(user)
        this.user?.value = user
    }

    private fun setEnableSignUpButtonValue(enable: Boolean) {
        this.enableSignUpButton.postValue(enable)
        this.enableSignUpButton.value = enable
    }
}
