package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getDouble
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserExerciseTrackingReport: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userCode: String = ""
    var deviceId: String = ""
    var reportedAt: RealmInstant? = null
    var burnedCalories: Double = 0.0
    var exerciseDescription: String? = null
    var absExerciseDuration: Double = 0.0
    var chestExerciseDuration: Double = 0.0
    var armsExerciseDuration: Double = 0.0
    var legsExerciseDuration: Double = 0.0
    var shoulderBackExerciseDuration: Double = 0.0

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserExerciseTrackingReport().apply {
            id = map["id"] as? String ?: id
            userCode = map["user_code"] as? String ?: userCode
            deviceId = map["device_id"] as? String ?: deviceId
            reportedAt = map.getRealmInstant("reported_at")
            burnedCalories = map.getDouble("burned_calories")
            exerciseDescription = map.getLocalizedString("exercise_description")
            absExerciseDuration = map.getDouble("abs_exercise_duration")
            chestExerciseDuration = map.getDouble("chest_exercise_duration")
            armsExerciseDuration = map.getDouble("arms_exercise_duration")
            legsExerciseDuration = map.getDouble("legs_exercise_duration")
            shoulderBackExerciseDuration = map.getDouble("shoulder_back_exercise_duration")
        }
    }

    override fun update(map: Map<String, Any>) {
        reportedAt = map.getRealmInstant("reported_at", reportedAt)
        burnedCalories = map.getDouble("burned_calories", burnedCalories)
        exerciseDescription = map.getLocalizedString("exercise_description", exerciseDescription)
        absExerciseDuration = map.getDouble("abs_exercise_duration", absExerciseDuration)
        chestExerciseDuration = map.getDouble("chest_exercise_duration", chestExerciseDuration)
        armsExerciseDuration = map.getDouble("arms_exercise_duration", armsExerciseDuration)
        legsExerciseDuration = map.getDouble("legs_exercise_duration", legsExerciseDuration)
        shoulderBackExerciseDuration = map.getDouble("shoulder_back_exercise_duration", shoulderBackExerciseDuration)
    }
}