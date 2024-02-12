package com.example.medijourney.modules.authentication.sign_up

import com.example.medijourney.common.FirebaseConstants
import com.example.medijourney.common.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpViewModel(private val auth: FirebaseAuth) {

    // Functions
    fun signUpUser(fullName: String, email: String, password: String, callBack: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserFullName(fullName, callBack)
                } else {
                    callBack(false)
                }
            }
    }

    private fun saveUserFullName(fullName: String, callBack: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return
        val user = User(fullName)
        FirebaseFirestore.getInstance()
            .collection(FirebaseConstants.userMemberCollectionID)
            .document(currentUser.uid)
            .set(user)
            .addOnCompleteListener { task ->
                callBack(task.isSuccessful)
            }
    }
}
