package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getDouble
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.extensions.getUserObjectKey
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserNutritionTrackingReport: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userCode: String = ""
    var deviceId: String = ""
    var reportedAt: RealmInstant? = null
    var calorieIntake: Double = 0.0
    var calorieIntakeStatus: String? = null
    var carbohydratesIntake: Double = 0.0
    var lipidsIntake: Double = 0.0
    var proteinsIntake: Double = 0.0
    var macronutrientsIntakeDescription: String? = null
    var waterIntake: Double = 0.0
    var waterIntakeStatus: String? = null
    var waterIntakeDescription: String? = null
    var vitaminIntake: Double = 0.0
    var vitaminIntakeStatus: String? = null
    var vitaminIntakeDescription: String? = null
    var vitaminB1Intake: Double = 0.0
    var vitaminB2Intake: Double = 0.0
    var vitaminB3Intake: Double = 0.0
    var vitaminB5Intake: Double = 0.0
    var vitaminB6Intake: Double = 0.0
    var vitaminB7Intake: Double = 0.0
    var vitaminB9Intake: Double = 0.0
    var vitaminB12Intake: Double = 0.0
    var vitaminCIntake: Double = 0.0
    var vitaminAIntake: Double = 0.0
    var vitaminDIntake: Double = 0.0
    var vitaminEIntake: Double = 0.0
    var vitaminKIntake: Double = 0.0
    var mineralIntake: Double = 0.0
    var mineralIntakeStatus: String? = null
    var mineralIntakeDescription: String? = null
    var calciumIntake: Double = 0.0
    var phosphorousIntake: Double = 0.0
    var magnesiumIntake: Double = 0.0
    var sodiumIntake: Double = 0.0
    var potassiumIntake: Double = 0.0
    var chlorideIntake: Double = 0.0
    var ironIntake: Double = 0.0
    var copperIntake: Double = 0.0
    var zincIntake: Double = 0.0
    var seleniumIntake: Double = 0.0
    var iodineIntake: Double = 0.0

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun toRealmObject(map: Map<String, Any>): RealmObject {
        return UserNutritionTrackingReport().apply {
            id = map.getUserObjectKey(id)
            userCode = map["user_code"] as? String ?: userCode
            deviceId = map["device_id"] as? String ?: deviceId
            reportedAt = map.getRealmInstant("reported_at")
            calorieIntake = map.getDouble("calorie_intake")
            calorieIntakeStatus = map["calorie_intake_status"] as? String
            carbohydratesIntake = map.getDouble("carbohydrates_intake")
            lipidsIntake = map.getDouble("lipids_intake")
            proteinsIntake = map.getDouble("proteins_intake")
            macronutrientsIntakeDescription = map.getLocalizedString("macronutrients_intake_description")
            waterIntake = map.getDouble("water_intake")
            waterIntakeStatus = map["water_intake_status"] as? String
            waterIntakeDescription = map.getLocalizedString("water_intake_description")
            vitaminIntake = map.getDouble("vitamin_intake")
            vitaminIntakeStatus = map["vitamin_intake_status"] as? String
            vitaminIntakeDescription = map.getLocalizedString("vitamin_intake_description")
            vitaminB1Intake = map.getDouble("vitamin_b1_intake")
            vitaminB2Intake = map.getDouble("vitamin_b2_intake")
            vitaminB3Intake = map.getDouble("vitamin_b3_intake")
            vitaminB5Intake = map.getDouble("vitamin_b5_intake")
            vitaminB6Intake = map.getDouble("vitamin_b6_intake")
            vitaminB7Intake = map.getDouble("vitamin_b7_intake")
            vitaminB9Intake = map.getDouble("vitamin_b9_intake")
            vitaminB12Intake = map.getDouble("vitamin_b12_intake")
            vitaminCIntake = map.getDouble("vitamin_c_intake")
            vitaminAIntake = map.getDouble("vitamin_a_intake")
            vitaminDIntake = map.getDouble("vitamin_d_intake")
            vitaminEIntake = map.getDouble("vitamin_e_intake")
            vitaminKIntake = map.getDouble("vitamin_k_intake")
            mineralIntake = map.getDouble("mineral_intake")
            mineralIntakeStatus = map["mineral_intake_status"] as? String
            mineralIntakeDescription = map.getLocalizedString("mineral_intake_description")
            calciumIntake = map.getDouble("calcium_intake")
            phosphorousIntake = map.getDouble("phosphorous_intake")
            magnesiumIntake = map.getDouble("magnesium_intake")
            sodiumIntake = map.getDouble("sodium_intake")
            potassiumIntake = map.getDouble("potassium_intake")
            chlorideIntake = map.getDouble("chloride_intake")
            ironIntake = map.getDouble("iron_intake")
            copperIntake = map.getDouble("copper_intake")
            zincIntake = map.getDouble("zinc_intake")
            seleniumIntake = map.getDouble("selenium_intake")
            iodineIntake = map.getDouble("iodine_intake")
        }
    }

    override fun updateFromMap(map: Map<String, Any>) {
        reportedAt = map.getRealmInstant("reported_at", reportedAt)
        calorieIntake = map.getDouble("calorie_intake", calorieIntake)
        calorieIntakeStatus = map["calorie_intake_status"] as? String ?: calorieIntakeStatus
        carbohydratesIntake = map.getDouble("carbohydrates_intake", carbohydratesIntake)
        lipidsIntake = map.getDouble("lipids_intake", lipidsIntake)
        proteinsIntake = map.getDouble("proteins_intake", proteinsIntake)
        macronutrientsIntakeDescription = map.getLocalizedString("macronutrients_intake_description", macronutrientsIntakeDescription)
        waterIntake = map.getDouble("water_intake", waterIntake)
        waterIntakeStatus = map["water_intake_status"] as? String ?: waterIntakeStatus
        waterIntakeDescription = map.getLocalizedString("water_intake_description", waterIntakeDescription)
        vitaminIntake = map.getDouble("vitamin_intake", vitaminIntake)
        vitaminIntakeStatus = map["vitamin_intake_status"] as? String ?: vitaminIntakeStatus
        vitaminIntakeDescription = map.getLocalizedString("vitamin_intake_description", vitaminIntakeDescription)
        vitaminB1Intake = map.getDouble("vitamin_b1_intake", vitaminB1Intake)
        vitaminB2Intake = map.getDouble("vitamin_b2_intake", vitaminB2Intake)
        vitaminB3Intake = map.getDouble("vitamin_b3_intake", vitaminB3Intake)
        vitaminB5Intake = map.getDouble("vitamin_b5_intake", vitaminB5Intake)
        vitaminB6Intake = map.getDouble("vitamin_b6_intake", vitaminB6Intake)
        vitaminB7Intake = map.getDouble("vitamin_b7_intake", vitaminB7Intake)
        vitaminB9Intake = map.getDouble("vitamin_b9_intake", vitaminB9Intake)
        vitaminB12Intake = map.getDouble("vitamin_b12_intake", vitaminB12Intake)
        vitaminCIntake = map.getDouble("vitamin_c_intake", vitaminCIntake)
        vitaminAIntake = map.getDouble("vitamin_a_intake", vitaminAIntake)
        vitaminDIntake = map.getDouble("vitamin_d_intake", vitaminDIntake)
        vitaminEIntake = map.getDouble("vitamin_e_intake", vitaminEIntake)
        vitaminKIntake = map.getDouble("vitamin_k_intake", vitaminKIntake)
        mineralIntake = map.getDouble("mineral_intake", mineralIntake)
        mineralIntakeStatus = map["mineral_intake_status"] as? String ?: mineralIntakeStatus
        mineralIntakeDescription = map.getLocalizedString("mineral_intake_description", mineralIntakeDescription)
        calciumIntake = map.getDouble("calcium_intake", calciumIntake)
        phosphorousIntake = map.getDouble("phosphorous_intake", phosphorousIntake)
        magnesiumIntake = map.getDouble("magnesium_intake", magnesiumIntake)
        sodiumIntake = map.getDouble("sodium_intake", sodiumIntake)
        potassiumIntake = map.getDouble("potassium_intake", potassiumIntake)
        chlorideIntake = map.getDouble("chloride_intake", chlorideIntake)
        ironIntake = map.getDouble("iron_intake", ironIntake)
        copperIntake = map.getDouble("copper_intake", copperIntake)
        zincIntake = map.getDouble("zinc_intake", zincIntake)
        seleniumIntake = map.getDouble("selenium_intake", seleniumIntake)
        iodineIntake = map.getDouble("iodine_intake", iodineIntake)
    }
}