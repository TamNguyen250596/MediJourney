package com.example.medijourney.common.managers.firebase_auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.DataStoreHelper
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.ui_components.dialogs.IndicatorHandler
import com.example.medijourney.modules.base.auth.AuthActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object FirebaseAuthManager {

    // Properties
    private val auth = Firebase.auth
    var storedVerificationId: String? = null
    var resendToken: String? = null
    var userAuthenticationResult: BehaviorSubject<AuthenticationResult> = BehaviorSubject.createDefault(AuthenticationResult.SIGN_OUT)

    // Sign Up
    fun signUpUser(fullName: String, email: String, password: String, context: Context?) {
        context?.let { IndicatorHandler.show(context) }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                auth.currentUser?.let { handleNewUser(it) }
            }
            .addOnFailureListener {
                updateUserAuthenticationResult(AuthenticationResult.SIGN_UP_FAILED)
            }
    }

    // Sign In
    fun signIn(email: String, password: String, context: Context?) {
        context?.let { IndicatorHandler.show(context) }
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                auth.currentUser?.let { handleAvailableUser(it) }
            }
            .addOnFailureListener {
                handleError(email)
            }
    }

    fun signInByFacebook(
        activity: Activity,
        callBackManager: CallbackManager?,
        permission: List<String>? = null,
    ) {
        val permissions = permission ?: listOf("email", "public_profile")
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions)

        LoginManager.getInstance().registerCallback(
            callBackManager,
            object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    updateUserAuthenticationResult(AuthenticationResult.SIGN_OUT)
                }

                override fun onError(error: FacebookException) {
                    updateUserAuthenticationResult(AuthenticationResult.SIGN_IN_FAILED)
                }

                override fun onSuccess(result: LoginResult) {
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    signInWithCredential(credential, activity)
                }
            }
        )
    }

    fun openGmailSignInDialog(
        activity: Activity,
        resultLauncher: ActivityResultLauncher<Intent>) {
        val signInRequest = GoogleSignInOptions.Builder()
            .requestIdToken(Constants.WEB_CLIENT_ID)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, signInRequest)
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    fun signInByGmail(
        data: Intent?,
        context: Context?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.result
            val idToken = account.idToken ?: return
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            signInWithCredential(credential, context)
        } catch (e: ApiException) {
            updateUserAuthenticationResult(AuthenticationResult.SIGN_IN_FAILED)
        }
    }

    fun signInByPhoneNumber(activity: Activity) {
        val phoneNumber = "+16505554567"
        val smsCode = "123456"

        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        auth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode)
        val callBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(),
            CallbackManager {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                signInWithCredential(p0, activity)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                updateUserAuthenticationResult(AuthenticationResult.SIGN_IN_FAILED)
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                storedVerificationId = p0
                resendToken = p1.toString()
            }

            override fun onActivityResult(
                requestCode: Int,
                resultCode: Int,
                data: Intent?
            ): Boolean {
                updateUserAuthenticationResult(AuthenticationResult.SIGN_OUT)
                return true
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callBack)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Log Out
    fun logOut(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            FireStoreManager.removeAllListeners()
            RealmManager.deleteAll()
            DataStoreHelper.deleteAuthenticatedPreferencesKey(activity)
            storedVerificationId = null
            resendToken = null
            auth.signOut()
            userAuthenticationResult.onNext(AuthenticationResult.SIGN_OUT)
            val intent = Intent(activity, AuthActivity::class.java)
            intent.putExtra(Constants.SIGN_OUT, true)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    // DeActive User
    fun deActiveUser(context: Context?) {
        val currentFirebaseUser = getCurrentFirebaseUser() ?: return
        context?.let { IndicatorHandler.show(it) }
        currentFirebaseUser.delete()
            .addOnSuccessListener {
                FireStoreManager.buildDocRef(Pair(FireStoreCollection.USER_MEMBER, currentFirebaseUser.uid))
                    .update(mapOf("de_active" to true))
                    .addOnSuccessListener {
                        updateUserAuthenticationResult(AuthenticationResult.DEACTIVE_ACCOUNT_SUCCESS)
                    }
                    .addOnFailureListener {
                        updateUserAuthenticationResult(AuthenticationResult.DEACTIVE_ACCOUNT_FAILED)
                    }
            }
            .addOnFailureListener {
                updateUserAuthenticationResult(AuthenticationResult.DEACTIVE_ACCOUNT_FAILED)
            }
    }

    // Delete Account
    fun deleteUser(activity: Activity) {
        val currentFirebaseUser = getCurrentFirebaseUser() ?: return
        IndicatorHandler.show(activity)
        currentFirebaseUser.delete()
            .addOnSuccessListener {
                FireStoreManager.buildDocRef(Pair(FireStoreCollection.USER_MEMBER, currentFirebaseUser.uid))
                    .delete()
                    .addOnSuccessListener {
                        updateUserAuthenticationResult(AuthenticationResult.DELETE_ACCOUNT_SUCCESS)
                    }
                    .addOnFailureListener {
                        updateUserAuthenticationResult(AuthenticationResult.DELETE_ACCOUNT_FAILED)
                    }
            }
            .addOnFailureListener {
                updateUserAuthenticationResult(AuthenticationResult.DELETE_ACCOUNT_FAILED)
            }
    }

    // Change password
    fun changePasswordWhenLogIn(newPassword: String, context: Context?) {
        val currentFirebaseUser = getCurrentFirebaseUser() ?: return
        context?.let { IndicatorHandler.show(context) }
        currentFirebaseUser.updatePassword(newPassword)
            .addOnCompleteListener { response ->
                if (response.isSuccessful) {
                    updateUserAuthenticationResult(AuthenticationResult.CHANGE_PASSWORD_SUCCESS)
                } else {
                    updateUserAuthenticationResult(AuthenticationResult.CHANGE_PASSWORD_FAILED)
                }
            }
    }

    fun changePasswordWithoutLogIn(tempToken: String, newPassword: String, context: Context?) {
        context?.let { IndicatorHandler.show(context) }

        val gson = Gson()
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        val map: Map<String, String> = gson.fromJson(tempToken, mapType)
        val userName = map["user_name"] ?: return
        val currentPassword = map["current_password"] ?: return
        val credential = EmailAuthProvider.getCredential(userName, currentPassword)
        val currentFirebaseUser = getCurrentFirebaseUser() ?: return

        currentFirebaseUser.reauthenticate(credential).addOnCompleteListener { response ->
            if (response.isSuccessful) {
                changePasswordWhenLogIn(newPassword, context)
            } else {
                updateUserAuthenticationResult(AuthenticationResult.CHANGE_PASSWORD_FAILED)
            }
        }
    }

    // Get Current User Info
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getCurrentUserCode(): String? {
        return auth.currentUser?.uid
    }

    suspend fun getCurrentRealmUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return RealmManager.read(User::class.java, uid)
    }

    // Functions
    private fun signInWithCredential(credential: AuthCredential, context: Context?) {
        context?.let { IndicatorHandler.show(context) }
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                auth.currentUser?.let { handleAvailableUser(it) }
            }
            .addOnFailureListener {
                updateUserAuthenticationResult(AuthenticationResult.SIGN_IN_FAILED)
            }
    }

    private fun handleAvailableUser(user: FirebaseUser) {
        FireStoreManager.buildUserCollectionRef(FireStoreCollection.USER_SETTINGS)
            .get()
            .addOnSuccessListener {
                val document = it.documents.firstOrNull()
                if (document != null && document.exists() &&
                    document.data?.get("enable_otp_auth") as? Boolean == true) {
                    updateUserAuthenticationResult(AuthenticationResult.OTP_REQUIRED)
                } else {
                    FireStoreManager.buildDocRef(Pair(FireStoreCollection.USER_MEMBER, user.uid))
                        .get()
                        .addOnSuccessListener { response ->
                            response.data?.let {
                                CoroutineScope(Dispatchers.IO).launch {
                                    RealmManager.create(User::class.java, it)
                                }
                                updateUserAuthenticationResult(AuthenticationResult.SIGN_IN_SUCCESS)
                            }
                        }
                        .addOnFailureListener {
                            updateUserAuthenticationResult(AuthenticationResult.SIGN_IN_FAILED)
                        }
                }
            }
            .addOnFailureListener {
                updateUserAuthenticationResult(AuthenticationResult.SIGN_IN_FAILED)
            }
    }

    private fun handleNewUser(user: FirebaseUser, additionalUserInfo: Map<String, Any>? = null) {
        val map: MutableMap<String, Any> = mutableMapOf()
        map["user_code"] = user.uid
        val fullName = additionalUserInfo?.get("full_name") as? String ?: user.displayName
        fullName?.let {
            map["full_name"] = it
            map["display_name"] = it
        }
        user.email?.let { email ->
            map["email"] = email
        }
        FireStoreManager.buildDocRef(Pair(FireStoreCollection.USER_MEMBER, user.uid))
            .set(map)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    updateUserAuthenticationResult(AuthenticationResult.SIGN_UP_FAILED)
                    return@addOnCompleteListener
                }

                CoroutineScope(Dispatchers.IO + Job()).launch {
                    RealmManager.create(User::class.java, map)
                    updateUserAuthenticationResult(AuthenticationResult.SIGN_UP_SUCCESS)
                }
            }
    }

    private fun handleError(email: String) {
        FireStoreManager.buildCollectionRef(FireStoreCollection.USER_MEMBER)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener {
                val document = it.documents.firstOrNull()
                if (document != null && document["de_active"] == true) {
                    updateUserAuthenticationResult(AuthenticationResult.ACCOUNT_DEACTIVATED)
                } else {
                    updateUserAuthenticationResult(AuthenticationResult.SIGN_UP_FAILED)
                }
            }
            .addOnFailureListener {
                updateUserAuthenticationResult(AuthenticationResult.SIGN_UP_FAILED)
            }
    }

    private fun updateUserAuthenticationResult(result: AuthenticationResult) {
        IndicatorHandler.hide()
        userAuthenticationResult.onNext(result)
    }
}