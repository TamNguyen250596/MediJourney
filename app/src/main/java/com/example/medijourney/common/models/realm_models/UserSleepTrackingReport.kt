package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getDouble
import com.example.medijourney.common.extensions.getLocalizedString
import com.example.medijourney.common.extensions.getRealmInstant
import com.example.medijourney.common.extensions.getUserObjectKey
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserSleepTrackingReport: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userCode: String = ""
    var sleepDuration: Double = 0.0
    var reportedAt: RealmInstant? = null
    var n1StageDuration: Double = 0.0
    var n2StageDuration: Double = 0.0
    var n3StageDuration: Double = 0.0
    var remStageDuration: Double = 0.0
    var sleepHealthStatus: String? = null
    var description: String? = null
    var healthyStageDuration: Double = 0.0
    var deviceId: String = ""

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserSleepTrackingReport().apply {
            id = map.getUserObjectKey(id)
            userCode = map["user_code"] as? String ?: userCode
            sleepDuration = map.getDouble("sleep_duration")
            reportedAt = map.getRealmInstant("reported_at")
            n1StageDuration = map.getDouble("n1_stage_duration")
            n2StageDuration = map.getDouble("n2_stage_duration")
            n3StageDuration = map.getDouble("n3_stage_duration")
            remStageDuration = map.getDouble("rem_stage_duration")
            sleepHealthStatus = map["sleep_health_status"] as? String
            description = map.getLocalizedString("description")
            healthyStageDuration = map.getDouble("healthy_stage_duration")
            deviceId = map["device_id"] as? String ?: deviceId
        }
    }

    override fun update(map: Map<String, Any>) {
        sleepDuration = map.getDouble("sleep_duration", sleepDuration)
        reportedAt = map.getRealmInstant("reported_at", reportedAt)
        n1StageDuration = map.getDouble("n1_stage_duration", n1StageDuration)
        n2StageDuration = map.getDouble("n2_stage_duration", n2StageDuration)
        n3StageDuration = map.getDouble("n3_stage_duration", n3StageDuration)
        remStageDuration = map.getDouble("rem_stage_duration", remStageDuration)
        sleepHealthStatus = map["sleep_health_status"] as? String ?: sleepHealthStatus
        description = map.getLocalizedString("description", description)
        healthyStageDuration = map.getDouble("healthy_stage_duration", healthyStageDuration)
        deviceId = map["device_id"] as? String ?: deviceId
    }
}