package com.example.medijourney.common.managers.firebase_auth

import android.app.Activity
import android.content.Intent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialException
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.Constants.WEB_CLIENT_ID
import com.example.medijourney.common.helpers.MDataStore
import com.example.medijourney.common.managers.fire_store.FSFilterBuilder
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager.getCurrentFirebaseUser
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.modules.base.auth.AuthActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume

object FirebaseAuthManager {

    // Properties
    private val auth = Firebase.auth

    // Get Current User Info
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun getCurrentUserCode(): String? {
        return auth.currentUser?.uid
    }
}

class FAManger @Inject constructor(
    private val mDataStore: MDataStore
) {

    val currentUser: FirebaseUser?
        get() {
            return Firebase.auth.currentUser
        }
    val currentUserCode: String
        get() {
            return Firebase.auth.currentUser?.uid ?: ""
        }

    // Sign Up
    suspend fun signUp(email: String, password: String, additionalUserInfo: Map<String, Any>? = null): AuthenticationResult {
        val user = suspendCancellableCoroutine { count ->
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    count.resume(it.result.user)
                }
        }
        return if (user != null) {
            handleNewUser(user, additionalUserInfo)
        } else {
            AuthenticationResult.SIGN_UP_FAILED
        }
    }

    // Sign In
    suspend fun signIn(email: String, password: String): AuthenticationResult {
        val user = suspendCancellableCoroutine { count ->
            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    count.resume(it.result.user)
                }
        }
        return if (user != null) {
            handleAvailableUser(user)
        } else {
            handleSignInFailed(email)
        }
    }

    suspend fun signInByFacebook(
        activity: Activity,
        callBackManager: CallbackManager?,
        permission: List<String>? = null,
    ): AuthenticationResult {
        val permissions = permission ?: listOf("email", "public_profile")
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions)

        val credential = suspendCancellableCoroutine { count ->
            LoginManager.getInstance().registerCallback(
                callBackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onCancel() {
                        count.resume(null)
                    }

                    override fun onError(error: FacebookException) {
                        count.resume(null)
                    }

                    override fun onSuccess(result: LoginResult) {
                        val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                        count.resume(credential)
                    }
                }
            )
        }

        return if (credential != null) {
            signIn(credential)
        } else {
            AuthenticationResult.SIGN_IN_FAILED
        }
    }

    suspend fun signInByPhoneNumber(phoneNumber: String, activity: Activity): AuthenticationResult {
        val credential = suspendCancellableCoroutine { count ->
            val callBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(), CallbackManager {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    count.resume(p0)
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    count.resume(null)
                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(p0, p1)
                    val credential = PhoneAuthProvider.getCredential(p0, p1.toString())
                    count.resume(credential)
                }

                override fun onActivityResult(
                    requestCode: Int,
                    resultCode: Int,
                    data: Intent?
                ): Boolean {
                    return true
                }
            }

            val options = PhoneAuthOptions.newBuilder(Firebase.auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callBack)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        return if (credential != null) {
            signIn(credential)
        } else {
            AuthenticationResult.SIGN_IN_FAILED
        }
    }

    suspend fun signInByGoogle(activity: Activity): AuthenticationResult {
        val rawNonce = UUID. randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md. digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credentialManager = CredentialManager.create(activity)

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = activity,
            )
            val credential = result.credential
            when (credential) {
                is PasswordCredential -> {
                    val username = credential.id
                    val password = credential.password
                    return signIn(username, password)
                }

                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            return signIn(firebaseCredential)
                        } catch (_: GoogleIdTokenParsingException) {
                            return AuthenticationResult.SIGN_IN_FAILED
                        }
                    } else {
                        return AuthenticationResult.SIGN_IN_FAILED
                    }
                }

                else -> {
                    return AuthenticationResult.SIGN_IN_FAILED
                }
            }

        } catch (_: GetCredentialException) {
            return AuthenticationResult.SIGN_IN_FAILED
        }
    }

    private suspend fun signIn(credential: AuthCredential): AuthenticationResult {
        val result = suspendCancellableCoroutine { count ->
            Firebase.auth.signInWithCredential(credential)
                .addOnCompleteListener {
                    count.resume(it.result.user)
                }
        }
        return if (result != null) {
            handleAvailableUser(result)
        } else {
            AuthenticationResult.SIGN_IN_FAILED
        }
    }

    private suspend fun handleAvailableUser(user: FirebaseUser): AuthenticationResult {
        try {
            val snapshot = FireStoreManager.getCollection(
                FireStoreCollection.USER_SETTINGS,
                filterBuilder = FSFilterBuilder().equalTo("user_id", user.uid)
            )
            val document = snapshot.documents.firstOrNull()

            if (document != null && document.exists() &&
                document.data?.get("enable_otp_auth") as? Boolean == true) {
                return AuthenticationResult.OTP_REQUIRED
            } else {
                val docSnapshot = FireStoreManager.getDoc(
                    FireStoreCollection.USER_MEMBERS,
                    user.uid
                )
                val data = docSnapshot.data ?: return AuthenticationResult.SIGN_IN_FAILED
                RealmManager.create(User::class.java, data)
                return AuthenticationResult.SIGN_IN_SUCCESS
            }
        } catch (_: Exception) {
            return AuthenticationResult.SIGN_IN_FAILED
        }
    }

    private suspend fun handleNewUser(user: FirebaseUser, additionalUserInfo: Map<String, Any>? = null): AuthenticationResult {
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
        val result = FireStoreManager.createDoc(
            FireStoreCollection.USER_MEMBERS,
            user.uid,
            map
        )
        return if (result) {
            AuthenticationResult.SIGN_UP_SUCCESS
        } else {
            AuthenticationResult.SIGN_UP_FAILED
        }
    }

    private suspend fun handleSignInFailed(email: String): AuthenticationResult {
        try {
            val snapshots = FireStoreManager.getCollection(
                FireStoreCollection.USER_MEMBERS,
                filterBuilder = FSFilterBuilder().equalTo("email", email)
            )
            val document = snapshots.documents.firstOrNull()
            return if (document != null && document["de_active"] == true) {
                AuthenticationResult.ACCOUNT_DEACTIVATED
            } else {
                AuthenticationResult.SIGN_UP_FAILED
            }
        } catch (_: Exception) {
            return AuthenticationResult.SIGN_IN_FAILED
        }
    }

    // DeActive User
    suspend fun deActiveUser(): AuthenticationResult {
        val currentUser = currentUser ?: return AuthenticationResult.DEACTIVE_ACCOUNT_FAILED
        val isDelectedUser = suspendCancellableCoroutine { count ->
            currentUser.delete()
                .addOnCompleteListener {
                    count.resume(it.isSuccessful)
                }
        }
        if (isDelectedUser) {
            val result = FireStoreManager.updateDoc(
                FireStoreCollection.USER_MEMBERS,
                currentUser.uid,
                mapOf("de_active" to true)
            )
            return if (result) {
                AuthenticationResult.DEACTIVE_ACCOUNT_SUCCESS
            } else {
                AuthenticationResult.DEACTIVE_ACCOUNT_FAILED
            }
        } else {
            return AuthenticationResult.DEACTIVE_ACCOUNT_FAILED
        }
    }

    // Delete Account
    suspend fun deleteUser(): AuthenticationResult {
        val currentUser = currentUser ?: return AuthenticationResult.DELETE_ACCOUNT_FAILED
        val isDelectedUser = suspendCancellableCoroutine { count ->
            currentUser.delete()
                .addOnCompleteListener {
                    count.resume(it.isSuccessful)
                }
        }
        return if (isDelectedUser) {
            val result = FireStoreManager.deleteDoc(
                FireStoreCollection.USER_MEMBERS,
                currentUser.uid
            )
            if (result) {
                AuthenticationResult.DELETE_ACCOUNT_SUCCESS
            } else {
                AuthenticationResult.DELETE_ACCOUNT_FAILED
            }
        } else {
            AuthenticationResult.DELETE_ACCOUNT_FAILED
        }
    }

    // Change password
    suspend fun changePasswordWhenLogIn(newPassword: String): AuthenticationResult {
        val currentUser = currentUser ?: return AuthenticationResult.CHANGE_PASSWORD_FAILED
        val isUpdatedPassword = suspendCancellableCoroutine { count ->
            currentUser.updatePassword(newPassword)
                .addOnCompleteListener {
                    count.resume(it.isSuccessful)
                }
        }
        return if (isUpdatedPassword) {
            AuthenticationResult.CHANGE_PASSWORD_SUCCESS
        } else {
            AuthenticationResult.CHANGE_PASSWORD_FAILED
        }
    }

    suspend fun changePasswordWithoutLogIn(tempToken: String, newPassword: String): AuthenticationResult {
        val gson = Gson()
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        val map: Map<String, String> = gson.fromJson(tempToken, mapType)
        val userName = map["user_name"] ?: return AuthenticationResult.CHANGE_PASSWORD_FAILED
        val currentPassword = map["current_password"] ?: return AuthenticationResult.CHANGE_PASSWORD_FAILED
        val credential = EmailAuthProvider.getCredential(userName, currentPassword)
        val currentFirebaseUser = getCurrentFirebaseUser() ?: return AuthenticationResult.CHANGE_PASSWORD_FAILED
        val isUpdatedPassword = suspendCancellableCoroutine { count ->
            currentFirebaseUser.reauthenticate(credential)
                .addOnCompleteListener {
                    count.resume(it.isSuccessful)
                }
        }

        return if (isUpdatedPassword) {
            changePasswordWhenLogIn(newPassword)
        } else {
            AuthenticationResult.CHANGE_PASSWORD_FAILED
        }
    }

    // Log Out
    suspend fun logOut(activity: Activity) {
        FireStoreManager.removeAllListeners()
        RealmManager.deleteAll()
        mDataStore.deleteAuthenticatedPreferencesKey()
        Firebase.auth.signOut()
        val intent = Intent(activity, AuthActivity::class.java)
        intent.putExtra(Constants.SIGN_OUT, true)
        activity.startActivity(intent)
        activity.finish()
    }
}