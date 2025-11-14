package com.example.medijourney.common.managers.fire_store

import com.example.medijourney.common.models.realm_models.Advertisement
import com.example.medijourney.common.models.realm_models.Conversation
import com.example.medijourney.common.models.realm_models.Doctor
import com.example.medijourney.common.models.realm_models.DoctorAppointment
import com.example.medijourney.common.models.realm_models.Exercise
import com.example.medijourney.common.models.realm_models.ExerciseLevel
import com.example.medijourney.common.models.realm_models.FitnessTrackerActivity
import com.example.medijourney.common.models.realm_models.Hospital
import com.example.medijourney.common.models.realm_models.MedicalProduct
import com.example.medijourney.common.models.realm_models.MedicalSpecialty
import com.example.medijourney.common.models.realm_models.MedicalSubSpecialty
import com.example.medijourney.common.models.realm_models.Membership
import com.example.medijourney.common.models.realm_models.Message
import com.example.medijourney.common.models.realm_models.Recipe
import com.example.medijourney.common.models.realm_models.User
import com.example.medijourney.common.models.realm_models.UserExercisePlan
import com.example.medijourney.common.models.realm_models.UserExerciseTrackingReport
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.realm_models.UserMedicalProduct
import com.example.medijourney.common.models.realm_models.UserMedicalSpecialty
import com.example.medijourney.common.models.realm_models.UserMembership
import com.example.medijourney.common.models.realm_models.UserNotification
import com.example.medijourney.common.models.realm_models.UserNutritionTrackingReport
import com.example.medijourney.common.models.realm_models.UserRecommendExercise
import com.example.medijourney.common.models.realm_models.UserRecommendRecipe
import com.example.medijourney.common.models.realm_models.UserSetting
import com.example.medijourney.common.models.realm_models.UserSleepTrackingReport
import io.realm.kotlin.types.RealmObject

enum class FireStoreCollection {
    ADVERTISEMENTS,
    CONVERSATIONS,
    DOCTORS,
    DOCTORS_APPOINTMENTS,
    EXERCISES,
    EXERCISE_LEVELS,
    FITNESS_TRACKER_ACTIVITIES,
    MEDICAL_PRODUCTS,
    HOSPITALS,
    MEDICAL_SPECIALTIES,
    MEDICAL_SUB_SPECIALTIES,
    MEMBERSHIPS,
    MESSAGES,
    RECIPES,
    USER_EXERCISE_PLANS,
    USER_EXERCISE_TRACKING_REPORTS,
    USER_FITNESS_TRACKERS,
    USER_MEDICAL_PRODUCTS,
    USER_MEMBERS,
    USER_MEDICAL_SPECIALTIES,
    USER_MEMBERSHIP,
    USER_NOTIFICATIONS,
    USER_NUTRITION_TRACKING_REPORTS,
    USER_RECOMMEND_EXERCISE,
    USER_RECOMMEND_RECIPES,
    USER_REDUNDANT_DATA,
    USER_SETTINGS,
    USER_SLEEP_TRACKING_REPORTS
}

fun FireStoreCollection.getRealmObject(): RealmObject? {
    when(this) {
        FireStoreCollection.ADVERTISEMENTS -> return Advertisement()
        FireStoreCollection.CONVERSATIONS -> return Conversation()
        FireStoreCollection.DOCTORS -> return Doctor()
        FireStoreCollection.DOCTORS_APPOINTMENTS -> return DoctorAppointment()
        FireStoreCollection.EXERCISES -> return Exercise()
        FireStoreCollection.EXERCISE_LEVELS -> return ExerciseLevel()
        FireStoreCollection.FITNESS_TRACKER_ACTIVITIES -> return FitnessTrackerActivity()
        FireStoreCollection.MEDICAL_PRODUCTS -> return MedicalProduct()
        FireStoreCollection.HOSPITALS -> return Hospital()
        FireStoreCollection.MEDICAL_SPECIALTIES -> return MedicalSpecialty()
        FireStoreCollection.MEDICAL_SUB_SPECIALTIES -> return MedicalSubSpecialty()
        FireStoreCollection.MEMBERSHIPS -> return Membership()
        FireStoreCollection.MESSAGES -> return Message()
        FireStoreCollection.RECIPES -> return Recipe()
        FireStoreCollection.USER_EXERCISE_PLANS -> return UserExercisePlan()
        FireStoreCollection.USER_EXERCISE_TRACKING_REPORTS -> return UserExerciseTrackingReport()
        FireStoreCollection.USER_FITNESS_TRACKERS -> return UserFitnessTracker()
        FireStoreCollection.USER_MEDICAL_PRODUCTS -> return UserMedicalProduct()
        FireStoreCollection.USER_MEMBERS -> return User()
        FireStoreCollection.USER_MEDICAL_SPECIALTIES -> return UserMedicalSpecialty()
        FireStoreCollection.USER_MEMBERSHIP -> return UserMembership()
        FireStoreCollection.USER_NOTIFICATIONS -> return UserNotification()
        FireStoreCollection.USER_NUTRITION_TRACKING_REPORTS -> return UserNutritionTrackingReport()
        FireStoreCollection.USER_RECOMMEND_EXERCISE -> return UserRecommendExercise()
        FireStoreCollection.USER_RECOMMEND_RECIPES -> return UserRecommendRecipe()
        FireStoreCollection.USER_REDUNDANT_DATA -> return null
        FireStoreCollection.USER_SETTINGS -> return UserSetting()
        FireStoreCollection.USER_SLEEP_TRACKING_REPORTS -> return UserSleepTrackingReport()
    }
}