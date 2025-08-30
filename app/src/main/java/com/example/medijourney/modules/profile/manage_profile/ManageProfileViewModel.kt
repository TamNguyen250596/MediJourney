package com.example.medijourney.modules.profile.manage_profile

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.DataStoreHelper
import com.example.medijourney.common.helpers.DateHelper
import com.example.medijourney.common.helpers.MediJourney
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.common.managers.realm.Operator
import com.example.medijourney.common.managers.realm.RealmManager
import com.example.medijourney.common.managers.realm.where
import com.example.medijourney.common.models.item_models.BaseItemInterface
import com.example.medijourney.common.models.item_models.DynamicUIItem
import com.example.medijourney.common.models.item_models.TitleItemModel
import com.example.medijourney.common.models.realm_models.UserMedicalSpecialty
import com.example.medijourney.common.models.realm_models.UserMembership
import com.example.medijourney.common.models.ui_models.MTextStyle
import com.example.medijourney.common.ui_components.recycle_view_adapter.h_dual_image_text_view.HDualImageTextViewHolder
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.launch

class ManageProfileViewModel: ViewModel() {

    // Properties
    var itemModels = MutableLiveData<MutableList<BaseItemInterface>>().also {
        it.value = mutableListOf()
    }
    private var userMembershipResult: RealmResults<UserMembership>? = null
    private var appVersion = ""
    private var currentAppManageProfile: Map<String, Any> = mutableMapOf()

    // Life cycle
    init {
        currentAppManageProfile = InternationManager.getCurrentAppManageProfile()
        appVersion = getAppVersion(MediJourney.getAppContext())
        viewModelScope.launch {
            getData()
        }
        val tempDataList = generateItemModels()
        itemModels.postValue(tempDataList)
    }

    // Get Data
    private suspend fun getData() {
        val currentUserCode = FirebaseAuthManager.getCurrentUserCode() ?: return

        userMembershipResult = RealmManager.read(UserMembership::class.java,
            realmQuery = where(UserMedicalSpecialty::userId.name, Operator.EQUAL, currentUserCode)
        )
    }

    // Functions
    @Suppress("UNCHECKED_CAST")
    private fun generateItemModels(): MutableList<BaseItemInterface>? {
        val attributes = currentAppManageProfile["attributes"] as? List<Map<String, Any>> ?: return null
        val dataList: MutableList<BaseItemInterface> = mutableListOf()

        for ((sectionIndex, attribute) in attributes.withIndex()) {
            val items = attribute["items"] as? List<Map<String, Any>>
            if (items.isNullOrEmpty()) continue

            val titleItemModel = TitleItemModel.fromMap(attribute)
            titleItemModel.title.font = R.font.proximanova_bold
            titleItemModel.groupIndex = sectionIndex
            dataList.add(titleItemModel)

            for ((itemIndex, item) in items.withIndex()) {
                val itemModel = generateDynamicUIModel(item)
                val isHideSeparator = itemIndex == items.lastIndex
                itemModel.groupIndex = sectionIndex
                itemModel.additionalData = mapOf(HDualImageTextViewHolder.IS_HIDE_SEPARATOR_KEY to isHideSeparator)
                dataList.add(itemModel)
            }
        }

        return dataList
    }

    private fun generateDynamicUIModel(map: Map<String, Any>): DynamicUIItem {
        val itemModel = DynamicUIItem.fromMap(map)
        val description = MTextStyle("", R.font.proximanova_regular, 16f, R.color.black)

        when (itemModel.itemTag) {
            "expiry_date" -> {
                val expiryDate = getMembershipExpirationDate()
                expiryDate?.let {
                    description.text = expiryDate
                }
            }
            "country" -> {
                description.text = InternationManager.getCountryName()
            }
            "language" -> {
                description.text = InternationManager.getLanguageName()
            }
            "version" -> {
                description.text = appVersion
            }
        }
        if (description.text.isNotEmpty()) {
            itemModel.description = description
        }
        return itemModel
    }

    // Helper function to get the app version
    private fun getMembershipExpirationDate(): String? {
        val userMembership = userMembershipResult?.firstOrNull() ?: return null
        if (!userMembership.isValid()) return null
        val expiredAt = userMembership.expiredAt ?: return null

        return DateHelper.convertRealmInstantToString(expiredAt, "dd/MM/yyyy")
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = if (VERSION.SDK_INT >= VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            "$versionName($versionCode)"
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    suspend fun generateLanguageNameInfo(): Pair<Array<String>, Int> {
        val currentLanguageCode = DataStoreHelper.getString2(Constants.currentLanguageCode)
        val tempLanguageNames = mutableListOf<String>()
        var currentCountryNameIndex = 0

        for ((index, language) in Constants.languageCodeArray.withIndex()) {
            val name = MediJourney.getAppContext().getString(R.string::class.java.getField(language).getInt(null))
            tempLanguageNames.add(name)
            if (currentLanguageCode?.contains(language) == true) {
                currentCountryNameIndex = index
            }
        }

        return Pair(tempLanguageNames.toTypedArray(), currentCountryNameIndex)
    }

    suspend fun generateCountryNameInfo(): Pair<Array<String>, Int> {
        val currentCountryCode = DataStoreHelper.getString2(Constants.currentCountryCode)
        val tempCountryNames = mutableListOf<String>()
        var currentCountryNameIndex = 0

        for ((index, country) in Constants.countryCodeArray.withIndex()) {
            val name = MediJourney.getAppContext().getString(R.string::class.java.getField(country.lowercase()).getInt(null))
            tempCountryNames.add(name)
            if (currentCountryCode?.contains(country) == true) {
                currentCountryNameIndex = index
            }
        }

        return Pair(tempCountryNames.toTypedArray(), currentCountryNameIndex)
    }

    suspend fun updateCurrentLanguageCode(index: Int) {
        InternationManager.updateCurrentLanguageCode(Constants.languageCodeArray[index])
    }

    fun updateCurrentCountryCode(index: Int) {
        viewModelScope.launch {
            InternationManager.updateCurrentCountryCode(Constants.countryCodeArray[index])
        }
    }
}