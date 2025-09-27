
package com.example.medijourney.common.managers

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.helpers.DataStoreHelper
import com.example.medijourney.common.helpers.FileHelper
import com.example.medijourney.common.helpers.MediJourney
import com.example.medijourney.common.managers.fire_store.FireStoreManager
import com.example.medijourney.modules.base.auth.AuthActivity
import com.google.common.base.Objects
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier
import jakarta.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppExerciseTrackingReportDetails

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppSleepTrackingReportDetails

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppNutritionTrackingReportDetails

@Module
@InstallIn(ViewModelComponent::class)
object InternationManager {

    // Properties
    var currentCountryName: String = ""
    var currentCountryCode: String = "US"
    var currentLanguageName: String = ""
    var currentLanguageCode: String = "en"
    var currentLocale: String = "en_US"
    var realmName = Constants.MEDI_JOURNEY

    // Functions
    suspend fun config() {
        val context = MediJourney.getAppContext()

        currentLanguageCode = getCurrentLanguageCode(context)
        currentLanguageName = getLanguageName()
        currentCountryCode = getCurrentCountryCode(context)
        currentCountryName = getCountryName()
//        currentLocale = "${currentLanguageCode}_${currentCountryCode}"
//        realmName = "${Constants.MEDI_JOURNEY}_${currentCountryCode}"
    }

    fun getCountryName(): String {
        val context = MediJourney.getAppContext()
        return when(currentCountryCode) {
            "VN" -> context.getString(R.string.vn)
            "US" -> context.getString(R.string.us)
            "JP" -> context.getString(R.string.jp)
            else -> context.getString(R.string.vn)
        }
    }

    suspend fun getCurrentCountryCode(context: Context): String {
        val defaultCountryCode = "VN"
        val currentCountryCode = DataStoreHelper.getString(context, Constants.currentCountryCode)
        return currentCountryCode ?: defaultCountryCode
    }

    fun getLanguageName(): String {
        val context = MediJourney.getAppContext()
        return when(currentLanguageCode) {
            "ja" -> context.getString(R.string.ja)
            "en" -> context.getString(R.string.en)
            "vi" -> context.getString(R.string.vi)
            else -> context.getString(R.string.en)
        }
    }

    suspend fun getCurrentLanguageCode(context: Context): String {
        val defaultLanguageCode = "en"
        val currentLanguageCode = DataStoreHelper.getString(context, Constants.currentLanguageCode)
        return currentLanguageCode ?: defaultLanguageCode
    }

    // Dashboard
    fun getCurrentAppDashboardMenus(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_dashboard_menus.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    fun getCurrentAppAddMedicalBooking(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_add_medical_booking.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    fun getCurrentAppPaymentMethods(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_payment_method.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    fun getCurrentAppMedicalPurchaseProgress(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_medical_purchase_progress.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    fun getCurrentAppEducationalOrganizations(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_educational_organizations.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    // Health Center
    fun getCurrentAppFitnessTrackerDetail(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_fitness_tracker_detail.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    @Provides
    @AppSleepTrackingReportDetails
    fun getCurrentAppSleepTrackingReportDetails(): Map<String, *> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_sleep_tracking_report_details.json")
        val mapData: Map<String, *> = Gson().fromJson(jsonString, object : TypeToken<Map<String, *>>(){}.type)
        return mapData
    }

    @Provides
    @AppNutritionTrackingReportDetails
    fun getCurrentAppNutritionTrackingReportDetails(): Map<String, *> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_nutrition_tracking_report_details.json")
        val mapData: Map<String, *> = Gson().fromJson(jsonString, object : TypeToken<Map<String, *>>(){}.type)
        return mapData
    }

    @Provides
    @AppExerciseTrackingReportDetails
    fun getCurrentAppExerciseTrackingReportDetails(): Map<String, *> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_exercise_tracking_report_details.json")
        val mapData: Map<String, *> = Gson().fromJson(jsonString, object : TypeToken<Map<String, *>>(){}.type)
        return mapData
    }

    // Profile
    fun getCurrentAppMainProfile(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_main_profile.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    fun getCurrentAppEditProfile(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_edit_profile.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    fun getCurrentAppManageProfile(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_manage_profile.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    fun getCurrentAppShare(): Map<String, Any> {
        val context = MediJourney.getAppContext()
        val jsonString = FileHelper.getValueFromAssets(context, "app_share.json")
        val mapData: Map<String, Any> = Gson().fromJson(jsonString, object : TypeToken<Map<String, Any>>(){}.type)
        return mapData
    }

    // Change Language and Country
    suspend fun updateCurrentLanguageCode(languageCode: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val context = MediJourney.getAppContext()
        DataStoreHelper.saveString(context, Constants.currentLanguageCode, languageCode)
        context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(languageCode)
    }

    suspend fun updateCurrentCountryCode(countryCode: String) {
        val context = MediJourney.getAppContext()
        DataStoreHelper.saveString(context, Constants.currentCountryCode, countryCode)
    }

    fun processChangeCountry(activity: Activity) {
        FireStoreManager.removeAllListeners()
        val intent = Intent(activity, AuthActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}