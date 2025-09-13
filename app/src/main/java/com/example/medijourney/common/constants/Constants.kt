package com.example.medijourney.common.constants

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {

    // Array
    val genderArray: Array<String> = arrayOf("Male", "Female", "Other")
    val countryCodeArray: Array<String> = arrayOf("JP", "US", "VN")
    val languageCodeArray: Array<String> = arrayOf("ja", "en", "vi")
    val authenticatedPreferencesKey: List<Preferences.Key<*>> = listOf()

    // Regex
    const val EMAIL_VALIDATOR_FORMAT = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    const val PHONE_NUMBER_VALIDATOR_FORMAT = "^\\+(?:[0-9] ?){6,14}[0-9]$"

    // Identifiers
    const val HEADER = 0
    const val ITEM = 1
    const val FOOTER = 2
    const val NESTED_RECYCLE_VIEW = 4
    val currentCountryCode = stringPreferencesKey("current_country_code")
    val currentLanguageCode = stringPreferencesKey("current_language_code")
    val biometricAuthUsers = stringPreferencesKey("biometric_auth_users")
    const val SHARED_PREFS_FILENAME = "biometric_prefs"
    const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"
    const val SIGN_IN_BY_PHONE_NUMBER = "SIGN_IN_BY_PHONE_NUMBER"
    const val FORGOT_PASSWORD = "FORGOT_PASSWORD"
    const val ENABLE_OTP_AUTH = "ENABLE_OTP_AUTH"
    const val SIGN_OUT = "SIGN_OUT"
    const val MEDI_JOURNEY = "medi_journey.realm"
    const val IS_VALID = "isValid"
    const val FID = "fid"
    const val CREATED_AT = "created_at"
    const val HIGHLIGHT_USER_MESSAGE_ID = "highlight_user_message_id"
    const val IS_INCOMING_MESSAGE = "is_incoming_message"
    const val IS_FIRST_CONSECUTIVE_FROM_USER = "is_first_consecutive_from_user"
    const val MESSAGE_MENU_ACTIONS = "message_menu_actions"
    const val MESSAGE_DATE = "message_date"
    const val CHAT_GPT = "chat_gpt"
    const val ENABLE_TYPED_ANIMATION = "enable_typed_animation"
    const val ENABLE_FIELD = "enable_field"
    const val HOSPITAL_MAP_DATA = "hospital_map_data"
    const val SUB_SPECIALTY_MAP_DATA = "sub_specialty_map_data"
    const val VALUE = "value"
    const val OBJECT_ID = "object_id"
    const val NUTRITIONX_APP_ID = "37eb8fc6"
    const val NUTRITIONX_APP_KEY = "17314a18ca649e908903fd9951589188"

    // Date Formats
    /**
     * Format for dates like dd/MM/yyyy
     */
    const val DATE_FORMAT_1 = "dd/MM/yyyy"
    /**
     * Format for time like HH:mm
     */
    const val DATE_FORMAT_2 = "HH:mm"
    /**
     * Format for dates like HH:mm dd/MM/yyyy
     */
    const val DATE_FORMAT_3 = "HH:mm dd/MM/yyyy"

    // Number
    const val DEFAULT_LIMIT = 100L

    // Keys
    const val WEB_CLIENT_ID = "797762986274-376j91rm8l9iej1crlgsqvh44pjiumrg.apps.googleusercontent.com"
}