package com.example.medijourney.modules.profile.edit_profile

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.constants.EditProfileField
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.helpers.MediJourney
import com.example.medijourney.common.managers.firebase_storage.FirebaseStorageManager
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.managers.fire_store.FireStoreCollection
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.modules.profile.edit_profile.adapter.EditProfileViewHolder
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.notifications.UpdatedObject
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class EditProfileViewModel: ViewModel() {

    // Properties
    val itemModels = MutableLiveData<MutableList<DynamicUIItem>>()
    var bgImageUri = MutableLiveData<Uri?>(null)
    var avatarImageUri = MutableLiveData<Uri?>(null)
    var changedItemPosition = MutableLiveData<Int?>(null)
    private var errorAtTag: MutableMap<String, String> = mutableMapOf()
    private var tempInfoAtTag: MutableMap<String, Any> = mutableMapOf()
    private var user: User? = null
    private var currentAppEditProfile: Map<String, Any> = mutableMapOf()

    // Life cycle
    init {
        currentAppEditProfile = InternationManager.getCurrentAppEditProfile()
        viewModelScope.launch {
            getCurrentUser()
            observeCurrentUser()
        }
        val data = generateItemModels()
        itemModels.postValue(data)
        handleBackgroundImage()
        handleAvatarImage()
    }

    // Functions
    private suspend fun getCurrentUser() {
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return
        user = RealmManager.read(User::class.java, currentUserCode)
    }

    private suspend fun observeCurrentUser() {
        val user = user ?: return

        user.asFlow().collect {
            this.user = it.obj
            when (it) {
                is UpdatedObject -> {
                    when (true) {
                        it.changedFields.contains(User::backgroundUrl.name) -> {
                            handleBackgroundImage()
                        }
                        it.changedFields.contains(User::avatarUrl.name) -> {
                            handleAvatarImage()
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
            tempInfoAtTag.clear()
            val data = generateItemModels()
            withContext(Dispatchers.Main) {
                itemModels.postValue(data)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun generateItemModels(): MutableList<DynamicUIItem> {
        val user = user ?: return mutableListOf()
        val attributes = currentAppEditProfile["attributes"] as? List<Map<String, Any>> ?: return mutableListOf()
        val tempList: MutableList<DynamicUIItem> =  mutableListOf()

        for (data in attributes) {
            val itemModel = DynamicUIItem.fromMap(data)
            getTitle(itemModel.itemTag, user)?.let {
                itemModel.title = MTextStyle(it)
            }
            itemModel.description?.color = R.color.disable_grey_color
            val isEditable = itemModel.itemTag != EditProfileField.GENDER.name.lowercase() &&
                    itemModel.itemTag != EditProfileField.BIRTH_DATE.name.lowercase()
            itemModel.additionalData = mapOf(EditProfileViewHolder.IS_EDITABLE to isEditable)
            tempList.add(itemModel)
        }
        return tempList
    }

    // Background image and Avatar
    fun handleGalleryLauncherResult(
        uri: Uri,
        currentFocusField: EditProfileField,
        completion: (Boolean) -> Unit
    ) {
        val (imageName, fireStoreKey) = when (currentFocusField) {
            EditProfileField.BACKGROUND -> "background" to "background_url"
            EditProfileField.AVATAR -> "avatar" to "avatar_url"
            else -> return completion(false)
        }

        FirebaseStorageManager.saveImage(uri, imageName) { url ->
            if (url.isNullOrEmpty()) return@saveImage completion(false)
            val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return@saveImage completion(false)

            FireStoreManager.buildDoc(FireStoreCollection.USER_MEMBERS, currentUserCode)
                .update(mapOf(fireStoreKey to url))
                .addOnCompleteListener { completion.invoke(it.isSuccessful) }
        }
    }

    private fun handleBackgroundImage() {
        FirebaseStorageManager.downloadUserImage("background", "jpg") { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                bgImageUri.postValue(uri)
            }
        }
    }

    private fun handleAvatarImage() {
        FirebaseStorageManager.downloadUserImage("avatar", "jpg") { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                avatarImageUri.postValue(uri)
            }
        }
    }

    // Edit Text
    private fun getTitle(itemTag: String, user: User): String? {
        if (!user.isValid()) return null

        return when (val fieldTag = EditProfileField.valueOf(itemTag.uppercase())) {
            EditProfileField.DISPLAY_NAME,
            EditProfileField.BIO,
            EditProfileField.FULL_NAME,
            EditProfileField.EMAIL,
            EditProfileField.MOBILE_NUMBER,
            EditProfileField.ADDRESS -> {
                val value = when (fieldTag) {
                    EditProfileField.DISPLAY_NAME -> user.displayName
                    EditProfileField.BIO -> user.bio
                    EditProfileField.FULL_NAME -> user.fullName
                    EditProfileField.EMAIL -> user.email
                    EditProfileField.MOBILE_NUMBER -> user.mobileNo
                    EditProfileField.ADDRESS -> user.address
                    else -> null
                }
                value?.let {
                    tempInfoAtTag[itemTag] = value
                }
                value
            }
            EditProfileField.GENDER -> {
                val gender = user.gender
                gender?.let {
                    tempInfoAtTag[itemTag] = it
                }
                handleGenderTitle(gender)
            }
            EditProfileField.BIRTH_DATE -> {
                val birthDate = user.birthDate
                birthDate?.let {
                    tempInfoAtTag[itemTag] = DateHelper.convertRealmInstantToMillis(it)
                }
                handleBirthdateTitle(birthDate)
            }
            else -> null
        }
    }

    fun getTempTitle(itemTag: String): String? {
        return when (EditProfileField.valueOf(itemTag.uppercase())) {
            EditProfileField.DISPLAY_NAME,
            EditProfileField.BIO,
            EditProfileField.FULL_NAME,
            EditProfileField.EMAIL,
            EditProfileField.MOBILE_NUMBER,
            EditProfileField.ADDRESS -> {
                tempInfoAtTag[itemTag] as? String
            }
            EditProfileField.GENDER -> {
                val gender = tempInfoAtTag[itemTag] as? Number
                handleGenderTitle(gender)
            }
            EditProfileField.BIRTH_DATE -> {
                val birthDate = tempInfoAtTag[itemTag]
                handleBirthdateTitle(birthDate)
            }
            else -> null
        }
    }

    fun checkErrorAtField(itemTag: String, text: String): String? {
        val context = MediJourney.getAppContext()

        val value = when (EditProfileField.valueOf(itemTag.uppercase())) {
            EditProfileField.EMAIL -> {
                if (text.isEmpty()) {
                    "Email is required"
                } else {
                    val isValid = text.matches(Constants.EMAIL_VALIDATOR_FORMAT.toRegex())
                    if (isValid) null else context.getString(R.string.email_format_invalid)
                }
            }
            else -> null
        }

        value?.let {
            errorAtTag[itemTag] = it
        } ?: run {
            errorAtTag.remove(itemTag)
        }
        return value
    }

    fun getErrorAtField(itemTag: String): String? {
        return errorAtTag[itemTag]
    }

    private fun handleGenderTitle(value: Number?): String? {
        return value?.let { Constants.genderArray.getOrNull(it.toInt()) }
    }

    private fun handleBirthdateTitle(value: Any?): String? {
        return when(value) {
            is Long -> {
                DateHelper.convertDateToString(value, "dd/MM/yyyy")
            }
            is RealmInstant -> {
                DateHelper.convertRealmInstantToString(value, "dd/MM/yyyy")
            }
            else -> {
                null
            }
        }
    }

    fun updateTempInfoAtTag(itemTag: String, value: Any) {
        when (val fieldTag = EditProfileField.valueOf(itemTag.uppercase())) {
            EditProfileField.DISPLAY_NAME,
            EditProfileField.BIO,
            EditProfileField.FULL_NAME,
            EditProfileField.EMAIL,
            EditProfileField.MOBILE_NUMBER,
            EditProfileField.ADDRESS -> {
                val tempValue = value as? String ?: user?.takeIf { it.isValid() }?.let {
                    when (fieldTag) {
                        EditProfileField.DISPLAY_NAME -> it.displayName
                        EditProfileField.BIO -> it.bio
                        EditProfileField.FULL_NAME -> it.fullName
                        EditProfileField.EMAIL -> it.email
                        EditProfileField.MOBILE_NUMBER -> it.mobileNo
                        EditProfileField.ADDRESS -> it.address
                        else -> null
                    }
                }
                tempValue?.let {
                    tempInfoAtTag[itemTag] = tempValue
                }
            }
            EditProfileField.GENDER -> {
                val gender = value as? Number ?: user?.takeIf { it.isValid() }?.gender
                updateGender(gender, itemTag)
            }
            EditProfileField.BIRTH_DATE -> {
                val birthDate = value as? Date ?: user?.takeIf { it.isValid() }?.birthDate
                updateBirthDay(birthDate, itemTag)
            }
            else -> {}
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun updateGender(gender: Number?, itemTag: String) {
        val gender = gender ?: return
        tempInfoAtTag[itemTag] = gender
        reloadItem(itemTag)
    }

    @Suppress("NAME_SHADOWING")
    private fun updateBirthDay(birthDate: Any?, itemTag: String) {
        val birthDate = birthDate ?: return
        when (birthDate) {
            is Date -> {
                tempInfoAtTag[itemTag] = birthDate.time
            }
            is RealmInstant -> {
                val milliseconds = DateHelper.convertRealmInstantToMillis(birthDate)
                tempInfoAtTag[itemTag] = milliseconds
            }
        }
        reloadItem(itemTag)
    }

    private fun reloadItem(itemTag: String) {
        val index = itemModels.value?.indexOfFirst { it.itemTag == itemTag }
        changedItemPosition.postValue(index)
    }

    fun saveUser(completion: (Boolean) -> Unit) {
        if (errorAtTag.isNotEmpty()) return completion.invoke(false)
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return completion.invoke(false)

        FireStoreManager.buildDoc(FireStoreCollection.USER_MEMBERS, currentUserCode)
            .update(tempInfoAtTag)
            .addOnCompleteListener { completion.invoke(it.isSuccessful) }
    }

    fun createDate(year: Int, month: Int, dayOfMonth: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        return calendar.time
    }

    fun getGenderIndex(): Long {
        return user?.gender ?: 0
    }
}