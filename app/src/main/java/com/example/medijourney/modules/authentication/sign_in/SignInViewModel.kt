package com.example.medijourney.modules.authentication.sign_in

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medijourney.R
import com.example.medijourney.common.constants.FirebaseConstants
import com.example.medijourney.common.constants.StringFormatConstants
import com.example.medijourney.common.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class SignInViewModel(private val auth: FirebaseAuth): ViewModel() {

    // Properties
    var user: MutableLiveData<User>? = null
    var enableSignInButton = MutableLiveData<Boolean>(false)
    private var validTextViews: MutableMap<String, Boolean> = mutableMapOf(
        "email" to false,
        "password" to false
    )
    // Functions
    fun checkEmailError(email: String, context: android.content.Context): String? {
        val isValid = email.matches(StringFormatConstants.emailValidatorFormat.toRegex())
        validTextViews["email"] = isValid
        this.setEnableSignInButtonValue(!validTextViews.values.contains(false))
        return if (isValid) null else context.getString(R.string.email_format_invalid)
    }

    fun checkPasswordError(password: String, context: android.content.Context): String? {
        val isValid = password.count() >= 6
        validTextViews["password"] = isValid
        this.setEnableSignInButtonValue(!validTextViews.values.contains(false) )
        return  if (isValid) null else context.getString(R.string.password_length_short)
    }

    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { currentUser ->
                        FirebaseFirestore.getInstance()
                            .collection(FirebaseConstants.userMemberCollectionID)
                            .document(currentUser.uid)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    task.result.data?.let { data ->
                                        val user: User = Gson().fromJson(data.toString(), User::class.java)
                                        this.setUserValue(user)
                                    }
                                } else {
                                    this.setUserValue(null)
                                }
                            }
                    }
                } else {
                    this.setUserValue(null)
                }
            }
    }

    private fun setUserValue(user: User?) {
        this.user?.postValue(user)
        this.user?.value = user
    }

    private fun setEnableSignInButtonValue(enable: Boolean) {
        this.enableSignInButton.postValue(enable)
        this.enableSignInButton.value = enable
    }

}