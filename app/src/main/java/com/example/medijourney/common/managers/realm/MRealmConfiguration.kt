package com.example.medijourney.common.managers.realm

import com.example.medijourney.common.managers.InternationManager
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
import com.example.medijourney.common.models.realm_models.UserConversation
import com.example.medijourney.common.models.realm_models.UserExercisePlan
import com.example.medijourney.common.models.realm_models.UserExerciseTrackingReport
import com.example.medijourney.common.models.realm_models.UserFitnessTracker
import com.example.medijourney.common.models.realm_models.UserMedicalProduct
import com.example.medijourney.common.models.realm_models.UserMedicalSpecialty
import com.example.medijourney.common.models.realm_models.UserMembership
import com.example.medijourney.common.models.realm_models.UserMessage
import com.example.medijourney.common.models.realm_models.UserNotification
import com.example.medijourney.common.models.realm_models.UserNutritionTrackingReport
import com.example.medijourney.common.models.realm_models.UserRecommendExercise
import com.example.medijourney.common.models.realm_models.UserRecommendRecipe
import com.example.medijourney.common.models.realm_models.UserSetting
import com.example.medijourney.common.models.realm_models.UserSleepTrackingReport
import io.realm.kotlin.RealmConfiguration

object MRealmConfiguration {

    // Properties
    private const val CURRENT_SCHEMA_VERSION: Long = 3
    val config = RealmConfiguration.Builder(
        schema = setOf(
            Advertisement::class,
            Conversation::class,
            Doctor::class,
            DoctorAppointment::class,
            Exercise::class,
            ExerciseLevel::class,
            FitnessTrackerActivity::class,
            Hospital::class,
            MedicalProduct::class,
            MedicalSpecialty::class,
            MedicalSubSpecialty::class,
            Membership::class,
            Message::class,
            Recipe::class,
            User::class,
            UserConversation::class,
            UserExercisePlan::class,
            UserExerciseTrackingReport::class,
            UserFitnessTracker::class,
            UserMedicalProduct::class,
            UserMedicalSpecialty::class,
            UserMembership::class,
            UserMessage::class,
            UserNotification::class,
            UserNutritionTrackingReport::class,
            UserRecommendExercise::class,
            UserRecommendRecipe::class,
            UserSetting::class,
            UserSleepTrackingReport::class
        )
    )
        .schemaVersion(CURRENT_SCHEMA_VERSION)
        .name(InternationManager.realmName)
        .deleteRealmIfMigrationNeeded()
        .build()
}
