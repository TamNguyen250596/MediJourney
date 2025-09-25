package com.example.medijourney.common.models.realm_models

import com.example.medijourney.common.extensions.getDouble
import com.example.medijourney.common.extensions.getIntSet
import com.example.medijourney.common.extensions.getUserObjectKey
import com.example.medijourney.common.managers.realm.RealmCycle
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey

class UserFitnessTracker: RealmObject, RealmCycle {

    // Properties
    @PrimaryKey
    var id: String = ""
    var userId: String = ""
    var imageName: String? = null
    var name: String? = null
    var model: String? = null
    var version: String? = null
    var ownerUserCode: String = ""
    var deviceId: String = ""
    var batteryPercentage: Double = 0.0
    var batteryMaximumCapacity: Double = 0.0
    var storageCapacity: Double = 0.0
    var storageAvailability: Double = 0.0
    var trackingActivities: RealmSet<Int> = realmSetOf()

    // Functions
    override fun primaryKey(): String {
        return "id"
    }

    override fun create(map: Map<String, Any>): RealmObject {
        return UserFitnessTracker().apply {
            id = map.getUserObjectKey(id)
            userId = map["user_id"] as? String ?: userId
            imageName = map["image_name"] as? String ?: imageName
            name = map["name"] as? String
            model = map["model"] as? String
            version = map["version"] as? String
            ownerUserCode = map["owner_user_code"] as? String ?: ownerUserCode
            deviceId = map["device_id"] as? String ?: deviceId
            batteryPercentage = map.getDouble("battery_percentage")
            batteryMaximumCapacity = map.getDouble("battery_maximum_capacity")
            storageCapacity = map.getDouble("storage_capacity")
            storageAvailability = map.getDouble("storage_availability")
            trackingActivities.addAll(map.getIntSet("tracking_activities"))
        }
    }

    override fun update(map: Map<String, Any>) {
        imageName = map["image_name"] as? String ?: imageName
        name = map["name"] as? String ?: name
        batteryPercentage = map.getDouble("battery_percentage", batteryPercentage)
        batteryMaximumCapacity = map.getDouble("battery_maximum_capacity", batteryMaximumCapacity)
        storageCapacity = map.getDouble("storage_capacity", storageCapacity)
        storageAvailability = map.getDouble("storage_availability", storageAvailability)
        trackingActivities.addAll(map.getIntSet("tracking_activities", trackingActivities))
    }
}